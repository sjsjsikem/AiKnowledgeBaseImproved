import { Outlet } from 'react-router-dom';

/**
 * AuthLayout 是登录和注册页面共用布局。
 * 它使用 React Router 的 Outlet 渲染子页面，在本项目中把认证表单和项目介绍放在同一个入口视图中。
 */
export function AuthLayout() {
  return (
    <main className="auth-shell">
      <section className="auth-aside">
        <p className="eyebrow">AI Knowledge Base</p>
        <h1>企业级 Java 全栈教程项目</h1>
        <p>认证、权限、知识库、文档、AI 与 RAG 将按阶段逐步实现。</p>
      </section>
      <section className="auth-panel">
        <Outlet />
      </section>
    </main>
  );
}
