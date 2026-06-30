package com.aiknowledgebase.knowledge.controller;

import com.aiknowledgebase.common.ApiResponse;
import com.aiknowledgebase.knowledge.dto.AttachmentResponse;
import com.aiknowledgebase.knowledge.dto.CreateDocumentRequest;
import com.aiknowledgebase.knowledge.dto.CreateKnowledgeBaseRequest;
import com.aiknowledgebase.knowledge.dto.DocumentDetailResponse;
import com.aiknowledgebase.knowledge.dto.DocumentSummaryResponse;
import com.aiknowledgebase.knowledge.dto.DocumentVersionResponse;
import com.aiknowledgebase.knowledge.dto.KnowledgeBaseResponse;
import com.aiknowledgebase.knowledge.dto.UpdateDocumentRequest;
import com.aiknowledgebase.knowledge.dto.UpdateKnowledgeBaseRequest;
import com.aiknowledgebase.knowledge.service.KnowledgeService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * KnowledgeController 是知识库与文档模块的 HTTP 接口入口。
 * 它使用 Spring MVC Controller 注解暴露 /knowledge-bases 和 /documents，在本项目中连接前端知识库页面、编辑器和 KnowledgeService。
 */
@RestController
@RequestMapping
public class KnowledgeController {

    // knowledgeService 来自 KnowledgeService.java，负责知识库、文档、正文和所有权校验的业务编排。
    private final KnowledgeService knowledgeService;

    /**
     * 构造方法由 Spring 注入知识库业务服务。
     * 它让 Controller 只处理 HTTP 参数和统一响应，把所有权校验和事务交给 Service 层。
     *
     * @param knowledgeService 来自 KnowledgeService.java，承接知识库与文档业务逻辑。
     */
    public KnowledgeController(KnowledgeService knowledgeService) {
        this.knowledgeService = knowledgeService;
    }

    /**
     * listKnowledgeBases 是当前用户知识库列表接口。
     * 它调用 KnowledgeService 查询当前用户拥有的知识库，在本项目中支撑知识库首页。
     *
     * @return ApiResponse 包装 KnowledgeBaseResponse 列表。
     */
    @GetMapping("/knowledge-bases")
    public ApiResponse<List<KnowledgeBaseResponse>> listKnowledgeBases() {
        return ApiResponse.success(knowledgeService.listKnowledgeBases());
    }

    /**
     * createKnowledgeBase 是创建知识库接口。
     * 它接收 CreateKnowledgeBaseRequest 并调用 KnowledgeService 写入 knowledge_bases，在本项目中建立文档容器。
     *
     * @param request 来自 CreateKnowledgeBaseRequest.java，由 Spring Validation 校验名称。
     * @return ApiResponse 包装新创建的 KnowledgeBaseResponse。
     */
    @PostMapping("/knowledge-bases")
    public ApiResponse<KnowledgeBaseResponse> createKnowledgeBase(@Valid @RequestBody CreateKnowledgeBaseRequest request) {
        return ApiResponse.success(knowledgeService.createKnowledgeBase(request));
    }

    /**
     * getKnowledgeBase 是知识库详情接口。
     * 它接收路径中的知识库 ID 并调用 KnowledgeService 做所有权校验，在本项目中供文档创建和详情展示复用。
     *
     * @param knowledgeBaseId Spring MVC 从路径中读取的知识库主键。
     * @return ApiResponse 包装 KnowledgeBaseResponse。
     */
    @GetMapping("/knowledge-bases/{knowledgeBaseId}")
    public ApiResponse<KnowledgeBaseResponse> getKnowledgeBase(@PathVariable Long knowledgeBaseId) {
        return ApiResponse.success(knowledgeService.getKnowledgeBase(knowledgeBaseId));
    }

    /**
     * updateKnowledgeBase 是更新知识库接口。
     * 它接收路径 ID 和请求体并调用 KnowledgeService 更新名称和说明，在本项目中支撑知识库资料编辑。
     *
     * @param knowledgeBaseId Spring MVC 从路径中读取的知识库主键。
     * @param request 来自 UpdateKnowledgeBaseRequest.java，由 Spring Validation 校验名称。
     * @return ApiResponse 包装更新后的 KnowledgeBaseResponse。
     */
    @PutMapping("/knowledge-bases/{knowledgeBaseId}")
    public ApiResponse<KnowledgeBaseResponse> updateKnowledgeBase(
            @PathVariable Long knowledgeBaseId,
            @Valid @RequestBody UpdateKnowledgeBaseRequest request
    ) {
        return ApiResponse.success(knowledgeService.updateKnowledgeBase(knowledgeBaseId, request));
    }

    /**
     * deleteKnowledgeBase 是删除知识库接口。
     * 它接收路径 ID 并调用 KnowledgeService 逻辑删除知识库和下属文档，在本项目中演示聚合删除。
     *
     * @param knowledgeBaseId Spring MVC 从路径中读取的知识库主键。
     * @return ApiResponse 包装空响应。
     */
    @DeleteMapping("/knowledge-bases/{knowledgeBaseId}")
    public ApiResponse<Void> deleteKnowledgeBase(@PathVariable Long knowledgeBaseId) {
        knowledgeService.deleteKnowledgeBase(knowledgeBaseId);
        return ApiResponse.success(null);
    }

    /**
     * listDocuments 是知识库下文档列表接口。
     * 它接收知识库 ID 并调用 KnowledgeService 查询 documents 元数据，在本项目中支撑文档列表。
     *
     * @param knowledgeBaseId Spring MVC 从路径中读取的知识库主键。
     * @return ApiResponse 包装 DocumentSummaryResponse 列表。
     */
    @GetMapping("/knowledge-bases/{knowledgeBaseId}/documents")
    public ApiResponse<List<DocumentSummaryResponse>> listDocuments(@PathVariable Long knowledgeBaseId) {
        return ApiResponse.success(knowledgeService.listDocuments(knowledgeBaseId));
    }

    /**
     * createDocument 是创建文档接口。
     * 它接收知识库 ID 和文档请求体并调用 KnowledgeService 分表写入，在本项目中支撑 Markdown 编辑器新建。
     *
     * @param knowledgeBaseId Spring MVC 从路径中读取的知识库主键。
     * @param request 来自 CreateDocumentRequest.java，由 Spring Validation 校验标题和正文。
     * @return ApiResponse 包装新创建的 DocumentDetailResponse。
     */
    @PostMapping("/knowledge-bases/{knowledgeBaseId}/documents")
    public ApiResponse<DocumentDetailResponse> createDocument(
            @PathVariable Long knowledgeBaseId,
            @Valid @RequestBody CreateDocumentRequest request
    ) {
        return ApiResponse.success(knowledgeService.createDocument(knowledgeBaseId, request));
    }

    /**
     * getDocument 是文档详情接口。
     * 它接收文档 ID 并调用 KnowledgeService 合并元数据和正文，在本项目中为编辑器加载已保存文档。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @return ApiResponse 包装 DocumentDetailResponse。
     */
    @GetMapping("/documents/{documentId}")
    public ApiResponse<DocumentDetailResponse> getDocument(@PathVariable Long documentId) {
        return ApiResponse.success(knowledgeService.getDocument(documentId));
    }

    /**
     * updateDocument 是保存文档接口。
     * 它接收文档 ID 和编辑器请求体并调用 KnowledgeService 更新两张表，在本项目中实现 Markdown 保存。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @param request 来自 UpdateDocumentRequest.java，由 Spring Validation 校验标题、状态和正文。
     * @return ApiResponse 包装更新后的 DocumentDetailResponse。
     */
    @PutMapping("/documents/{documentId}")
    public ApiResponse<DocumentDetailResponse> updateDocument(
            @PathVariable Long documentId,
            @Valid @RequestBody UpdateDocumentRequest request
    ) {
        return ApiResponse.success(knowledgeService.updateDocument(documentId, request));
    }

    /**
     * deleteDocument 是删除文档接口。
     * 它接收文档 ID 并调用 KnowledgeService 逻辑删除 documents 记录，在本项目中提供文档列表删除能力。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @return ApiResponse 包装空响应。
     */
    @DeleteMapping("/documents/{documentId}")
    public ApiResponse<Void> deleteDocument(@PathVariable Long documentId) {
        knowledgeService.deleteDocument(documentId);
        return ApiResponse.success(null);
    }

    /**
     * listDocumentVersions 是文档版本历史接口。
     * 它接收文档 ID 并调用 KnowledgeService 查询 document_versions，在本项目中支撑版本历史面板。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @return ApiResponse 包装 DocumentVersionResponse 列表。
     */
    @GetMapping("/documents/{documentId}/versions")
    public ApiResponse<List<DocumentVersionResponse>> listDocumentVersions(@PathVariable Long documentId) {
        return ApiResponse.success(knowledgeService.listDocumentVersions(documentId));
    }

    /**
     * rollbackDocumentVersion 是文档版本回滚接口。
     * 它接收文档 ID 和版本 ID 并调用 KnowledgeService 恢复历史快照，在本项目中实现版本回滚。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @param versionId Spring MVC 从路径中读取的版本主键。
     * @return ApiResponse 包装回滚后的 DocumentDetailResponse。
     */
    @PostMapping("/documents/{documentId}/versions/{versionId}/rollback")
    public ApiResponse<DocumentDetailResponse> rollbackDocumentVersion(
            @PathVariable Long documentId,
            @PathVariable Long versionId
    ) {
        return ApiResponse.success(knowledgeService.rollbackDocumentVersion(documentId, versionId));
    }

    /**
     * deleteDocumentVersion 是文档版本删除接口。
     * 它接收文档 ID 和版本 ID 并调用 KnowledgeService 删除历史快照，在本项目中支持用户清理不需要的版本历史。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @param versionId Spring MVC 从路径中读取的版本主键。
     * @return ApiResponse 包装空响应。
     */
    @DeleteMapping("/documents/{documentId}/versions/{versionId}")
    public ApiResponse<Void> deleteDocumentVersion(
            @PathVariable Long documentId,
            @PathVariable Long versionId
    ) {
        knowledgeService.deleteDocumentVersion(documentId, versionId);
        return ApiResponse.success(null);
    }

    /**
     * listAttachments 是文档附件列表接口。
     * 它接收文档 ID 并调用 KnowledgeService 查询 attachments，在本项目中支撑编辑器附件面板。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @return ApiResponse 包装 AttachmentResponse 列表。
     */
    @GetMapping("/documents/{documentId}/attachments")
    public ApiResponse<List<AttachmentResponse>> listAttachments(@PathVariable Long documentId) {
        return ApiResponse.success(knowledgeService.listAttachments(documentId));
    }

    /**
     * uploadAttachment 是文档附件上传接口。
     * 它接收文档 ID 和 multipart 文件并调用 KnowledgeService 保存文件和元数据，在本项目中实现附件上传。
     *
     * @param documentId Spring MVC 从路径中读取的文档主键。
     * @param file Spring MVC 从 multipart/form-data 中读取的文件参数。
     * @return ApiResponse 包装新上传的 AttachmentResponse。
     */
    @PostMapping(value = "/documents/{documentId}/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<AttachmentResponse> uploadAttachment(
            @PathVariable Long documentId,
            @RequestPart("file") MultipartFile file
    ) {
        return ApiResponse.success(knowledgeService.uploadAttachment(documentId, file));
    }

    /**
     * downloadAttachment 是附件下载接口。
     * 它接收附件 ID 并调用 KnowledgeService 准备 Resource，在本项目中返回文件流而不是 ApiResponse JSON。
     *
     * @param attachmentId Spring MVC 从路径中读取的附件主键。
     * @return ResponseEntity<Resource>，包含文件流和下载响应头。
     */
    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable Long attachmentId) {
        // download 来自 KnowledgeService.AttachmentDownload，包含文件 Resource、原始文件名和 MIME 类型。
        KnowledgeService.AttachmentDownload download = knowledgeService.prepareAttachmentDownload(attachmentId);
        // encodedFilename 使用 KnowledgeService 的 UTF-8 编码方法生成 Content-Disposition 文件名。
        String encodedFilename = knowledgeService.encodeDownloadFilename(download.originalFilename());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFilename)
                .contentLength(download.sizeBytes())
                .contentType(MediaType.parseMediaType(download.contentType()))
                .body(download.resource());
    }

    /**
     * deleteAttachment 是附件删除接口。
     * 它接收附件 ID 并调用 KnowledgeService 删除元数据和文件，在本项目中支撑附件面板删除操作。
     *
     * @param attachmentId Spring MVC 从路径中读取的附件主键。
     * @return ApiResponse 包装空响应。
     */
    @DeleteMapping("/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable Long attachmentId) {
        knowledgeService.deleteAttachment(attachmentId);
        return ApiResponse.success(null);
    }
}
