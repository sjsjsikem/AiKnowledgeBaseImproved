import axios, { AxiosError } from 'axios';
import type { ApiResponse } from '../types/api';
import { useAuthStore } from '../store/auth';

/**
 * ApiError 是前端 API 层的统一错误对象。
 * 它继承 JavaScript 自带 Error，并携带后端 ApiResponse 的 code 和 X-Trace-Id，在本项目中支撑页面错误提示和日志定位。
 */
export class ApiError extends Error {
  /** code 来自后端 ErrorCode.java，经 ApiResponse.code 返回给前端。 */
  readonly code: number;
  /** traceId 来自后端 TraceIdFilter.java 写出的 X-Trace-Id 响应头。 */
  readonly traceId?: string;

  /**
   * constructor 创建 API 错误实例。
   * 它使用 JavaScript Error 基类保存 message，在本项目中把 Axios 异常转换为页面可识别错误。
   *
   * @param code 后端业务错误码，来自 ApiResponse.code 或 HTTP 状态码。
   * @param message 后端错误消息或 Axios 错误消息。
   * @param traceId 后端 TraceIdFilter.java 返回的请求追踪 ID。
   */
  constructor(code: number, message: string, traceId?: string) {
    super(message);
    this.name = 'ApiError';
    this.code = code;
    this.traceId = traceId;
  }
}

/**
 * apiClient 是项目统一 Axios 实例。
 * 它使用 axios.create 配置后端基础路径和超时时间，在本项目中作为所有前端 API 方法的 HTTP 客户端。
 */
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 12000,
});

/**
 * 请求拦截器负责为已登录请求追加 Authorization 头。
 * 它从 auth.ts 的 Zustand store 读取 accessToken，在本项目中避免页面组件重复拼接 Bearer Token。
 */
apiClient.interceptors.request.use((config) => {
  // token 来自 auth.ts 的 Zustand store，是后端 AuthResponse.java 返回的 accessToken。
  const token = useAuthStore.getState().accessToken;
  if (token) {
    // 教学重点：认证头在 Axios 拦截器集中处理，页面组件不需要知道 Token 如何拼接。
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * 响应拦截器负责处理全局认证失效。
 * 它使用 AxiosError 判断 401 状态并清理 auth store，在本项目中让过期 Token 自动退出登录态。
 */
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiResponse<null>>) => {
    if (error.response?.status === 401) {
      // 401 表示当前 Token 已失效或缺失，清理本地登录态，避免用户继续停留在“看似已登录”的界面。
      useAuthStore.getState().clear();
    }
    return Promise.reject(error);
  },
);

/**
 * unwrap 解包后端统一 ApiResponse。
 * 它接收 Axios 请求 Promise，检查 code 是否为 0，并在失败时抛出 ApiError，在本项目中让业务页面只处理成功 data。
 *
 * @param request 来自 apiClient.get/post 等 Axios 方法的请求 Promise。
 * @returns 后端 ApiResponse.data 中的业务数据。
 */
export async function unwrap<T>(request: Promise<{ data: ApiResponse<T>; headers: Record<string, string> }>): Promise<T> {
  try {
    // response 来自 Axios 请求 Promise，包含后端 ApiResponse.java 和响应头。
    const response = await request;
    if (response.data.code !== 0) {
      throw new ApiError(response.data.code, response.data.message, response.headers['x-trace-id']);
    }
    // 后端统一 ApiResponse 后，业务页面只拿 data，错误分支统一抛 ApiError。
    return response.data.data;
  } catch (error) {
    if (error instanceof ApiError) {
      throw error;
    }
    if (axios.isAxiosError<ApiResponse<null>>(error)) {
      // traceId 来自后端 TraceIdFilter.java 写出的 X-Trace-Id，用于前后端排查同一次请求。
      const traceId = error.response?.headers?.['x-trace-id'];
      // code 来自后端 ApiResponse.code 或 HTTP 状态码，用于前端错误分支和提示。
      const code = error.response?.data?.code ?? error.response?.status ?? 50000;
      // message 来自后端 ApiResponse.message 或 Axios 错误消息，用于页面展示。
      const message = error.response?.data?.message ?? error.message;
      // traceId 保留下来后，前端报错截图可以和后端日志按同一次请求定位。
      throw new ApiError(code, message, traceId);
    }
    throw error;
  }
}
