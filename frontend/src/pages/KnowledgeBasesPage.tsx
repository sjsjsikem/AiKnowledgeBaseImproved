import { useQuery } from '@tanstack/react-query';
import { fetchSystemInfo } from '../api/system';
import { StageNotice } from '../components/StageNotice';

/**
 * KnowledgeBasesPage 是知识库列表入口页。
 * 它使用 TanStack Query 调用 system.ts 的系统信息接口，在本项目中先验证前后端连通，Stage 3 再扩展为真实知识库 CRUD。
 */
export function KnowledgeBasesPage() {
  const { data, isLoading, error } = useQuery({
    queryKey: ['system-info'],
    queryFn: fetchSystemInfo,
    retry: 1,
  });

  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Knowledge Bases</p>
          <h1>知识库列表</h1>
        </div>
        <button disabled>新建知识库</button>
      </div>

      <div className="status-grid">
        <article>
          <span>后端连接</span>
          <strong>{isLoading ? '检查中' : error ? '未连接' : data?.status ?? '未知'}</strong>
        </article>
        <article>
          <span>当前阶段</span>
          <strong>{data?.stage ?? 'stage-0-foundation'}</strong>
        </article>
        <article>
          <span>下一阶段</span>
          <strong>RBAC 与管理员基础</strong>
        </article>
      </div>

      <StageNotice
        stage="Stage 3"
        title="知识库 CRUD 将在 Stage 3 实现"
        description="当前页面先固定业务入口。Stage 2 完成 RBAC 后，再实现知识库列表、创建、编辑、删除和所有权校验。"
      />
    </section>
  );
}
