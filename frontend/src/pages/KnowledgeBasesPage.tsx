import { FormEvent, useState } from 'react';
import { Link } from 'react-router-dom';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  createKnowledgeBase,
  deleteDocument,
  deleteKnowledgeBase,
  fetchDocuments,
  fetchKnowledgeBases,
  updateKnowledgeBase,
} from '../api/knowledge';
import { ApiError } from '../api/client';
import type { DocumentSummary, KnowledgeBase } from '../types/api';

/**
 * KnowledgeBasesPage 是 Stage 3 的知识库管理页面。
 * 它使用 knowledge.ts 的知识库和文档 API、TanStack Query 和 React 表单状态，在本项目中演示个人知识库 CRUD 与文档列表入口。
 */
export function KnowledgeBasesPage() {
  // queryClient 来自 TanStack Query，用于在知识库增删改后主动刷新缓存。
  const queryClient = useQueryClient();
  // name 使用 React 自带 useState 保存新建知识库表单中的名称。
  const [name, setName] = useState('');
  // description 使用 React 自带 useState 保存新建知识库表单中的说明。
  const [description, setDescription] = useState('');
  // editingId 保存当前正在编辑的知识库 ID，用于控制哪张知识库卡片显示编辑表单。
  const [editingId, setEditingId] = useState<number | null>(null);
  // editName 保存知识库编辑表单中的名称，用于提交 UpdateKnowledgeBaseRequest.java。
  const [editName, setEditName] = useState('');
  // editDescription 保存知识库编辑表单中的说明，用于提交 UpdateKnowledgeBaseRequest.java。
  const [editDescription, setEditDescription] = useState('');
  // error 使用 React 自带 useState 保存知识库操作失败消息，并在页面顶部展示。
  const [error, setError] = useState('');

  // knowledgeBasesQuery 使用 TanStack Query 的 useQuery，并调用 knowledge.ts 中的 fetchKnowledgeBases。
  // 它在本项目中负责缓存和刷新当前用户的知识库列表。
  const knowledgeBasesQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，用于标识知识库列表缓存。
    queryKey: ['knowledge-bases'],
    // queryFn 调用 fetchKnowledgeBases，把页面查询动作连接到 KnowledgeController.java。
    queryFn: fetchKnowledgeBases,
  });

  // refreshKnowledgeBases 是页面级缓存刷新函数。
  // 它通过 queryClient.invalidateQueries 让知识库列表在写操作后重新从后端加载。
  const refreshKnowledgeBases = () => {
    queryClient.invalidateQueries({ queryKey: ['knowledge-bases'] });
  };

  // createMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 createKnowledgeBase。
  // 它在本项目中把新建知识库表单转换为后端 knowledge_bases 表新增请求。
  const createMutation = useMutation({
    // mutationFn 直接复用 createKnowledgeBase API 方法，把表单 payload 发送到后端。
    mutationFn: createKnowledgeBase,
    // onSuccess 来自 TanStack Query mutation 配置，用于创建成功后清空表单并刷新列表。
    onSuccess: () => {
      setName('');
      setDescription('');
      refreshKnowledgeBases();
    },
    // onError 来自 TanStack Query mutation 配置，用于把失败统一交给 handleError 展示。
    onError: handleError,
  });

  // updateMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 updateKnowledgeBase。
  // 它在本项目中把知识库编辑表单转换为后端 knowledge_bases 表更新请求。
  const updateMutation = useMutation({
    // mutationFn 接收知识库 ID 和表单字段，并调用 KnowledgeController.java 的知识库更新接口。
    mutationFn: ({ knowledgeBaseId, payload }: { knowledgeBaseId: number; payload: { name: string; description?: string } }) =>
      updateKnowledgeBase(knowledgeBaseId, payload),
    // onSuccess 操作成功后退出编辑状态并刷新知识库列表。
    onSuccess: () => {
      setEditingId(null);
      refreshKnowledgeBases();
    },
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: handleError,
  });

  // deleteMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 deleteKnowledgeBase。
  // 它在本项目中把删除按钮转换为后端知识库逻辑删除请求。
  const deleteMutation = useMutation({
    // mutationFn 接收知识库 ID，并调用 KnowledgeController.java 的知识库删除接口。
    mutationFn: deleteKnowledgeBase,
    // onSuccess 操作成功后刷新知识库列表，删除后的卡片会从页面消失。
    onSuccess: refreshKnowledgeBases,
    // onError 操作失败后统一展示 ApiError 或通用错误文案。
    onError: handleError,
  });

  // knowledgeBases 是从 knowledgeBasesQuery 中派生出的知识库列表，接口尚未返回时使用空数组保证 JSX 可安全渲染。
  const knowledgeBases = knowledgeBasesQuery.data ?? [];
  // queryError 是知识库列表加载错误对象，用于页面顶部统一展示数据加载失败。
  const queryError = knowledgeBasesQuery.error;

  /**
   * handleError 统一处理知识库页面操作失败。
   * 它接收 TanStack Query mutation 抛出的错误，在本项目中把 ApiError 消息展示到页面顶部。
   *
   * @param err 来自 knowledge.ts API 调用失败时抛出的错误对象。
   */
  function handleError(err: unknown) {
    setError(err instanceof ApiError ? err.message : '知识库操作失败');
  }

  /**
   * handleCreate 处理创建知识库表单提交。
   * 它使用 React FormEvent 阻止默认刷新，并调用 createMutation 写入后端 knowledge_bases 表。
   *
   * @param event React 提供的表单提交事件，类型来自 React 的 FormEvent。
   */
  function handleCreate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setError('');
    createMutation.mutate({
      name: name.trim(),
      description: description.trim() || undefined,
    });
  }

  /**
   * startEdit 把知识库卡片切换为编辑状态。
   * 它从 KnowledgeBase 响应中读取当前名称和说明，在本项目中让编辑表单显示已有值。
   *
   * @param knowledgeBase 来自 KnowledgeBaseResponse.java 的知识库数据。
   */
  function startEdit(knowledgeBase: KnowledgeBase) {
    setEditingId(knowledgeBase.id);
    setEditName(knowledgeBase.name);
    setEditDescription(knowledgeBase.description ?? '');
  }

  /**
   * handleUpdate 处理知识库编辑表单提交。
   * 它使用 React FormEvent 阻止默认刷新，并调用 updateMutation 更新后端 knowledge_bases 表。
   *
   * @param event React 提供的表单提交事件，类型来自 React 的 FormEvent。
   */
  function handleUpdate(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!editingId) {
      return;
    }
    setError('');
    updateMutation.mutate({
      knowledgeBaseId: editingId,
      payload: {
        name: editName.trim(),
        description: editDescription.trim() || undefined,
      },
    });
  }

  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">Knowledge Bases</p>
          <h1>知识库列表</h1>
        </div>
      </div>

      {(error || queryError) && (
        <p className="form-error">{error || (queryError instanceof ApiError ? queryError.message : '知识库加载失败')}</p>
      )}

      <section className="admin-panel">
        <div className="panel-title">
          <div>
            <p className="eyebrow">Create</p>
            <h2>新建知识库</h2>
          </div>
          <span>{knowledgeBasesQuery.isLoading ? '加载中' : `${knowledgeBases.length} 个知识库`}</span>
        </div>
        <form className="compact-form" onSubmit={handleCreate}>
          <input value={name} onChange={(event) => setName(event.target.value)} placeholder="知识库名称" required />
          <input value={description} onChange={(event) => setDescription(event.target.value)} placeholder="知识库说明" />
          <button type="submit" disabled={createMutation.isPending}>创建知识库</button>
        </form>
      </section>

      <div className="knowledge-grid">
        {knowledgeBases.map((knowledgeBase) => (
          <article className="knowledge-card" key={knowledgeBase.id}>
            {editingId === knowledgeBase.id ? (
              <form className="compact-form" onSubmit={handleUpdate}>
                <input value={editName} onChange={(event) => setEditName(event.target.value)} required />
                <input value={editDescription} onChange={(event) => setEditDescription(event.target.value)} />
                <div className="button-row">
                  <button type="submit" disabled={updateMutation.isPending}>保存</button>
                  <button type="button" onClick={() => setEditingId(null)}>取消</button>
                </div>
              </form>
            ) : (
              <>
                <div className="panel-title">
                  <div>
                    <p className="eyebrow">{knowledgeBase.visibility}</p>
                    <h2>{knowledgeBase.name}</h2>
                  </div>
                  <span>{knowledgeBase.documentCount} 篇文档</span>
                </div>
                <p className="muted">{knowledgeBase.description ?? '暂无说明'}</p>
                <div className="button-row">
                  <Link className="button-link" to={`/documents/new?knowledgeBaseId=${knowledgeBase.id}`}>新建文档</Link>
                  <button type="button" onClick={() => startEdit(knowledgeBase)}>编辑</button>
                  <button type="button" onClick={() => deleteMutation.mutate(knowledgeBase.id)}>删除</button>
                </div>
              </>
            )}
            <DocumentList knowledgeBaseId={knowledgeBase.id} />
          </article>
        ))}
      </div>
    </section>
  );
}

/**
 * DocumentList 是知识库卡片内部的文档列表组件。
 * 它接收知识库 ID 并调用 knowledge.ts 的文档列表接口，在本项目中展示 documents 元数据并提供编辑入口。
 *
 * @param knowledgeBaseId 来自 KnowledgeBase.id 的知识库主键。
 */
function DocumentList({ knowledgeBaseId }: { knowledgeBaseId: number }) {
  // queryClient 来自 TanStack Query，用于文档删除后刷新当前知识库下的文档列表和知识库计数。
  const queryClient = useQueryClient();
  // documentsQuery 使用 TanStack Query 的 useQuery，并调用 knowledge.ts 中的 fetchDocuments。
  // 它在本项目中为单个知识库卡片加载轻量文档元数据。
  const documentsQuery = useQuery({
    // queryKey 是 TanStack Query 自带缓存键，knowledgeBaseId 区分不同知识库下的文档列表。
    queryKey: ['documents', knowledgeBaseId],
    // queryFn 调用 fetchDocuments，把卡片组件连接到 KnowledgeController.java 的文档列表接口。
    queryFn: () => fetchDocuments(knowledgeBaseId),
  });

  // deleteMutation 使用 TanStack Query 的 useMutation，并调用 knowledge.ts 中的 deleteDocument。
  // 它在本项目中把文档删除按钮转换为后端 documents 逻辑删除请求。
  const deleteMutation = useMutation({
    // mutationFn 接收文档 ID，并调用 KnowledgeController.java 的文档删除接口。
    mutationFn: deleteDocument,
    // onSuccess 操作成功后刷新当前文档列表和知识库列表中的文档数量。
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['documents', knowledgeBaseId] });
      queryClient.invalidateQueries({ queryKey: ['knowledge-bases'] });
    },
  });

  // documents 是从 documentsQuery 中派生出的文档列表，接口尚未返回时使用空数组保证 JSX 可安全渲染。
  const documents: DocumentSummary[] = documentsQuery.data ?? [];

  return (
    <div className="document-list">
      {documentsQuery.isLoading && <p className="muted">文档加载中</p>}
      {!documentsQuery.isLoading && documents.length === 0 && <p className="muted">还没有文档</p>}
      {documents.map((document) => (
        <div className="document-row" key={document.id}>
          <div>
            <strong>{document.title}</strong>
            <span>{document.status} · {document.summary ?? '暂无摘要'}</span>
          </div>
          <div className="button-row">
            <Link className="button-link" to={`/documents/${document.id}`}>编辑</Link>
            <button type="button" onClick={() => deleteMutation.mutate(document.id)}>删除</button>
          </div>
        </div>
      ))}
    </div>
  );
}
