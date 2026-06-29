import { Navigate, Outlet, useLocation } from 'react-router-dom';
import { useAuthStore } from '../store/auth';

/**
 * ProtectedRoute 是前端受保护路由组件。
 * 它使用 React Router 的 Navigate、Outlet 和 useLocation，在本项目中阻止未登录用户访问业务页面。
 */
export function ProtectedRoute() {
  // token 来自 auth.ts 的 Zustand store，用于判断当前用户是否已经登录。
  const token = useAuthStore((state) => state.accessToken);
  // location 来自 React Router，用于把用户原本想访问的路径传给登录页。
  const location = useLocation();

  if (!token) {
    // 教学重点：把原始路径放进 state，登录成功后可以回到用户原本想访问的业务页面。
    return <Navigate to="/login" replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
