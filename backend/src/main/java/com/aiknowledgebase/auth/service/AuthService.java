package com.aiknowledgebase.auth.service;

import com.aiknowledgebase.auth.dto.AuthResponse;
import com.aiknowledgebase.auth.dto.LoginRequest;
import com.aiknowledgebase.auth.dto.RegisterRequest;
import com.aiknowledgebase.auth.dto.UserProfile;
import com.aiknowledgebase.auth.entity.User;
import com.aiknowledgebase.auth.mapper.UserMapper;
import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.rbac.service.RbacService;
import com.aiknowledgebase.security.CurrentUser;
import com.aiknowledgebase.security.JwtService;
import com.aiknowledgebase.security.SecurityUtils;
import com.aiknowledgebase.security.TokenBlacklistService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * AuthService 负责 Stage 1 的认证业务编排。
 * 它连接 users 表、Spring Security BCrypt 密码加密、JwtService 签发和当前用户查询，是注册登录闭环的核心业务层。
 */
@Service
public class AuthService {

    // ENABLED 是 users.status 的启用状态值，用于注册初始化和登录校验。
    private static final String ENABLED = "ENABLED";

    // userMapper 来自 UserMapper.java，用 MyBatis-Plus 操作 users 表。
    private final UserMapper userMapper;
    // passwordEncoder 是 Spring Security 提供的密码加密接口，本项目用 BCrypt 保存不可逆密码哈希。
    private final PasswordEncoder passwordEncoder;
    // jwtService 来自 JwtService.java，负责签发和解析登录 Token。
    private final JwtService jwtService;
    // rbacService 来自 RbacService.java，负责注册默认角色和读取真实角色权限。
    private final RbacService rbacService;
    // tokenBlacklistService 来自 TokenBlacklistService.java，用于把退出登录的 JWT 写入 Redis 黑名单。
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * 构造方法由 Spring 自动注入认证业务需要的依赖。
     * 它把用户数据访问、密码加密、JWT 和 RBAC 能力组合到 AuthService 中，支撑注册、登录和权限资料返回。
     *
     * @param userMapper 来自 UserMapper.java，用于查询和写入 users 表。
     * @param passwordEncoder 是 Spring Security 自带的 PasswordEncoder 接口，用于 BCrypt 密码处理。
     * @param jwtService 来自 JwtService.java，用于生成登录 Token。
     * @param rbacService 来自 RbacService.java，用于分配默认角色和加载角色权限。
     * @param tokenBlacklistService 来自 TokenBlacklistService.java，用于服务端退出登录。
     */
    public AuthService(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RbacService rbacService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.rbacService = rbacService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    /**
     * register 使用 RegisterRequest 入参创建新用户。
     * 它通过用户名查重、BCrypt 加密密码、写入 users 表并签发 JWT，在本项目中完成“注册后自动登录”的业务闭环。
     *
     * @param request 来自 RegisterRequest.java，承载前端注册表单提交的用户名、密码、昵称和邮箱。
     * @return AuthResponse 来自 AuthResponse.java，返回 accessToken 和用户资料给前端保存登录态。
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (findByUsername(request.username()) != null) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }

        // 教学重点：注册是一个事务边界。后续 Stage 2 给用户分配默认角色时，也应该和用户创建放在同一事务里。
        User user = new User();
        user.setUsername(request.username());
        // BCrypt 每次 encode 都会生成随机盐；数据库只保存哈希，不保存明文密码。
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setNickname(request.nickname());
        user.setEmail(request.email());
        user.setStatus(ENABLED);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);
        rbacService.assignDefaultUserRole(user.getId());

        return issueToken(user);
    }

    /**
     * login 使用 LoginRequest 校验用户名和密码。
     * 它通过读取 users 表、使用 Spring Security 的 BCrypt 校验密码并签发 JWT，在本项目中完成登录入口。
     *
     * @param request 来自 LoginRequest.java，承载前端登录表单提交的用户名和密码。
     * @return AuthResponse 来自 AuthResponse.java，返回 accessToken 和用户资料给前端进入受保护页面。
     */
    public AuthResponse login(LoginRequest request) {
        User user = findByUsername(request.username());
        if (user == null || !ENABLED.equals(user.getStatus())) {
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        }
        // 使用 matches 校验明文密码和 BCrypt 哈希。不要自己手写字符串比较，也不要尝试“解密”密码。
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        }

        return issueToken(user);
    }

    /**
     * currentUser 读取 Spring Security 上下文中的当前登录用户。
     * 它通过 SecurityUtils 获取 JWT 过滤器写入的 CurrentUser，再查 users 表刷新资料，在本项目中支持前端刷新后的登录态恢复。
     *
     * @return UserProfile 来自 UserProfile.java，返回当前登录用户的安全展示资料。
     */
    public UserProfile currentUser() {
        CurrentUser currentUser = SecurityUtils.currentUser();
        // 当前用户资料重新查库，保证禁用、改昵称、后续权限变更等状态可以被及时反映到前端。
        User user = userMapper.selectById(currentUser.id());
        if (user == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        return toProfile(user, currentUser.roles(), currentUser.permissions());
    }

    /**
     * logout 把当前请求携带的 JWT 写入 Redis 黑名单。
     * 它从 Authorization 头中提取 Bearer Token 并按 JWT 剩余有效期设置 TTL，在本项目中让退出登录后的旧 Token 立即失效。
     *
     * @param authorizationHeader 来自 HTTP Authorization 请求头，由 AuthController.java 传入。
     */
    public void logout(String authorizationHeader) {
        // token 来自 extractBearerToken 的结果，缺失时保持退出登录接口幂等成功。
        String token = extractBearerToken(authorizationHeader);
        if (!StringUtils.hasText(token)) {
            return;
        }
        tokenBlacklistService.blacklist(token, jwtService.remainingTtl(token));
    }

    /**
     * issueToken 使用 JwtService 为已认证用户签发 Token。
     * 它把用户主键和用户名写入 JWT，并组装 AuthResponse，在本项目中把后端认证结果交给前端保存。
     *
     * @param user 来自 User.java 的用户实体，表示已经注册或登录成功的账号。
     * @return AuthResponse 来自 AuthResponse.java，包含 accessToken 和前端需要展示的用户资料。
     */
    private AuthResponse issueToken(User user) {
        String token = jwtService.createToken(user.getId(), user.getUsername());
        return new AuthResponse(token, toProfile(user, rbacService.loadRoleCodes(user.getId()), rbacService.loadPermissionCodes(user.getId())));
    }

    /**
     * findByUsername 使用 MyBatis-Plus 的 LambdaQueryWrapper 查询 users 表。
     * 它通过用户名定位唯一账号，在本项目中服务于注册查重和登录校验。
     *
     * @param username 来自注册或登录请求的用户名字符串。
     * @return User 来自 User.java；如果不存在则返回 null。
     */
    private User findByUsername(String username) {
        return userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("LIMIT 1"));
    }

    /**
     * extractBearerToken 从 Authorization 请求头提取 JWT。
     * 它使用 Spring StringUtils 做空值判断，在本项目中把 HTTP 头格式处理留在认证服务的退出登录流程中。
     *
     * @param authorizationHeader 来自 AuthController.java 的 Authorization 请求头。
     * @return JWT 字符串；请求头缺失或格式不匹配时返回 null。
     */
    private String extractBearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring("Bearer ".length());
    }

    /**
     * toProfile 把数据库实体转换为前端可见的用户资料。
     * 它通过丢弃 passwordHash 等敏感字段，在本项目中保证 Entity 不直接暴露给前端。
     *
     * @param user 来自 User.java 的数据库实体。
     * @param roles 当前用户角色列表，Stage 1 默认来自认证服务，Stage 2 后来自 RBAC 表。
     * @param permissions 当前用户权限列表，Stage 2 后由角色权限关系加载。
     * @return UserProfile 来自 UserProfile.java，是前端安全展示的用户资料。
     */
    private UserProfile toProfile(User user, List<String> roles, List<String> permissions) {
        return new UserProfile(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getEmail(),
                user.getStatus(),
                roles,
                permissions
        );
    }
}
