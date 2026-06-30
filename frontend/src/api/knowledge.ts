import { apiClient, unwrap } from './client';
import type {
  CreateDocumentPayload,
  CreateKnowledgeBasePayload,
  DocumentDetail,
  DocumentSummary,
  KnowledgeBase,
  UpdateDocumentPayload,
  UpdateKnowledgeBasePayload,
} from '../types/api';

/**
 * fetchKnowledgeBases 查询当前用户的知识库列表。
 * 它调用 KnowledgeController.java 的 /knowledge-bases，在本项目中为知识库首页提供数据。
 *
 * @returns KnowledgeBase 列表，包含每个知识库的文档数量。
 */
export function fetchKnowledgeBases() {
  return unwrap<KnowledgeBase[]>(apiClient.get('/knowledge-bases'));
}

/**
 * createKnowledgeBase 创建知识库。
 * 它调用 KnowledgeController.java 的知识库创建接口，在本项目中把前端表单写入 knowledge_bases 表。
 *
 * @param payload 来自 KnowledgeBasesPage.tsx 的知识库创建表单。
 * @returns KnowledgeBase，表示新创建的知识库。
 */
export function createKnowledgeBase(payload: CreateKnowledgeBasePayload) {
  return unwrap<KnowledgeBase>(apiClient.post('/knowledge-bases', payload));
}

/**
 * updateKnowledgeBase 更新知识库资料。
 * 它调用 KnowledgeController.java 的知识库更新接口，在本项目中修改 knowledge_bases.name 和 description。
 *
 * @param knowledgeBaseId 来自 KnowledgeBase.id 的知识库主键。
 * @param payload 来自 KnowledgeBasesPage.tsx 的知识库编辑表单。
 * @returns KnowledgeBase，表示更新后的知识库。
 */
export function updateKnowledgeBase(knowledgeBaseId: number, payload: UpdateKnowledgeBasePayload) {
  return unwrap<KnowledgeBase>(apiClient.put(`/knowledge-bases/${knowledgeBaseId}`, payload));
}

/**
 * deleteKnowledgeBase 删除知识库。
 * 它调用 KnowledgeController.java 的知识库删除接口，在本项目中逻辑删除知识库和下属文档。
 *
 * @param knowledgeBaseId 来自 KnowledgeBase.id 的知识库主键。
 * @returns void，表示后端已完成删除。
 */
export function deleteKnowledgeBase(knowledgeBaseId: number) {
  return unwrap<void>(apiClient.delete(`/knowledge-bases/${knowledgeBaseId}`));
}

/**
 * fetchDocuments 查询某个知识库下的文档列表。
 * 它调用 KnowledgeController.java 的文档列表接口，在本项目中只加载 documents 元数据。
 *
 * @param knowledgeBaseId 来自 KnowledgeBase.id 的知识库主键。
 * @returns DocumentSummary 列表，不包含 Markdown 正文。
 */
export function fetchDocuments(knowledgeBaseId: number) {
  return unwrap<DocumentSummary[]>(apiClient.get(`/knowledge-bases/${knowledgeBaseId}/documents`));
}

/**
 * createDocument 创建文档。
 * 它调用 KnowledgeController.java 的文档创建接口，在本项目中把标题、摘要和 Markdown 正文分表写入后端。
 *
 * @param knowledgeBaseId 来自 KnowledgeBase.id 的知识库主键。
 * @param payload 来自 DocumentEditorPage.tsx 的新建文档表单。
 * @returns DocumentDetail，表示新创建的完整文档。
 */
export function createDocument(knowledgeBaseId: number, payload: CreateDocumentPayload) {
  return unwrap<DocumentDetail>(apiClient.post(`/knowledge-bases/${knowledgeBaseId}/documents`, payload));
}

/**
 * fetchDocument 查询文档详情。
 * 它调用 KnowledgeController.java 的 /documents/{id}，在本项目中为编辑器加载元数据和 Markdown 正文。
 *
 * @param documentId 来自 DocumentSummary.id 或路由参数的文档主键。
 * @returns DocumentDetail，包含 Markdown 正文。
 */
export function fetchDocument(documentId: number) {
  return unwrap<DocumentDetail>(apiClient.get(`/documents/${documentId}`));
}

/**
 * updateDocument 保存已有文档。
 * 它调用 KnowledgeController.java 的文档更新接口，在本项目中同步更新 documents 和 document_contents。
 *
 * @param documentId 来自 DocumentDetail.id 的文档主键。
 * @param payload 来自 DocumentEditorPage.tsx 的编辑器保存表单。
 * @returns DocumentDetail，表示更新后的完整文档。
 */
export function updateDocument(documentId: number, payload: UpdateDocumentPayload) {
  return unwrap<DocumentDetail>(apiClient.put(`/documents/${documentId}`, payload));
}

/**
 * deleteDocument 删除文档。
 * 它调用 KnowledgeController.java 的文档删除接口，在本项目中逻辑删除 documents 记录。
 *
 * @param documentId 来自 DocumentSummary.id 的文档主键。
 * @returns void，表示后端已完成删除。
 */
export function deleteDocument(documentId: number) {
  return unwrap<void>(apiClient.delete(`/documents/${documentId}`));
}
