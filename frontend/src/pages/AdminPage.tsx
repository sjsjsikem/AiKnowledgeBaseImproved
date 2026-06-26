import { StageNotice } from '../components/StageNotice';

/**
 * AdminPage 是管理员后台入口页。
 * 它当前通过 StageNotice 展示 Stage 2 规划，在本项目中预留 RBAC 用户、角色和权限管理的页面位置。
 */
export function AdminPage() {
  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Admin</p>
          <h1>管理员后台</h1>
        </div>
      </div>
      <StageNotice
        stage="Stage 2"
        title="RBAC 与后台管理将在 Stage 2 实现"
        description="用户、角色、权限、用户角色和角色权限会通过 Flyway 建表，并配合后端权限校验和前端菜单控制。"
      />
    </section>
  );
}
