import { ChangeEvent, FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createDocument,
  deleteAttachment,
  deleteDocumentVersion,
  downloadAttachment,
  fetchAttachments,
  fetchDocument,
  fetchDocumentVersions,
  fetchKnowledgeBases,
  rollbackDocumentVersion,
  updateDocument,
  uploadAttachment,
} from '../api/knowledge';
import { ApiError } from '../api/client';
import type { Attachment, DocumentDetail, DocumentVersion } from '../types/api';

/**
 * DocumentEditorPage 是 Stage 4 的 Markdown 文档编辑器。
 * 它使用 knowledge.ts 的文档、附件和版本 API，在本项目中实现文档保存、附件上传、版本历史和回滚。
 */
export function DocumentEditorPage() {
  // navigate 来自 React Router，用于文档新建成功后跳转到已有文档编辑路由。
  const navigate = useNavigate();
  // params 来自 React Router 的 useParams，用于读取 /documents/:documentId 路由参数。
  const params = useParams();
  // searchParams 来自 React Router 的 useSearchParams，用于读取新建文档时传入的 knowledgeBaseId。
  const [searchParams] = useSearchParams();
  // queryClient 来自 TanStack Query，用于保存成功后刷新文档和知识库缓存。
  const queryClient = useQueryClient();

  // routeDocumentId 是从路由参数解析出的文档 ID，新增文档页面没有这个值。
  const routeDocumentId = params.documentId ? Number(params.documentId) : undefined;
  // initialKnowledgeBaseId 是从 query string 解析出的知识库 ID，用于从知识库页点击“新建文档”后的默认选中。
  const initialKnowledgeBaseId = searchParams.get('knowledgeBaseId') ? Number(searchParams.get('knowledgeBaseId')) : undefined;
  // editingExisting 判断当前页面是否在编辑已有文档，用于决定保存时调用创建接口还是更新接口。
  const editingExisting = Boolean(routeDocumentId);

  // knowledgeBaseId 使用 React 自带 useState 保存当前文档所属知识库。
  const [knowledgeBaseId, setKnowledgeBaseId] = useState<number | undefined>(initialKnowledgeBaseId);
  // title 使用 React 自带 useState 保存编辑器中的文档标题。
  const [title, setTitle] = useState('');
  // summary 使用 React 自带 useState 保存编辑器中的文档摘要。
  const [summary, setSummary] = useState('');
  // content 使用 React 自带 useState 保存编辑器中的 Markdown 正文。
  const [content, setContent] = useState('# 新文档\n\n在这里编写 Markdown 内容。');
  // status 使用 React 自带 useState 保存文档状态，对应后端 UpdateDocumentRequest.java 的 status。
  const [status, setStatus] = useState<'DRAFT' | 'PUBLISHED'>('DRAFT');
  // error 使用 React 自带 useState 保存编辑器保存或加载失败消息。
  const [error, setError] = useState('');
  // attachmentError 使用 React 自带 useState 保存附件上传、下载或删除失败消息。
  const [attachmentError, setAttachmentError] = useState('');
  // versionError 使用 React 自带 useState 保存版本历史加载或回滚失败消息。
  const [versionError, setVersionError] = useState('');

  // knowledgeBasesQuery 使用 TanStack Query 的 useQuery，并调用 knowledge.ts 中的 fetchKnowledgeBases。
  // 它在本项目中为新建文档选择所属知识库提供选项。
  const knowledgeBasesQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，用于标识知识库列表缓存。
    queryKey: ['knowledge-bases'],
    // queryFn 调用 fetchKnowledgeBases，从 KnowledgeController.java 读取当前用户知识库。
    queryFn: fetchKnowledgeBases,
  });

  // documentQuery 使用 TanStack Query 的 useQuery，并调用 knowledge.ts 中的 fetchDocument。
  // 它在本项目中为已有文档编辑页面加载标题、摘要、状态和 Markdown 正文。
  const documentQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，routeDocumentId 区分不同文档详情缓存。
    queryKey: ['document', routeDocumentId],
    // queryFn 调用 fetchDocument，把编辑器连接到 KnowledgeController.java 的文档详情接口。
    queryFn: () => fetchDocument(routeDocumentId as number),
    // enabled 使用 TanStack Query 自带开关，只有编辑已有文档时才请求详情。
    enabled: editingExisting,
  });

  // versionsQuery 使用 TanStack Query 的 useQuery，并调用 knowledge.ts 中的 fetchDocumentVersions。
  // 它在本项目中为已有文档加载版本历史，新建文档保存前不会请求。
  const versionsQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，routeDocumentId 区分不同文档的版本历史。
    queryKey: ['document-versions', routeDocumentId],
    // queryFn 调用 fetchDocumentVersions，把编辑器连接到 KnowledgeController.java 的版本列表接口。
    queryFn: () => fetchDocumentVersions(routeDocumentId as number),
    // enabled 使用 TanStack Query 自带开关，只有已有文档才显示版本历史。
    enabled: editingExisting,
  });

  // attachmentsQuery 使用 TanStack Query 的 useQuery，并调用 knowledge.ts 中的 fetchAttachments。
  // 它在本项目中为已有文档加载附件列表，新建文档保存前不会请求。
  const attachmentsQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，routeDocumentId 区分不同文档的附件列表。
    queryKey: ['document-attachments', routeDocumentId],
    // queryFn 调用 fetchAttachments，把编辑器连接到 KnowledgeController.java 的附件列表接口。
    queryFn: () => fetchAttachments(routeDocumentId as number),
    // enabled 使用 TanStack Query 自带开关，只有已有文档才显示附件列表。
    enabled: editingExisting,
  });

  // selectedKnowledgeBase 使用 useMemo 从知识库列表中查找当前选中的知识库。
  // 它在本项目中用于在编辑器顶部显示文档归属，不参与后端安全判断。
  const selectedKnowledgeBase = useMemo(
    () => knowledgeBasesQuery.data?.find((knowledgeBase) => knowledgeBase.id === knowledgeBaseId),
    [knowledgeBasesQuery.data, knowledgeBaseId],
  );

  // saveMutation 使用 TanStack Query 的 useMutation，并根据当前模式调用 createDocument 或 updateDocument。
  // 它在本项目中把编辑器保存按钮转换为后端 documents 和 document_contents 的写操作。
  const saveMutation = useMutation({
    // mutationFn 使用当前页面状态组装请求体，编辑已有文档时更新，新文档时创建。
    mutationFn: () => {
      if (editingExisting && routeDocumentId) {
        return updateDocument(routeDocumentId, {
          title: title.trim(),
          summary: summary.trim() || undefined,
          status,
          content,
        });
      }
      if (!knowledgeBaseId) {
        throw new ApiError(40000, '请先选择知识库');
      }
      return createDocument(knowledgeBaseId, {
        title: title.trim(),
        summary: summary.trim() || undefined,
        content,
      });
    },
    // onSuccess 保存成功后刷新相关缓存，并跳转到已有文档编辑路由。
    onSuccess: (savedDocument: DocumentDetail) => {
      queryClient.invalidateQueries({ queryKey: ['knowledge-bases'] });
      queryClient.invalidateQueries({ queryKey: ['documents', savedDocument.knowledgeBaseId] });
      queryClient.invalidateQueries({ queryKey: ['document', savedDocument.id] });
      queryClient.invalidateQueries({ queryKey: ['document-versions', savedDocument.id] });
      navigate(`/documents/${savedDocument.id}`);
    },
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: (err) => {
      setError(err instanceof ApiError ? err.message : '文档保存失败');
    },
  });

  // rollbackMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 rollbackDocumentVersion。
  // 它在本项目中把版本面板的回滚按钮转换为后端版本恢复请求。
  const rollbackMutation = useMutation({
    // mutationFn 接收版本 ID，并调用后端版本回滚接口恢复当前文档。
    mutationFn: (versionId: number) => rollbackDocumentVersion(routeDocumentId as number, versionId),
    // onSuccess 回滚成功后刷新当前文档、版本历史和文档列表，并同步编辑器表单状态。
    onSuccess: (rolledBackDocument: DocumentDetail) => {
      setTitle(rolledBackDocument.title);
      setSummary(rolledBackDocument.summary ?? '');
      setStatus(rolledBackDocument.status);
      setContent(rolledBackDocument.content);
      queryClient.invalidateQueries({ queryKey: ['document', rolledBackDocument.id] });
      queryClient.invalidateQueries({ queryKey: ['document-versions', rolledBackDocument.id] });
      queryClient.invalidateQueries({ queryKey: ['documents', rolledBackDocument.knowledgeBaseId] });
    },
    // onError 操作失败后展示版本回滚错误。
    onError: (err) => {
      setVersionError(err instanceof ApiError ? err.message : '版本回滚失败');
    },
  });

  // deleteVersionMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 deleteDocumentVersion。
  // 它在本项目中把版本面板的删除按钮转换为后端版本快照删除请求。
  const deleteVersionMutation = useMutation({
    // mutationFn 接收版本 ID，并调用后端版本删除接口清理 document_versions 记录。
    mutationFn: (versionId: number) => deleteDocumentVersion(routeDocumentId as number, versionId),
    // onSuccess 删除成功后刷新当前文档的版本历史列表。
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-versions', routeDocumentId] });
    },
    // onError 操作失败后展示版本删除错误。
    onError: (err) => {
      setVersionError(err instanceof ApiError ? err.message : '版本删除失败');
    },
  });

  // uploadMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 uploadAttachment。
  // 它在本项目中把浏览器 File 对象上传到后端附件接口。
  const uploadMutation = useMutation({
    // mutationFn 接收 File，并调用后端文档附件上传接口。
    mutationFn: (file: File) => uploadAttachment(routeDocumentId as number, file),
    // onSuccess 上传成功后刷新当前文档附件列表。
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-attachments', routeDocumentId] });
    },
    // onError 操作失败后展示附件上传错误。
    onError: (err) => {
      setAttachmentError(err instanceof ApiError ? err.message : '附件上传失败');
    },
  });

  // deleteAttachmentMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 deleteAttachment。
  // 它在本项目中把附件删除按钮转换为后端附件删除请求。
  const deleteAttachmentMutation = useMutation({
    // mutationFn 接收附件 ID，并调用后端附件删除接口。
    mutationFn: deleteAttachment,
    // onSuccess 删除成功后刷新当前文档附件列表。
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['document-attachments', routeDocumentId] });
    },
    // onError 操作失败后展示附件删除错误。
    onError: (err) => {
      setAttachmentError(err instanceof ApiError ? err.message : '附件删除失败');
    },
  });

  useEffect(() => {
    if (documentQuery.data) {
      // 教学重点：编辑已有文档时以后端详情为准，避免页面初始状态覆盖已保存内容。
      setKnowledgeBaseId(documentQuery.data.knowledgeBaseId);
      setTitle(documentQuery.data.title);
      setSummary(documentQuery.data.summary ?? '');
      setStatus(documentQuery.data.status);
      setContent(documentQuery.data.content);
    }
  }, [documentQuery.data]);

  /**
   * handleSave 处理文档编辑器保存。
   * 它使用 React FormEvent 阻止默认刷新，并调用 saveMutation 完成创建或更新。
   *
   * @param event React 提供的表单提交事件，类型来自 React 的 FormEvent。
   */
  function handleSave(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    saveMutation.mutate();
  }

  /**
   * handleUploadAttachment 处理附件上传文件选择。
   * 它接收浏览器 change 事件中的 File 并调用 uploadMutation，在本项目中完成编辑器附件上传。
   *
   * @param file 浏览器文件选择控件返回的 File 对象。
   */
  function handleUploadAttachment(file: File) {
    setAttachmentError('');
    uploadMutation.mutate(file);
  }

  /**
   * handleDownloadAttachment 处理附件下载点击。
   * 它调用 knowledge.ts 的 downloadAttachment 并创建临时 Blob 链接，在本项目中保留带 Token 的 Axios 下载流程。
   *
   * @param attachment 来自 AttachmentResponse.java 的附件数据。
   */
  async function handleDownloadAttachment(attachment: Attachment) {
    try {
      setAttachmentError('');
      // blob 来自附件下载接口返回的文件内容，用于生成浏览器临时下载 URL。
      const blob = await downloadAttachment(attachment.id);
      // url 使用浏览器 URL.createObjectURL 生成临时文件地址，用完后需要释放。
      const url = window.URL.createObjectURL(blob);
      // link 是临时 a 标签，用于触发浏览器下载并保留原始文件名。
      const link = window.document.createElement('a');
      link.href = url;
      link.download = attachment.originalFilename;
      link.click();
      window.URL.revokeObjectURL(url);
    } catch (err) {
      setAttachmentError(err instanceof ApiError ? err.message : '附件下载失败');
    }
  }

  return (
    <section className="page">
      <form onSubmit={handleSave}>
        <div className="page-heading">
          <div>
            <p className="eyebrow">Document Editor</p>
            <h1>{editingExisting ? '编辑 Markdown 文档' : '新建 Markdown 文档'}</h1>
          </div>
          <button type="submit" disabled={saveMutation.isPending || documentQuery.isLoading}>保存文档</button>
        </div>

        {(error || documentQuery.error) && (
          <p className="form-error">{error || (documentQuery.error instanceof ApiError ? documentQuery.error.message : '文档加载失败')}</p>
        )}

        <section className="editor-meta">
          <label>
            所属知识库
            <select
              value={knowledgeBaseId ?? ''}
              onChange={(event) => setKnowledgeBaseId(Number(event.target.value))}
              disabled={editingExisting}
              required
            >
              <option value="">请选择知识库</option>
              {knowledgeBasesQuery.data?.map((knowledgeBase) => (
                <option value={knowledgeBase.id} key={knowledgeBase.id}>{knowledgeBase.name}</option>
              ))}
            </select>
          </label>
          <label>
            标题
            <input value={title} onChange={(event) => setTitle(event.target.value)} placeholder="文档标题" required />
          </label>
          <label>
            摘要
            <input value={summary} onChange={(event) => setSummary(event.target.value)} placeholder="文档摘要" />
          </label>
          <label>
            状态
            <select
              value={status}
              onChange={(event) => setStatus(event.target.value as 'DRAFT' | 'PUBLISHED')}
              disabled={!editingExisting}
            >
              <option value="DRAFT">草稿</option>
              <option value="PUBLISHED">发布</option>
            </select>
          </label>
        </section>

        <div className="editor-shell">
          <textarea value={content} onChange={(event) => setContent(event.target.value)} />
          <article>
            <p className="eyebrow">{selectedKnowledgeBase?.name ?? '未选择知识库'}</p>
            <MarkdownPreview content={content} />
          </article>
        </div>
      </form>

      <div className="document-tools-grid">
        <AttachmentPanel
          attachments={attachmentsQuery.data ?? []}
          disabled={!editingExisting}
          error={attachmentError || (attachmentsQuery.error instanceof ApiError ? attachmentsQuery.error.message : '')}
          loading={attachmentsQuery.isLoading}
          uploading={uploadMutation.isPending}
          deleting={deleteAttachmentMutation.isPending}
          onUpload={handleUploadAttachment}
          onDownload={handleDownloadAttachment}
          onDelete={(attachmentId) => deleteAttachmentMutation.mutate(attachmentId)}
        />
        <VersionHistoryPanel
          versions={versionsQuery.data ?? []}
          disabled={!editingExisting}
          error={versionError || (versionsQuery.error instanceof ApiError ? versionsQuery.error.message : '')}
          loading={versionsQuery.isLoading}
          rollingBack={rollbackMutation.isPending}
          deleting={deleteVersionMutation.isPending}
          onRollback={(versionId) => rollbackMutation.mutate(versionId)}
          onDelete={(versionId) => deleteVersionMutation.mutate(versionId)}
        />
      </div>
    </section>
  );
}

/**
 * AttachmentPanel 是文档编辑器的附件管理面板。
 * 它接收附件列表和上传/下载/删除方法，在本项目中把文件操作集中展示在文档详情页。
 *
 * @param attachments 来自 AttachmentResponse.java 的附件列表。
 * @param disabled 当前是否禁止附件操作，新建文档保存前为 true。
 * @param error 附件操作错误消息。
 * @param loading 附件列表是否加载中。
 * @param uploading 附件是否正在上传。
 * @param deleting 附件是否正在删除。
 * @param onUpload 父组件传入的附件上传方法。
 * @param onDownload 父组件传入的附件下载方法。
 * @param onDelete 父组件传入的附件删除方法。
 */
function AttachmentPanel({
  attachments,
  disabled,
  error,
  loading,
  uploading,
  deleting,
  onUpload,
  onDownload,
  onDelete,
}: {
  attachments: Attachment[];
  disabled: boolean;
  error: string;
  loading: boolean;
  uploading: boolean;
  deleting: boolean;
  onUpload: (file: File) => void;
  onDownload: (attachment: Attachment) => void;
  onDelete: (attachmentId: number) => void;
}) {
  /**
   * handleFileChange 处理文件选择控件变化。
   * 它读取浏览器 FileList 的第一项并交给父组件上传，在本项目中连接 input[type=file] 和后端 multipart 接口。
   *
   * @param event 浏览器 input change 事件，携带用户选择的文件。
   */
  function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    // file 来自浏览器 FileList 的第一项，用户取消选择时可能为空。
    const file = event.target.files?.[0];
    if (file) {
      onUpload(file);
      event.target.value = '';
    }
  }

  return (
    <section className="admin-panel">
      <div className="panel-title">
        <div>
          <p className="eyebrow">Attachments</p>
          <h2>附件</h2>
        </div>
        <span>{disabled ? '保存文档后可上传' : loading ? '加载中' : `${attachments.length} 个附件`}</span>
      </div>
      {error && <p className="form-error">{error}</p>}
      <label className="file-picker">
        选择附件
        <input type="file" disabled={disabled || uploading} onChange={handleFileChange} />
      </label>
      <div className="document-list">
        {!disabled && attachments.length === 0 && !loading && <p className="muted">还没有附件</p>}
        {attachments.map((attachment) => (
          <div className="document-row" key={attachment.id}>
            <div>
              <strong>{attachment.originalFilename}</strong>
              <span>{formatBytes(attachment.sizeBytes)} · {attachment.contentType}</span>
            </div>
            <div className="button-row">
              <button type="button" onClick={() => onDownload(attachment)}>下载</button>
              <button type="button" disabled={deleting} onClick={() => onDelete(attachment.id)}>删除</button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

/**
 * VersionHistoryPanel 是文档编辑器的版本历史面板。
 * 它接收版本列表和回滚方法，在本项目中让学习者看到每次保存都会形成可回滚快照。
 *
 * @param versions 来自 DocumentVersionResponse.java 的版本列表。
 * @param disabled 当前是否禁止版本操作，新建文档保存前为 true。
 * @param error 版本操作错误消息。
 * @param loading 版本历史是否加载中。
 * @param rollingBack 是否正在执行回滚。
 * @param deleting 是否正在删除版本快照。
 * @param onRollback 父组件传入的版本回滚方法。
 * @param onDelete 父组件传入的版本删除方法。
 */
function VersionHistoryPanel({
  versions,
  disabled,
  error,
  loading,
  rollingBack,
  deleting,
  onRollback,
  onDelete,
}: {
  versions: DocumentVersion[];
  disabled: boolean;
  error: string;
  loading: boolean;
  rollingBack: boolean;
  deleting: boolean;
  onRollback: (versionId: number) => void;
  onDelete: (versionId: number) => void;
}) {
  return (
    <section className="admin-panel">
      <div className="panel-title">
        <div>
          <p className="eyebrow">Versions</p>
          <h2>版本历史</h2>
        </div>
        <span>{disabled ? '保存文档后生成' : loading ? '加载中' : `${versions.length} 个版本`}</span>
      </div>
      {error && <p className="form-error">{error}</p>}
      <div className="document-list">
        {!disabled && versions.length === 0 && !loading && <p className="muted">保存后会生成版本快照</p>}
        {versions.map((version) => (
          <div className="version-row" key={version.id}>
            <div>
              <strong>版本 {version.versionNo} · {version.title}</strong>
              <span>{version.status} · {version.summary ?? '暂无摘要'}</span>
              <p>{version.content.slice(0, 120)}</p>
            </div>
            <div className="button-row">
              <button type="button" disabled={rollingBack} onClick={() => onRollback(version.id)}>回滚</button>
              <button type="button" disabled={deleting} onClick={() => onDelete(version.id)}>删除</button>
            </div>
          </div>
        ))}
      </div>
    </section>
  );
}

/**
 * MarkdownPreview 是编辑器右侧的轻量预览组件。
 * 它接收 Markdown 原文并用少量规则渲染标题和段落，在本项目中先提供编辑体验，完整解析器可在后续阶段替换。
 *
 * @param content 来自 DocumentEditorPage 的 Markdown 正文状态。
 */
function MarkdownPreview({ content }: { content: string }) {
  // lines 使用 JavaScript String.split 方法把 Markdown 正文拆成行，用于逐行生成预览节点。
  const lines = content.split('\n');

  return (
    <div className="markdown-preview">
      {lines.map((line, index) => {
        if (line.startsWith('# ')) {
          return <h1 key={index}>{line.slice(2)}</h1>;
        }
        if (line.startsWith('## ')) {
          return <h2 key={index}>{line.slice(3)}</h2>;
        }
        if (!line.trim()) {
          return <br key={index} />;
        }
        return <p key={index}>{line}</p>;
      })}
    </div>
  );
}

/**
 * formatBytes 把附件字节数转换为前端展示文本。
 * 它使用 JavaScript 数字计算 KB/MB，在本项目中让附件列表更容易阅读。
 *
 * @param sizeBytes 来自 AttachmentResponse.java 的附件大小。
 * @return 人类可读的文件大小文本。
 */
function formatBytes(sizeBytes: number) {
  if (sizeBytes < 1024) {
    return `${sizeBytes} B`;
  }
  if (sizeBytes < 1024 * 1024) {
    return `${(sizeBytes / 1024).toFixed(1)} KB`;
  }
  return `${(sizeBytes / 1024 / 1024).toFixed(1)} MB`;
}
