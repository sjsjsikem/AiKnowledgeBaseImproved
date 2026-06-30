package com.aiknowledgebase.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * JwtService 负责 JWT 的签发和解析。
 * 它使用 jjwt 库和 Spring 配置属性读取密钥、过期时间，在本项目中为登录态提供无状态 Token 能力。
 */
@Service
public class JwtService {

    // key 是 jjwt 根据配置密钥生成的 HMAC 签名密钥，用于签发和验签 Token。
    private final SecretKey key;
    // expiration 是 Java time 包的 Duration，用于控制 Token 有效期。
    private final Duration expiration;

    /**
     * 构造方法读取 application.yml 中的 JWT 配置。
     * 它使用 Spring 自带 @Value 注入配置值并生成签名密钥，在本项目中避免把安全参数写死在代码里。
     *
     * @param secret 来自 application.yml 的 app.security.jwt-secret 配置。
     * @param expirationMinutes 来自 application.yml 的 app.security.jwt-expiration-minutes 配置。
     */
    public JwtService(
            @Value("${app.security.jwt-secret}") String secret,
            @Value("${app.security.jwt-expiration-minutes}") long expirationMinutes
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = Duration.ofMinutes(expirationMinutes);
    }

    /**
     * createToken 为登录成功的用户创建 JWT。
     * 它把 User.java 的用户 ID 写入 subject、用户名写入 claim，在本项目中把认证结果交给前端后续请求使用。
     *
     * @param userId 来自 User.java 的用户主键。
     * @param username 来自 User.java 的用户名，用于 Token 中保留最小展示信息。
     * @return String 形式的 JWT，前端会放入 Authorization Bearer 头。
     */
    public String createToken(Long userId, String username) {
        Instant now = Instant.now();
        // 教学重点：Token 中只放最小身份信息。角色和权限每次请求从数据库加载，避免权限变化后旧 Token 继续“自带权限”。
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(key)
                .compact();
    }

    /**
     * parseUserId 解析 JWT 并取出用户 ID。
     * 它使用 jjwt 的 parser 验证签名和过期时间，在本项目中为 JwtAuthenticationFilter 提供当前用户主键。
     *
     * @param token 来自前端 Authorization Bearer 头的 JWT 字符串。
     * @return Long 用户主键，来自 JWT 的 subject 字段。
     */
    public Long parseUserId(String token) {
        // claims 来自 parseClaims 的验签结果，subject 中保存的是 User.java 的用户主键。
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.getSubject());
    }

    /**
     * remainingTtl 计算 JWT 距离过期还剩多久。
     * 它读取 JWT expiration 字段并转换为 Java Duration，在本项目中为 Redis Token 黑名单设置自动过期时间。
     *
     * @param token 来自前端 Authorization Bearer 头的 JWT 字符串。
     * @return Duration 表示 Token 剩余有效期；已过期时返回 Duration.ZERO。
     */
    public Duration remainingTtl(String token) {
        // expiration 来自 JWT 标准过期时间字段，用于让黑名单 key 不超过 Token 原本生命周期。
        Instant expiration = parseClaims(token).getExpiration().toInstant();
        // ttl 使用当前时间和过期时间计算，避免已过期 Token 写入 Redis。
        Duration ttl = Duration.between(Instant.now(), expiration);
        if (ttl.isNegative() || ttl.isZero()) {
            return Duration.ZERO;
        }
        return ttl;
    }

    /**
     * parseClaims 统一解析并校验 JWT。
     * 它使用 jjwt parser 验证签名和过期时间，在本项目中让用户 ID 解析和 TTL 计算复用同一套校验逻辑。
     *
     * @param token 来自前端 Authorization Bearer 头的 JWT 字符串。
     * @return Claims 是 jjwt 提供的 JWT 载荷对象。
     */
    private Claims parseClaims(String token) {
        // verifyWith 会先验证签名和过期时间；验签失败会抛异常，由调用方统一当作未登录处理。
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
