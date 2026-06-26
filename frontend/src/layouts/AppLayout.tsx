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
  const token = useAuthStore((state) => state.accessToken);
  const user = useAuthStore((state) => state.user);
  const setUser = useAuthStore((state) => state.setUser);
  const clear = useAuthStore((state) => state.clear);

  const currentUserQuery = useQuery({
    queryKey: ['current-user'],
    queryFn: fetchCurrentUser,
    enabled: Boolean(token),
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
          <NavLink to="/admin">管理员后台</NavLink>
          <NavLink to="/learning">学习路线</NavLink>
        </nav>
      </aside>
      <main className="main-panel">
        <header className="topbar">
          <span>Stage 1 Authentication</span>
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
