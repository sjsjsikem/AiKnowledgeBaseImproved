/**
 * StageNoticeProps 是阶段提示组件的参数类型。
 * 它由各页面传入阶段、标题和说明，在本项目中统一展示尚未实现功能的教学提示。
 */
interface StageNoticeProps {
  stage: string;
  title: string;
  description: string;
}

/**
 * StageNotice 是阶段占位提示组件。
 * 它接收 StageNoticeProps 并渲染统一提示区域，在本项目中让每个未完成阶段都有明确学习边界。
 *
 * @param stage 当前提示对应的开发阶段。
 * @param title 阶段提示标题。
 * @param description 阶段提示说明。
 */
export function StageNotice({ stage, title, description }: StageNoticeProps) {
  return (
    <section className="stage-notice">
      <span>{stage}</span>
      <div>
        <h2>{title}</h2>
        <p>{description}</p>
      </div>
    </section>
  );
}
