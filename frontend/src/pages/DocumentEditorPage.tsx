import { FormEvent, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import { createDocument, fetchDocument, fetchKnowledgeBases, updateDocument } from '../api/knowledge';
import { ApiError } from '../api/client';
import type { DocumentDetail } from '../types/api';

/**
 * DocumentEditorPage 是 Stage 3 的 Markdown 文档编辑器。
 * 它使用 knowledge.ts 的文档 API、React Router 路由参数和 TanStack Query，在本项目中实现文档新建、加载、保存和预览。
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
      navigate(`/documents/${savedDocument.id}`);
    },
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: (err) => {
      setError(err instanceof ApiError ? err.message : '文档保存失败');
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
