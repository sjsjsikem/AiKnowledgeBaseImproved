import { StageNotice } from '../components/StageNotice';

/**
 * DocumentEditorPage 是文档编辑器入口页。
 * 它当前用静态占位内容保留页面结构，在本项目中为 Stage 3 文档编辑和 Stage 4 版本历史提前确定前端位置。
 */
export function DocumentEditorPage() {
  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Document Editor</p>
          <h1>Markdown 文档编辑器</h1>
        </div>
        <button disabled>保存文档</button>
      </div>
      <div className="editor-shell">
        <textarea disabled value={"# Stage 3\n\nMarkdown 编辑、预览、保存和版本历史将在后续阶段实现。"} readOnly />
        <article>
          <h2>预览区域</h2>
          <p>Stage 3 接入 Markdown 编辑，Stage 4 接入版本历史和附件。</p>
        </article>
      </div>
      <StageNotice
        stage="Stage 3 / Stage 4"
        title="文档、附件与版本历史"
        description="文档正文与元数据会分表设计，避免列表查询读取大字段；版本回滚和附件安全校验将在 Stage 4 讲解。"
      />
    </section>
  );
}
