import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/auth';

/**
 * ProtectedRoute 是前端受保护路由组件。
 * 它使用 React Router 的 Navigate、Outlet 和 useLocation，在本项目中阻止未登录用户访问业务页面。
 */
export function ProtectedRoute() {
  const token = useAuthStore((state) => state.accessToken);
  const location = useLocation();

  if (!token) {
    // 教学重点：把原始路径放进 state，登录成功后可以回到用户原本想访问的业务页面。
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
