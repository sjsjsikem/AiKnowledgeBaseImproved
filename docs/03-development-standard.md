# Development Standard

## 总原则

- 需求先写入 PRD。
- 接口先写入 API 契约。
- 表结构先写入数据库设计和 Flyway migration。
- 代码实现后补充学习指南和阶段清单。

## 后端规范

- Controller 只负责 HTTP 适配、参数校验和返回响应。
- Service 负责业务规则、事务、权限和跨表一致性。
- Mapper 只负责数据访问。
- Entity 只映射数据库表，不直接返回给前端。
- DTO 分为请求 DTO 和响应 DTO。
- 业务异常统一使用 `BusinessException`。
- 所有接口统一返回 `ApiResponse<T>`。
- 所有分页统一返回 `PageResponse<T>`。

## 前端规范

- 页面组件不直接拼接 URL。
- 页面组件不直接读取 `response.data.data`。
- API 调用集中在 `src/api`。
- 类型集中在 `src/types`。
- 登录态和权限信息集中在 Zustand store。
- 路由守卫统一处理未登录和无权限。

## 注释规范

本项目是教程项目，代码注释要服务“新人理解企业级 Java 全栈项目链路”，不是只服务维护者快速浏览。因此从当前阶段开始，所有业务源码都必须持续保持以下注释风格；Stage 0 和 Stage 1 代码也按这个风格补齐。

### 文件、类、组件注释

所有业务源码文件都要在类、组件、模块或主要导出对象顶部添加简短注释，说明：

- 这个类、组件或模块是用来做什么的。
- 它在当前 Java 全栈项目中承担什么作用。

示例：

```java
/**
 * AuthService 负责认证业务编排。
 * 它在本项目中连接用户表、密码加密、JWT 签发和当前用户查询，是登录闭环的核心业务层。
 */
@Service
public class AuthService {
}
```

### 方法、参数和业务变量注释

所有业务源码中的业务方法、构造方法、关键私有方法、依赖字段、业务入参和业务变量都要添加简短注释，说明：

- 这段代码使用的是什么代码或对象。
- 如果来自其他文件，注明来自哪个文件；如果是 Java/Spring/第三方库自带能力，说明是 Java、Spring 或对应库提供的能力。
- 这个方法、参数或变量通过什么操作，在本项目中发挥什么作用。

示例：

```java
/**
 * register 使用 RegisterRequest 入参、UserMapper 数据访问和 PasswordEncoder 密码加密。
 * 它通过创建用户、保存 BCrypt 密码哈希并签发 JWT，在本项目中完成注册后自动登录。
 *
 * @param request 来自 RegisterRequest.java 的注册请求 DTO，承载用户名、密码、昵称和邮箱。
 * @return AuthResponse 来自 AuthResponse.java，返回 accessToken 和用户资料给前端保存登录态。
 */
@Transactional
public AuthResponse register(RegisterRequest request) {
}
```

前端业务变量示例：

```tsx
// usersQuery 使用 TanStack Query 的 useQuery，并调用 admin.ts 中的 fetchAdminUsers。
// 它在本项目中负责缓存和刷新管理员后台的用户分页数据。
const usersQuery = useQuery({
  // queryKey 是 TanStack Query 自带缓存键，用于标识后台用户列表缓存。
  queryKey: ['admin-users'],
  // queryFn 调用 fetchAdminUsers，在本项目中把页面查询动作连接到 AdminRbacController.java。
  queryFn: () => fetchAdminUsers(1, 20),
});
```

### 注释限制

- 注释要短，优先解释“项目作用”和“链路位置”，不要逐行翻译代码。
- “参数/变量”包括业务 DTO 参数、Controller/Service 方法参数、组件 Props、关键事件参数、前端 Hook 状态、Query/Mutation 配置对象、派生展示数据、后端依赖字段和关键业务常量。
- 普通循环变量、简单 JSX 属性、lambda 临时变量、显而易见的字符串拼接不强制逐个注释。
- DTO、Entity、Mapper 可以用类注释和字段/record 参数注释说明职责，不需要给简单 getter/setter 写注释。
- `pom.xml`、`package-lock.json`、构建产物、自动生成文件不按业务注释规范处理。
- 普通字段赋值不需要行内注释，除非它体现安全、事务、权限、缓存、AI 或 RAG 的关键设计。

## 测试规范

- Service 单元测试覆盖业务规则。
- Controller 测试覆盖参数校验、401、403、统一响应。
- 集成测试覆盖认证、RBAC、知识库、文档和 AI Mock Provider。
- 前端构建必须通过 `npm run build`。
