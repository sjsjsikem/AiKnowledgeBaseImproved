import { apiClient, unwrap } from './client';
import type { SystemInfo } from '../types/api';

/**
 * fetchSystemInfo 查询后端系统信息。
 * 它调用 SystemInfoController.java 的 /system/info，在本项目中用于首页验证前后端联通和当前开发阶段。
 *
 * @returns SystemInfo，包含项目名、阶段、运行状态和服务端时间。
 */
export function fetchSystemInfo() {
  return unwrap<SystemInfo>(apiClient.get('/system/info'));
}
