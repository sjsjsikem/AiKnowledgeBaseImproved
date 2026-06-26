import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UserProfile } from '../types/api';

/**
 * AuthState 是前端登录态 Store 的类型定义。
 * 它使用 Zustand 的状态模型保存 Token、用户资料、角色和权限，在本项目中支撑路由守卫和页面权限判断。
 */
interface AuthState {
  /** accessToken 来自 AuthController.java 返回的 AuthResponse，用于 Axios Authorization 头。 */
  accessToken?: string;
  /** user 来自 UserProfile DTO，用于顶部栏展示和当前用户判断。 */
  user?: UserProfile;
  /** roles 来自 UserProfile.roles，Stage 1 默认 USER，Stage 2 后用于菜单和页面权限。 */
  roles: string[];
  /** permissions 来自 UserProfile.permissions，Stage 2 后用于按钮级权限控制。 */
  permissions: string[];
  /** setAuth 使用登录或注册返回值建立完整登录态。 */
  setAuth: (payload: { accessToken: string; user: UserProfile }) => void;
  /** setUser 使用 /users/me 返回值刷新用户资料。 */
  setUser: (user: UserProfile) => void;
  /** clear 清空本地登录态，用于退出登录和 401 失效处理。 */
  clear: () => void;
}

/**
 * useAuthStore 是前端认证状态入口。
 * 它使用 Zustand 和 persist 中间件把登录态保存到 localStorage，在本项目中支持页面刷新后恢复 Token。
 */
export const useAuthStore = create<AuthState>()(
  persist(
    (set) => ({
      roles: [],
      permissions: [],
      setAuth: (payload) =>
        set({
          accessToken: payload.accessToken,
          user: payload.user,
          roles: payload.user.roles,
          permissions: payload.user.permissions,
        }),
      setUser: (user) => set({ user, roles: user.roles, permissions: user.permissions }),
      clear: () => set({ accessToken: undefined, user: undefined, roles: [], permissions: [] }),
    }),
    {
      name: 'ai-kb-auth',
    },
  ),
);
