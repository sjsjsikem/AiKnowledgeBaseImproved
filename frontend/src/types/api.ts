/**
 * ApiResponse 对应后端 ApiResponse.java。
 * 它描述统一响应 code、message、data，在本项目中让 unwrap 可以类型安全地解包后端返回值。
 */
export interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

/**
 * PageResponse 对应后端 PageResponse.java。
 * 它将在知识库、文档、用户管理等列表接口中复用，在本项目中统一前端分页数据结构。
 */
export interface PageResponse<T> {
  items: T[];
  total: number;
  page: number;
  pageSize: number;
}

/**
 * SystemInfo 对应后端 /system/info 返回的数据。
 * 它由 SystemInfoController.java 组装，在本项目中用于展示当前阶段和后端运行状态。
 */
export interface SystemInfo {
  name: string;
  stage: string;
  status: string;
  time: string;
}

/**
 * UserProfile 对应后端 UserProfile.java。
 * 它是不包含 passwordHash 的安全用户资料，在本项目中用于页面展示、路由守卫和后续 RBAC 判断。
 */
export interface UserProfile {
  id: number;
  username: string;
  nickname: string;
  email?: string;
  status: string;
  roles: string[];
  permissions: string[];
}

/**
 * AuthResponse 对应后端 AuthResponse.java。
 * 它把 JWT 和 UserProfile 组合起来，在本项目中作为登录、注册成功后的前端登录态来源。
 */
export interface AuthResponse {
  accessToken: string;
  user: UserProfile;
}

/**
 * LoginPayload 对应后端 LoginRequest.java。
 * 它由 LoginPage.tsx 表单创建，在本项目中作为登录接口请求体。
 */
export interface LoginPayload {
  username: string;
  password: string;
}

/**
 * RegisterPayload 对应后端 RegisterRequest.java。
 * 它由 RegisterPage.tsx 表单创建，在本项目中作为注册接口请求体。
 */
export interface RegisterPayload {
  username: string;
  password: string;
  nickname: string;
  email?: string;
}
