import { apiClient, unwrap } from './client';
import type { AuthResponse, LoginPayload, RegisterPayload, UserProfile } from '../types/api';

/**
 * login 调用后端登录接口。
 * 它使用 apiClient.post 发送 LoginPayload，在本项目中把登录表单提交给 AuthController.java。
 *
 * @param payload 来自 LoginPage.tsx 的登录表单数据。
 * @returns AuthResponse，包含 JWT 和当前用户资料。
 */
export function login(payload: LoginPayload) {
  return unwrap<AuthResponse>(apiClient.post('/auth/login', payload));
}

/**
 * register 调用后端注册接口。
 * 它使用 apiClient.post 发送 RegisterPayload，在本项目中把注册表单提交给 AuthController.java。
 *
 * @param payload 来自 RegisterPage.tsx 的注册表单数据。
 * @returns AuthResponse，注册成功后直接用于建立前端登录态。
 */
export function register(payload: RegisterPayload) {
  return unwrap<AuthResponse>(apiClient.post('/auth/register', payload));
}

/**
 * fetchCurrentUser 查询当前登录用户。
 * 它调用 UserController.java 的 /users/me，在本项目中用于刷新页面后的登录态恢复。
 *
 * @returns UserProfile，来自后端安全用户资料 DTO。
 */
export function fetchCurrentUser() {
  return unwrap<UserProfile>(apiClient.get('/users/me'));
}

/**
 * logout 调用后端退出登录接口。
 * Stage 1 主要由前端清理 Token，保留接口是为了后续 Stage 5 接入 Redis Token 黑名单。
 *
 * @returns void，表示退出请求已完成。
 */
export function logout() {
  return unwrap<void>(apiClient.post('/auth/logout'));
}
