const lessons = [
  '统一响应、异常和 Trace ID',
  'BCrypt、JWT 与登录态恢复',
  'RBAC 权限模型',
  '知识库和文档事务边界',
  '附件安全与版本回滚',
  'Redis Cache Aside',
  'OpenAI 兼容 Provider',
  'RAG 引用与 SSE 流式输出',
];

/**
 * LearningPage 是教程学习路线页。
 * 它使用本文件中的 lessons 阶段清单渲染学习主题，在本项目中帮助新手按阶段理解企业级全栈能力。
 */
export function LearningPage() {
  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Learning Guide</p>
          <h1>学习路线</h1>
        </div>
      </div>
      <div className="lesson-grid">
        {lessons.map((lesson, index) => (
          <article key={lesson} className="lesson-card">
            <span>{String(index + 1).padStart(2, '0')}</span>
            <strong>{lesson}</strong>
          </article>
        ))}
      </div>
    </section>
  );
}
