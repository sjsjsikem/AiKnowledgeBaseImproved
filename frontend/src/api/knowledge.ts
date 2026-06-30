import { apiClient, unwrap } from './client';
import type {
  Attachment,
  CreateDocumentPayload,
  CreateKnowledgeBasePayload,
  DocumentDetail,
  DocumentSummary,
  DocumentVersion,
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

/**
 * fetchDocumentVersions 查询文档版本历史。
 * 它调用 KnowledgeController.java 的版本列表接口，在本项目中为编辑器版本面板提供快照数据。
 *
 * @param documentId 来自 DocumentDetail.id 的文档主键。
 * @returns DocumentVersion 列表，按后端返回顺序展示。
 */
export function fetchDocumentVersions(documentId: number) {
  return unwrap<DocumentVersion[]>(apiClient.get(`/documents/${documentId}/versions`));
}

/**
 * rollbackDocumentVersion 回滚到指定文档版本。
 * 它调用 KnowledgeController.java 的版本回滚接口，在本项目中把历史快照恢复为当前文档。
 *
 * @param documentId 来自 DocumentDetail.id 的文档主键。
 * @param versionId 来自 DocumentVersion.id 的版本主键。
 * @returns DocumentDetail，表示回滚后的当前文档。
 */
export function rollbackDocumentVersion(documentId: number, versionId: number) {
  return unwrap<DocumentDetail>(apiClient.post(`/documents/${documentId}/versions/${versionId}/rollback`));
}

/**
 * deleteDocumentVersion 删除指定文档版本快照。
 * 它调用 KnowledgeController.java 的版本删除接口，在本项目中让用户清理不再需要的历史快照。
 *
 * @param documentId 来自 DocumentDetail.id 的文档主键。
 * @param versionId 来自 DocumentVersion.id 的版本主键。
 * @returns void，表示后端已完成删除。
 */
export function deleteDocumentVersion(documentId: number, versionId: number) {
  return unwrap<void>(apiClient.delete(`/documents/${documentId}/versions/${versionId}`));
}

/**
 * fetchAttachments 查询文档附件列表。
 * 它调用 KnowledgeController.java 的附件列表接口，在本项目中为编辑器附件面板提供数据。
 *
 * @param documentId 来自 DocumentDetail.id 的文档主键。
 * @returns Attachment 列表。
 */
export function fetchAttachments(documentId: number) {
  return unwrap<Attachment[]>(apiClient.get(`/documents/${documentId}/attachments`));
}

/**
 * uploadAttachment 上传文档附件。
 * 它使用浏览器 FormData 构造 multipart/form-data 请求，在本项目中把文件发送到后端附件接口。
 *
 * @param documentId 来自 DocumentDetail.id 的文档主键。
 * @param file 浏览器文件选择控件返回的 File 对象。
 * @returns Attachment，表示新上传的附件元数据。
 */
export function uploadAttachment(documentId: number, file: File) {
  // formData 是浏览器 FormData 对象，用于按后端 @RequestPart("file") 要求提交文件字段。
  const formData = new FormData();
  formData.append('file', file);
  return unwrap<Attachment>(apiClient.post(`/documents/${documentId}/attachments`, formData));
}

/**
 * downloadAttachment 下载文档附件。
 * 它调用附件下载接口并以 Blob 形式接收文件，在本项目中保留 Axios Authorization 头。
 *
 * @param attachmentId 来自 Attachment.id 的附件主键。
 * @returns Blob，表示浏览器可保存的文件内容。
 */
export async function downloadAttachment(attachmentId: number) {
  // response 来自 Axios blob 请求，文件下载不走 ApiResponse JSON 包装。
  const response = await apiClient.get<Blob>(`/attachments/${attachmentId}/download`, { responseType: 'blob' });
  return response.data;
}

/**
 * deleteAttachment 删除文档附件。
 * 它调用 KnowledgeController.java 的附件删除接口，在本项目中删除附件元数据和本地文件。
 *
 * @param attachmentId 来自 Attachment.id 的附件主键。
 * @returns void，表示后端已完成删除。
 */
export function deleteAttachment(attachmentId: number) {
  return unwrap<void>(apiClient.delete(`/attachments/${attachmentId}`));
}
