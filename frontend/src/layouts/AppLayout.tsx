import { NavLink, Outlet } from 'react-router-dom';
import { useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchCurrentUser, logout } from '../api/auth';
import { useAuthStore } from '../store/auth';

/**
 * AppLayout 是登录后业务页面的整体布局。
 * 它使用 React Router 的 Outlet、TanStack Query 和 auth store，在本项目中承载侧边导航、顶部用户信息和登录态刷新。
 */
export function AppLayout() {
  // token 来自 auth.ts 的 Zustand store，用于判断是否需要查询当前用户。
  const token = useAuthStore((state) => state.accessToken);
  // user 来自 auth.ts 的 Zustand store，用于顶部栏展示当前登录用户。
  const user = useAuthStore((state) => state.user);
  // permissions 来自 auth.ts 的 Zustand store，用于前端菜单级权限控制。
  const permissions = useAuthStore((state) => state.permissions);
  // setUser 来自 auth.ts 的 Zustand store，用于用 /users/me 返回值刷新用户资料。
  const setUser = useAuthStore((state) => state.setUser);
  // clear 来自 auth.ts 的 Zustand store，用于退出登录时清空本地登录态。
  const clear = useAuthStore((state) => state.clear);
  // canAccessAdmin 根据 RBAC 权限编码计算管理员菜单是否可见。
  // 它只负责前端体验控制，真正安全边界仍由后端 SecurityConfig.java 控制。
  const canAccessAdmin = permissions.includes('admin:user:read') || permissions.includes('admin:role:read');

  // currentUserQuery 使用 TanStack Query 调用 auth.ts 的 fetchCurrentUser。
  // 它在本项目中负责页面刷新后用 Token 重新加载后端当前用户资料和权限。
  const currentUserQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，用于标识当前用户资料缓存。
    queryKey: ['current-user'],
    // queryFn 调用 fetchCurrentUser，把布局组件连接到 UserController.java 的 /users/me。
    queryFn: fetchCurrentUser,
    // enabled 使用 TanStack Query 自带开关，只有本地存在 Token 时才请求当前用户。
    enabled: Boolean(token),
    // retry 是 TanStack Query 自带重试配置，避免当前用户接口失败时重复请求过多。
    retry: 1,
  });

  useEffect(() => {
    if (currentUserQuery.data) {
      // 教学重点：localStorage 只负责恢复 Token；用户昵称、状态、后续权限仍以后端 /users/me 为准。
      setUser(currentUserQuery.data);
    }
  }, [currentUserQuery.data, setUser]);

  /**
   * handleLogout 处理退出登录点击。
   * 它调用 auth.ts 中的 logout 接口并清理 Zustand 登录态，在本项目中完成前端退出流程。
   */
  async function handleLogout() {
    try {
      await logout();
    } finally {
      // Stage 1 的退出登录是前端清 Token；Stage 5 接入 Redis 后再做服务端 Token 黑名单。
      clear();
      window.location.href = '/login';
    }
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <strong>ai-knowledge-base</strong>
          <span>Enterprise Tutorial</span>
        </div>
        <nav className="nav-list" aria-label="主导航">
          <NavLink to="/knowledge-bases">知识库</NavLink>
          <NavLink to="/documents/new">文档编辑</NavLink>
          {canAccessAdmin && <NavLink to="/admin">管理员后台</NavLink>}
          <NavLink to="/learning">学习路线</NavLink>
        </nav>
      </aside>
      <main className="main-panel">
        <header className="topbar">
          <span>Stage 2 RBAC</span>
          <div className="topbar-user">
            <strong>{user?.nickname ?? user?.username}</strong>
            <button type="button" onClick={handleLogout}>退出</button>
          </div>
        </header>
        <Outlet />
      </main>
    </div>
  );
}
