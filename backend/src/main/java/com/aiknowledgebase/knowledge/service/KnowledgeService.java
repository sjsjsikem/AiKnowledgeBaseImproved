package com.aiknowledgebase.knowledge.service;

import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.knowledge.dto.CreateDocumentRequest;
import com.aiknowledgebase.knowledge.dto.CreateKnowledgeBaseRequest;
import com.aiknowledgebase.knowledge.dto.AttachmentResponse;
import com.aiknowledgebase.knowledge.dto.DocumentDetailResponse;
import com.aiknowledgebase.knowledge.dto.DocumentSummaryResponse;
import com.aiknowledgebase.knowledge.dto.DocumentVersionResponse;
import com.aiknowledgebase.knowledge.dto.KnowledgeBaseResponse;
import com.aiknowledgebase.knowledge.dto.UpdateDocumentRequest;
import com.aiknowledgebase.knowledge.dto.UpdateKnowledgeBaseRequest;
import com.aiknowledgebase.knowledge.entity.Attachment;
import com.aiknowledgebase.knowledge.entity.Document;
import com.aiknowledgebase.knowledge.entity.DocumentContent;
import com.aiknowledgebase.knowledge.entity.DocumentVersion;
import com.aiknowledgebase.knowledge.entity.KnowledgeBase;
import com.aiknowledgebase.knowledge.mapper.AttachmentMapper;
import com.aiknowledgebase.knowledge.mapper.DocumentContentMapper;
import com.aiknowledgebase.knowledge.mapper.DocumentMapper;
import com.aiknowledgebase.knowledge.mapper.DocumentVersionMapper;
import com.aiknowledgebase.knowledge.mapper.KnowledgeBaseMapper;
import com.aiknowledgebase.security.CurrentUser;
import com.aiknowledgebase.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * KnowledgeService 负责知识库和文档的业务编排。
 * 它连接 knowledge_bases、documents、document_contents 三张表，在本项目中实现用户自己的知识库 CRUD 和 Markdown 文档编辑。
 */
@Service
public class KnowledgeService {

    // PRIVATE_VISIBILITY 是 Stage 3 知识库默认可见性，用于先完成个人知识库所有权模型。
    private static final String PRIVATE_VISIBILITY = "PRIVATE";
    // DRAFT_STATUS 是文档草稿状态，用于新建文档的默认状态。
    private static final String DRAFT_STATUS = "DRAFT";
    // PUBLISHED_STATUS 是文档发布状态，用于 Stage 3 演示文档状态字段如何进入业务规则。
    private static final String PUBLISHED_STATUS = "PUBLISHED";
    // SUPPORTED_DOCUMENT_STATUSES 使用 Java Set 保存允许的文档状态，在本项目中集中校验前端提交值。
    private static final Set<String> SUPPORTED_DOCUMENT_STATUSES = Set.of(DRAFT_STATUS, PUBLISHED_STATUS);

    // knowledgeBaseMapper 来自 KnowledgeBaseMapper.java，用于读写 knowledge_bases 表。
    private final KnowledgeBaseMapper knowledgeBaseMapper;
    // documentMapper 来自 DocumentMapper.java，用于读写 documents 元数据表。
    private final DocumentMapper documentMapper;
    // documentContentMapper 来自 DocumentContentMapper.java，用于读写 document_contents 正文表。
    private final DocumentContentMapper documentContentMapper;
    // documentVersionMapper 来自 DocumentVersionMapper.java，用于写入和读取文档版本快照。
    private final DocumentVersionMapper documentVersionMapper;
    // attachmentMapper 来自 AttachmentMapper.java，用于写入和读取附件元数据。
    private final AttachmentMapper attachmentMapper;
    // attachmentsRoot 使用 Spring @Value 从 application.yml 或默认值读取附件根目录。
    private final Path attachmentsRoot;

    /**
     * 构造方法由 Spring 注入知识库模块需要的 Mapper。
     * 它把知识库、文档元数据和文档正文的数据访问能力组合到一个业务服务中。
     *
     * @param knowledgeBaseMapper 来自 KnowledgeBaseMapper.java，用于 knowledge_bases 表 CRUD。
     * @param documentMapper 来自 DocumentMapper.java，用于 documents 表 CRUD。
     * @param documentContentMapper 来自 DocumentContentMapper.java，用于 document_contents 表 CRUD。
     * @param documentVersionMapper 来自 DocumentVersionMapper.java，用于 document_versions 表 CRUD。
     * @param attachmentMapper 来自 AttachmentMapper.java，用于 attachments 表 CRUD。
     * @param attachmentsDir 来自 Spring @Value 的附件根目录配置。
     */
    public KnowledgeService(
            KnowledgeBaseMapper knowledgeBaseMapper,
            DocumentMapper documentMapper,
            DocumentContentMapper documentContentMapper,
            DocumentVersionMapper documentVersionMapper,
            AttachmentMapper attachmentMapper,
            @Value("${app.storage.attachments-dir:uploads/attachments}") String attachmentsDir
    ) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentMapper = documentMapper;
        this.documentContentMapper = documentContentMapper;
        this.documentVersionMapper = documentVersionMapper;
        this.attachmentMapper = attachmentMapper;
        this.attachmentsRoot = Paths.get(attachmentsDir).toAbsolutePath().normalize();
    }

    /**
     * listKnowledgeBases 查询当前用户的知识库列表。
     * 它通过 SecurityUtils.java 读取登录用户并按 ownerId 查询，在本项目中保证用户只看到自己的知识库。
     *
     * @return KnowledgeBaseResponse 列表，包含每个知识库的文档数量。
     */
    public List<KnowledgeBaseResponse> listKnowledgeBases() {
        // currentUser 来自 SecurityUtils.java 的 Spring Security 上下文，用于确定知识库所有权查询条件。
        CurrentUser currentUser = SecurityUtils.currentUser();
        return knowledgeBaseMapper.selectList(new LambdaQueryWrapper<KnowledgeBase>()
                        .eq(KnowledgeBase::getOwnerId, currentUser.id())
                        .orderByDesc(KnowledgeBase::getUpdatedAt))
                .stream()
                .map(this::toKnowledgeBaseResponse)
                .toList();
    }

    /**
     * createKnowledgeBase 创建当前用户的知识库。
     * 它把请求 DTO 转为 KnowledgeBase 实体并写入 knowledge_bases，在本项目中建立文档管理的顶层容器。
     *
     * @param request 来自 CreateKnowledgeBaseRequest.java，承载知识库名称和说明。
     * @return KnowledgeBaseResponse 新创建的知识库响应。
     */
    @Transactional
    public KnowledgeBaseResponse createKnowledgeBase(CreateKnowledgeBaseRequest request) {
        // currentUser 来自 SecurityUtils.java，用于把新知识库绑定到当前登录用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // now 使用 Java LocalDateTime 记录创建和更新时间，在本项目中保持审计字段一致。
        LocalDateTime now = LocalDateTime.now();
        // knowledgeBase 是即将写入 knowledge_bases 表的新知识库实体，由 CreateKnowledgeBaseRequest.java 转换而来。
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setOwnerId(currentUser.id());
        knowledgeBase.setName(request.name().trim());
        knowledgeBase.setDescription(normalizeText(request.description()));
        knowledgeBase.setVisibility(PRIVATE_VISIBILITY);
        knowledgeBase.setDeleted(0);
        knowledgeBase.setCreatedAt(now);
        knowledgeBase.setUpdatedAt(now);
        knowledgeBaseMapper.insert(knowledgeBase);
        return toKnowledgeBaseResponse(knowledgeBase);
    }

    /**
     * getKnowledgeBase 查询当前用户拥有的单个知识库。
     * 它复用所有权校验方法，在本项目中为详情页和文档创建前校验提供数据。
     *
     * @param knowledgeBaseId 来自路径参数的知识库主键。
     * @return KnowledgeBaseResponse 知识库详情。
     */
    public KnowledgeBaseResponse getKnowledgeBase(Long knowledgeBaseId) {
        // currentUser 来自 SecurityUtils.java，用于判断路径中的知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        return toKnowledgeBaseResponse(findOwnedKnowledgeBase(knowledgeBaseId, currentUser.id()));
    }

    /**
     * updateKnowledgeBase 更新当前用户拥有的知识库。
     * 它只允许修改名称和说明，在本项目中避免 ownerId、visibility 等内部字段被前端越权修改。
     *
     * @param knowledgeBaseId 来自路径参数的知识库主键。
     * @param request 来自 UpdateKnowledgeBaseRequest.java，承载可编辑字段。
     * @return KnowledgeBaseResponse 更新后的知识库响应。
     */
    @Transactional
    public KnowledgeBaseResponse updateKnowledgeBase(Long knowledgeBaseId, UpdateKnowledgeBaseRequest request) {
        // currentUser 来自 SecurityUtils.java，用于校验当前用户是否拥有该知识库。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // knowledgeBase 来自 knowledge_bases 表，用于执行名称和说明更新。
        KnowledgeBase knowledgeBase = findOwnedKnowledgeBase(knowledgeBaseId, currentUser.id());
        knowledgeBase.setName(request.name().trim());
        knowledgeBase.setDescription(normalizeText(request.description()));
        knowledgeBase.setUpdatedAt(LocalDateTime.now());
        knowledgeBaseMapper.updateById(knowledgeBase);
        return toKnowledgeBaseResponse(knowledgeBase);
    }

    /**
     * deleteKnowledgeBase 逻辑删除当前用户拥有的知识库和下属文档。
     * 它先校验所有权再删除 documents 与 knowledge_bases，在本项目中演示聚合根删除的事务边界。
     *
     * @param knowledgeBaseId 来自路径参数的知识库主键。
     */
    @Transactional
    public void deleteKnowledgeBase(Long knowledgeBaseId) {
        // currentUser 来自 SecurityUtils.java，用于校验当前用户是否拥有该知识库。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedKnowledgeBase(knowledgeBaseId, currentUser.id());
        documentMapper.delete(new LambdaQueryWrapper<Document>().eq(Document::getKnowledgeBaseId, knowledgeBaseId));
        knowledgeBaseMapper.deleteById(knowledgeBaseId);
    }

    /**
     * listDocuments 查询某个知识库下的文档列表。
     * 它先校验知识库所有权，再读取 documents 元数据表，在本项目中避免列表接口加载 Markdown 大字段。
     *
     * @param knowledgeBaseId 来自路径参数的知识库主键。
     * @return DocumentSummaryResponse 列表。
     */
    public List<DocumentSummaryResponse> listDocuments(Long knowledgeBaseId) {
        // currentUser 来自 SecurityUtils.java，用于校验当前用户是否拥有该知识库。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedKnowledgeBase(knowledgeBaseId, currentUser.id());
        return documentMapper.selectList(new LambdaQueryWrapper<Document>()
                        .eq(Document::getKnowledgeBaseId, knowledgeBaseId)
                        .orderByDesc(Document::getUpdatedAt))
                .stream()
                .map(this::toDocumentSummaryResponse)
                .toList();
    }

    /**
     * createDocument 在指定知识库下创建文档。
     * 它校验知识库所有权后分别写入 documents 和 document_contents，在本项目中体现元数据与正文分表设计。
     *
     * @param knowledgeBaseId 来自路径参数的知识库主键。
     * @param request 来自 CreateDocumentRequest.java，承载标题、摘要和 Markdown 正文。
     * @return DocumentDetailResponse 新创建文档的完整详情。
     */
    @Transactional
    public DocumentDetailResponse createDocument(Long knowledgeBaseId, CreateDocumentRequest request) {
        // currentUser 来自 SecurityUtils.java，用于校验当前用户是否拥有该知识库。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedKnowledgeBase(knowledgeBaseId, currentUser.id());
        // now 使用 Java LocalDateTime 记录文档元数据和正文的创建时间。
        LocalDateTime now = LocalDateTime.now();
        // document 是即将写入 documents 表的新文档元数据，由 CreateDocumentRequest.java 转换而来。
        Document document = new Document();
        document.setKnowledgeBaseId(knowledgeBaseId);
        document.setTitle(request.title().trim());
        document.setSummary(normalizeText(request.summary()));
        document.setStatus(DRAFT_STATUS);
        document.setDeleted(0);
        document.setCreatedAt(now);
        document.setUpdatedAt(now);
        documentMapper.insert(document);

        // content 是即将写入 document_contents 表的 Markdown 正文，与 documentId 建立一对一关系。
        DocumentContent content = new DocumentContent();
        content.setDocumentId(document.getId());
        content.setContent(request.content());
        content.setCreatedAt(now);
        content.setUpdatedAt(now);
        documentContentMapper.insert(content);
        createVersionSnapshot(document, content, currentUser.id(), now);
        return toDocumentDetailResponse(document, content);
    }

    /**
     * getDocument 查询当前用户可访问的文档详情。
     * 它先找到文档元数据并校验所属知识库所有权，再读取正文表，在本项目中为编辑器加载完整数据。
     *
     * @param documentId 来自路径参数的文档主键。
     * @return DocumentDetailResponse 文档完整详情。
     */
    public DocumentDetailResponse getDocument(Long documentId) {
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // document 来自 documents 表，用于读取元数据并找到正文关系。
        Document document = findOwnedDocument(documentId, currentUser.id());
        return toDocumentDetailResponse(document, findDocumentContent(document.getId()));
    }

    /**
     * updateDocument 更新当前用户可访问的文档。
     * 它校验文档状态、更新元数据并保存 Markdown 正文，在本项目中实现编辑器保存能力。
     *
     * @param documentId 来自路径参数的文档主键。
     * @param request 来自 UpdateDocumentRequest.java，承载标题、摘要、状态和正文。
     * @return DocumentDetailResponse 更新后的文档完整详情。
     */
    @Transactional
    public DocumentDetailResponse updateDocument(Long documentId, UpdateDocumentRequest request) {
        if (!SUPPORTED_DOCUMENT_STATUSES.contains(request.status())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "文档状态只支持 DRAFT 或 PUBLISHED");
        }
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // document 来自 documents 表，用于更新标题、摘要和状态。
        Document document = findOwnedDocument(documentId, currentUser.id());
        // content 来自 document_contents 表，用于更新 Markdown 正文。
        DocumentContent content = findDocumentContent(document.getId());
        // now 使用 Java LocalDateTime 记录本次保存时间，同步更新元数据和正文表。
        LocalDateTime now = LocalDateTime.now();
        document.setTitle(request.title().trim());
        document.setSummary(normalizeText(request.summary()));
        document.setStatus(request.status());
        document.setUpdatedAt(now);
        documentMapper.updateById(document);
        content.setContent(request.content());
        content.setUpdatedAt(now);
        documentContentMapper.updateById(content);
        createVersionSnapshot(document, content, currentUser.id(), now);
        return toDocumentDetailResponse(document, content);
    }

    /**
     * deleteDocument 逻辑删除当前用户可访问的文档。
     * 它校验文档所属知识库所有权后删除 documents 记录，在本项目中保留正文表用于后续版本历史教学讨论。
     *
     * @param documentId 来自路径参数的文档主键。
     */
    @Transactional
    public void deleteDocument(Long documentId) {
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedDocument(documentId, currentUser.id());
        attachmentMapper.delete(new LambdaQueryWrapper<Attachment>().eq(Attachment::getDocumentId, documentId));
        documentMapper.deleteById(documentId);
    }

    /**
     * listDocumentVersions 查询当前用户可访问文档的版本历史。
     * 它先校验文档所有权，再读取 document_versions 表，在本项目中为版本面板提供历史快照。
     *
     * @param documentId 来自路径参数的文档主键。
     * @return DocumentVersionResponse 列表，按版本号倒序排列。
     */
    public List<DocumentVersionResponse> listDocumentVersions(Long documentId) {
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedDocument(documentId, currentUser.id());
        return documentVersionMapper.selectList(new LambdaQueryWrapper<DocumentVersion>()
                        .eq(DocumentVersion::getDocumentId, documentId)
                        .orderByDesc(DocumentVersion::getVersionNo))
                .stream()
                .map(this::toDocumentVersionResponse)
                .toList();
    }

    /**
     * rollbackDocumentVersion 把文档恢复到指定历史版本。
     * 它校验版本归属和文档所有权后更新当前文档，并创建新的版本快照记录这次回滚。
     *
     * @param documentId 来自路径参数的文档主键。
     * @param versionId 来自路径参数的版本主键。
     * @return DocumentDetailResponse 回滚后的文档详情。
     */
    @Transactional
    public DocumentDetailResponse rollbackDocumentVersion(Long documentId, Long versionId) {
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // document 来自 documents 表，是即将被历史版本覆盖的当前元数据。
        Document document = findOwnedDocument(documentId, currentUser.id());
        // version 来自 document_versions 表，保存要恢复的历史快照。
        DocumentVersion version = findDocumentVersion(documentId, versionId);
        // content 来自 document_contents 表，是即将被历史版本覆盖的当前 Markdown 正文。
        DocumentContent content = findDocumentContent(documentId);
        // now 使用 Java LocalDateTime 记录回滚操作的更新时间和新版本生成时间。
        LocalDateTime now = LocalDateTime.now();
        document.setTitle(version.getTitle());
        document.setSummary(version.getSummary());
        document.setStatus(version.getStatus());
        document.setUpdatedAt(now);
        documentMapper.updateById(document);
        content.setContent(version.getContent());
        content.setUpdatedAt(now);
        documentContentMapper.updateById(content);
        createVersionSnapshot(document, content, currentUser.id(), now);
        return toDocumentDetailResponse(document, content);
    }

    /**
     * deleteDocumentVersion 删除当前用户可访问文档下的某个版本快照。
     * 它校验文档所有权和版本归属后删除 document_versions 记录，在本项目中让用户清理不再需要的历史快照。
     *
     * @param documentId 来自路径参数的文档主键。
     * @param versionId 来自路径参数的版本主键。
     */
    @Transactional
    public void deleteDocumentVersion(Long documentId, Long versionId) {
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedDocument(documentId, currentUser.id());
        findDocumentVersion(documentId, versionId);
        documentVersionMapper.deleteById(versionId);
    }

    /**
     * listAttachments 查询当前用户可访问文档的附件列表。
     * 它先校验文档所有权，再读取 attachments 表，在本项目中为编辑器附件面板提供数据。
     *
     * @param documentId 来自路径参数的文档主键。
     * @return AttachmentResponse 列表。
     */
    public List<AttachmentResponse> listAttachments(Long documentId) {
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedDocument(documentId, currentUser.id());
        return attachmentMapper.selectList(new LambdaQueryWrapper<Attachment>()
                        .eq(Attachment::getDocumentId, documentId)
                        .orderByDesc(Attachment::getCreatedAt))
                .stream()
                .map(this::toAttachmentResponse)
                .toList();
    }

    /**
     * uploadAttachment 为当前用户可访问的文档上传附件。
     * 它生成安全存储文件名、写入磁盘并保存附件元数据，在本项目中演示文件上传的路径隔离。
     *
     * @param documentId 来自路径参数的文档主键。
     * @param file Spring MVC MultipartFile，来自浏览器 multipart/form-data 上传。
     * @return AttachmentResponse 新上传附件的响应。
     */
    @Transactional
    public AttachmentResponse uploadAttachment(Long documentId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "附件不能为空");
        }
        // currentUser 来自 SecurityUtils.java，用于判断文档所属知识库是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        findOwnedDocument(documentId, currentUser.id());
        // originalFilename 来自 MultipartFile.getOriginalFilename，并通过文件名提取避免路径穿越。
        String originalFilename = sanitizeOriginalFilename(file.getOriginalFilename());
        // storedFilename 是服务端生成的随机文件名，避免用户文件名直接决定磁盘路径。
        String storedFilename = UUID.randomUUID() + getFileExtension(originalFilename);
        // documentDir 是当前文档的附件目录，用 documentId 做隔离，便于后续清理和迁移。
        Path documentDir = attachmentsRoot.resolve("document-" + documentId).normalize();
        // targetPath 是最终写入磁盘的路径，必须仍位于 attachmentsRoot 下。
        Path targetPath = documentDir.resolve(storedFilename).normalize();
        ensurePathInsideRoot(targetPath);
        try {
            Files.createDirectories(documentDir);
            Files.copy(file.getInputStream(), targetPath);
        } catch (IOException ex) {
            throw new IllegalStateException("附件保存失败", ex);
        }
        // now 使用 Java LocalDateTime 记录附件创建和更新时间。
        LocalDateTime now = LocalDateTime.now();
        // attachment 是即将写入 attachments 表的附件元数据。
        Attachment attachment = new Attachment();
        attachment.setDocumentId(documentId);
        attachment.setOriginalFilename(originalFilename);
        attachment.setStoredFilename(storedFilename);
        attachment.setContentType(normalizeContentType(file.getContentType()));
        attachment.setSizeBytes(file.getSize());
        attachment.setStoragePath(attachmentsRoot.relativize(targetPath).toString());
        attachment.setDeleted(0);
        attachment.setCreatedAt(now);
        attachment.setUpdatedAt(now);
        attachmentMapper.insert(attachment);
        return toAttachmentResponse(attachment);
    }

    /**
     * prepareAttachmentDownload 准备附件下载资源。
     * 它校验附件所属文档所有权并定位磁盘文件，在本项目中避免用户下载其他文档的附件。
     *
     * @param attachmentId 来自路径参数的附件主键。
     * @return AttachmentDownload 下载所需资源和响应头元数据。
     */
    public AttachmentDownload prepareAttachmentDownload(Long attachmentId) {
        // currentUser 来自 SecurityUtils.java，用于判断附件所属文档是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // attachment 来自 attachments 表，用于找到文件路径和原始文件名。
        Attachment attachment = findOwnedAttachment(attachmentId, currentUser.id());
        // filePath 由附件根目录和相对路径组合而来，normalize 后继续做根目录校验。
        Path filePath = attachmentsRoot.resolve(attachment.getStoragePath()).normalize();
        ensurePathInsideRoot(filePath);
        if (!Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件文件不存在");
        }
        // resource 使用 Spring FileSystemResource 包装本地文件，在 Controller 中作为响应体返回。
        Resource resource = new FileSystemResource(filePath);
        return new AttachmentDownload(resource, attachment.getOriginalFilename(), attachment.getContentType(), attachment.getSizeBytes());
    }

    /**
     * deleteAttachment 删除当前用户可访问文档下的附件。
     * 它先逻辑删除 attachments 元数据，再尝试删除磁盘文件，在本项目中演示文件和元数据的协同清理。
     *
     * @param attachmentId 来自路径参数的附件主键。
     */
    @Transactional
    public void deleteAttachment(Long attachmentId) {
        // currentUser 来自 SecurityUtils.java，用于判断附件所属文档是否属于当前用户。
        CurrentUser currentUser = SecurityUtils.currentUser();
        // attachment 来自 attachments 表，用于定位要删除的元数据和磁盘文件。
        Attachment attachment = findOwnedAttachment(attachmentId, currentUser.id());
        attachmentMapper.deleteById(attachmentId);
        // filePath 由附件根目录和相对路径组合而来，normalize 后继续做根目录校验。
        Path filePath = attachmentsRoot.resolve(attachment.getStoragePath()).normalize();
        ensurePathInsideRoot(filePath);
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new IllegalStateException("附件文件删除失败", ex);
        }
    }

    /**
     * findOwnedKnowledgeBase 查询并校验知识库所有权。
     * 它使用 ownerId 条件访问 knowledge_bases 表，在本项目中把“资源不存在”和“不是你的资源”都收敛为不可访问。
     *
     * @param knowledgeBaseId 来自路径参数或文档关系的知识库主键。
     * @param ownerId 来自 CurrentUser.java 的当前用户主键。
     * @return KnowledgeBase 当前用户拥有的知识库实体。
     */
    private KnowledgeBase findOwnedKnowledgeBase(Long knowledgeBaseId, Long ownerId) {
        // knowledgeBase 来自 knowledge_bases 表，只有 ownerId 匹配时才会返回。
        KnowledgeBase knowledgeBase = knowledgeBaseMapper.selectOne(new LambdaQueryWrapper<KnowledgeBase>()
                .eq(KnowledgeBase::getId, knowledgeBaseId)
                .eq(KnowledgeBase::getOwnerId, ownerId)
                .last("LIMIT 1"));
        if (knowledgeBase == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "知识库不存在");
        }
        return knowledgeBase;
    }

    /**
     * findOwnedDocument 查询并校验文档所属知识库所有权。
     * 它先读取 documents，再复用 findOwnedKnowledgeBase 校验 ownerId，在本项目中统一保护文档详情、更新和删除。
     *
     * @param documentId 来自路径参数的文档主键。
     * @param ownerId 来自 CurrentUser.java 的当前用户主键。
     * @return Document 当前用户可访问的文档实体。
     */
    private Document findOwnedDocument(Long documentId, Long ownerId) {
        // document 来自 documents 表，用于找到所属知识库并继续做所有权校验。
        Document document = documentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文档不存在");
        }
        findOwnedKnowledgeBase(document.getKnowledgeBaseId(), ownerId);
        return document;
    }

    /**
     * findDocumentContent 查询文档正文。
     * 它使用 DocumentContentMapper.java 按 documentId 查询一对一正文，在本项目中让详情接口合并元数据和正文。
     *
     * @param documentId 来自 Document.java 的文档主键。
     * @return DocumentContent 文档正文实体。
     */
    private DocumentContent findDocumentContent(Long documentId) {
        // content 来自 document_contents 表，是文档详情和编辑器保存所需的 Markdown 正文。
        DocumentContent content = documentContentMapper.selectOne(new LambdaQueryWrapper<DocumentContent>()
                .eq(DocumentContent::getDocumentId, documentId)
                .last("LIMIT 1"));
        if (content == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文档正文不存在");
        }
        return content;
    }

    /**
     * findDocumentVersion 查询并校验版本属于指定文档。
     * 它使用 documentId 和 versionId 双条件访问 document_versions 表，在本项目中避免跨文档回滚。
     *
     * @param documentId 来自路径参数的文档主键。
     * @param versionId 来自路径参数的版本主键。
     * @return DocumentVersion 指定文档下的历史版本实体。
     */
    private DocumentVersion findDocumentVersion(Long documentId, Long versionId) {
        // version 来自 document_versions 表，只有 documentId 和 versionId 同时匹配时才允许回滚。
        DocumentVersion version = documentVersionMapper.selectOne(new LambdaQueryWrapper<DocumentVersion>()
                .eq(DocumentVersion::getId, versionId)
                .eq(DocumentVersion::getDocumentId, documentId)
                .last("LIMIT 1"));
        if (version == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "文档版本不存在");
        }
        return version;
    }

    /**
     * findOwnedAttachment 查询并校验附件所属文档所有权。
     * 它先读取 attachments，再复用 findOwnedDocument 校验 ownerId，在本项目中保护附件下载和删除。
     *
     * @param attachmentId 来自路径参数的附件主键。
     * @param ownerId 来自 CurrentUser.java 的当前用户主键。
     * @return Attachment 当前用户可访问的附件实体。
     */
    private Attachment findOwnedAttachment(Long attachmentId, Long ownerId) {
        // attachment 来自 attachments 表，用于找到所属文档并继续做所有权校验。
        Attachment attachment = attachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "附件不存在");
        }
        findOwnedDocument(attachment.getDocumentId(), ownerId);
        return attachment;
    }

    /**
     * createVersionSnapshot 为当前文档状态创建版本快照。
     * 它读取当前最大版本号并递增写入 document_versions，在本项目中让每次保存都可回溯。
     *
     * @param document 来自 Document.java 的当前文档元数据。
     * @param content 来自 DocumentContent.java 的当前文档正文。
     * @param userId 来自 CurrentUser.java 的当前用户主键。
     * @param createdAt 版本快照生成时间。
     */
    private void createVersionSnapshot(Document document, DocumentContent content, Long userId, LocalDateTime createdAt) {
        // latestVersion 来自 document_versions 表，用于计算下一次保存的递增版本号。
        DocumentVersion latestVersion = documentVersionMapper.selectOne(new LambdaQueryWrapper<DocumentVersion>()
                .eq(DocumentVersion::getDocumentId, document.getId())
                .orderByDesc(DocumentVersion::getVersionNo)
                .last("LIMIT 1"));
        // nextVersionNo 根据最新版本号递增，当前没有历史版本时从 1 开始。
        int nextVersionNo = latestVersion == null ? 1 : latestVersion.getVersionNo() + 1;
        // version 是即将写入 document_versions 表的完整文档快照。
        DocumentVersion version = new DocumentVersion();
        version.setDocumentId(document.getId());
        version.setVersionNo(nextVersionNo);
        version.setTitle(document.getTitle());
        version.setSummary(document.getSummary());
        version.setStatus(document.getStatus());
        version.setContent(content.getContent());
        version.setCreatedBy(userId);
        version.setCreatedAt(createdAt);
        documentVersionMapper.insert(version);
    }

    /**
     * toKnowledgeBaseResponse 把 KnowledgeBase 实体转换为响应 DTO。
     * 它额外统计 documents 表中文档数量，在本项目中让列表页展示知识库规模而不加载文档详情。
     *
     * @param knowledgeBase 来自 KnowledgeBase.java 的数据库实体。
     * @return KnowledgeBaseResponse 前端知识库响应。
     */
    private KnowledgeBaseResponse toKnowledgeBaseResponse(KnowledgeBase knowledgeBase) {
        // documentCount 使用 DocumentMapper.java 统计当前知识库下未删除文档数量，用于前端知识库卡片展示。
        Long documentCount = documentMapper.selectCount(new LambdaQueryWrapper<Document>()
                .eq(Document::getKnowledgeBaseId, knowledgeBase.getId()));
        return new KnowledgeBaseResponse(
                knowledgeBase.getId(),
                knowledgeBase.getName(),
                knowledgeBase.getDescription(),
                knowledgeBase.getVisibility(),
                documentCount,
                knowledgeBase.getCreatedAt(),
                knowledgeBase.getUpdatedAt()
        );
    }

    /**
     * toDocumentSummaryResponse 把 Document 实体转换为列表响应 DTO。
     * 它不读取 DocumentContent.java，在本项目中保持文档列表接口轻量。
     *
     * @param document 来自 Document.java 的数据库实体。
     * @return DocumentSummaryResponse 文档列表项。
     */
    private DocumentSummaryResponse toDocumentSummaryResponse(Document document) {
        return new DocumentSummaryResponse(
                document.getId(),
                document.getKnowledgeBaseId(),
                document.getTitle(),
                document.getSummary(),
                document.getStatus(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    /**
     * toDocumentDetailResponse 合并文档元数据和正文为详情响应。
     * 它把 Document.java 和 DocumentContent.java 两个实体组合起来，在本项目中服务 Markdown 编辑器。
     *
     * @param document 来自 Document.java 的文档元数据实体。
     * @param content 来自 DocumentContent.java 的文档正文实体。
     * @return DocumentDetailResponse 文档完整详情。
     */
    private DocumentDetailResponse toDocumentDetailResponse(Document document, DocumentContent content) {
        return new DocumentDetailResponse(
                document.getId(),
                document.getKnowledgeBaseId(),
                document.getTitle(),
                document.getSummary(),
                document.getStatus(),
                content.getContent(),
                document.getCreatedAt(),
                document.getUpdatedAt()
        );
    }

    /**
     * toDocumentVersionResponse 把 DocumentVersion 实体转换为版本响应 DTO。
     * 它保留历史标题、摘要、状态和正文，在本项目中让前端能展示并回滚版本。
     *
     * @param version 来自 DocumentVersion.java 的数据库实体。
     * @return DocumentVersionResponse 文档版本响应。
     */
    private DocumentVersionResponse toDocumentVersionResponse(DocumentVersion version) {
        return new DocumentVersionResponse(
                version.getId(),
                version.getDocumentId(),
                version.getVersionNo(),
                version.getTitle(),
                version.getSummary(),
                version.getStatus(),
                version.getContent(),
                version.getCreatedBy(),
                version.getCreatedAt()
        );
    }

    /**
     * toAttachmentResponse 把 Attachment 实体转换为附件响应 DTO。
     * 它隐藏服务端存储路径，只返回下载 URL 和展示字段，在本项目中避免泄漏磁盘结构。
     *
     * @param attachment 来自 Attachment.java 的数据库实体。
     * @return AttachmentResponse 附件响应。
     */
    private AttachmentResponse toAttachmentResponse(Attachment attachment) {
        return new AttachmentResponse(
                attachment.getId(),
                attachment.getDocumentId(),
                attachment.getOriginalFilename(),
                attachment.getContentType(),
                attachment.getSizeBytes(),
                "/api/attachments/" + attachment.getId() + "/download",
                attachment.getCreatedAt()
        );
    }

    /**
     * normalizeText 规范化可选文本字段。
     * 它使用 Java String 的 trim 方法处理前端输入，在本项目中把空字符串统一存为 null。
     *
     * @param value 前端请求 DTO 中的可选文本字段。
     * @return 去除首尾空格后的文本；空白时返回 null。
     */
    private String normalizeText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    /**
     * sanitizeOriginalFilename 清理浏览器上传的原始文件名。
     * 它使用 Java Path API 只保留文件名部分，在本项目中防止用户提交带目录的路径。
     *
     * @param originalFilename MultipartFile 提供的原始文件名。
     * @return 安全的展示文件名。
     */
    private String sanitizeOriginalFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "attachment";
        }
        // filename 使用 Paths.get(...).getFileName() 只取最后一段，去掉可能的客户端目录。
        String filename = Paths.get(originalFilename).getFileName().toString();
        if (filename.isBlank()) {
            return "attachment";
        }
        return filename;
    }

    /**
     * getFileExtension 提取原始文件扩展名。
     * 它使用 Java String 方法截取最后一个点之后的内容，在本项目中让随机存储文件名仍保留常见扩展名。
     *
     * @param filename 清理后的原始文件名。
     * @return 包含点号的扩展名；没有扩展名时返回空字符串。
     */
    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex);
    }

    /**
     * normalizeContentType 规范化附件 MIME 类型。
     * 它使用浏览器上传的 contentType 或默认二进制类型，在本项目中为下载响应设置 Content-Type。
     *
     * @param contentType MultipartFile 提供的 MIME 类型。
     * @return 非空 MIME 类型。
     */
    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return "application/octet-stream";
        }
        return contentType;
    }

    /**
     * ensurePathInsideRoot 校验文件路径仍在附件根目录内。
     * 它使用 Java Path.normalize 和 startsWith 防止路径穿越，在本项目中保护本地文件系统。
     *
     * @param path 即将读写的文件路径。
     */
    private void ensurePathInsideRoot(Path path) {
        if (!path.normalize().startsWith(attachmentsRoot)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "非法文件路径");
        }
    }

    /**
     * encodeDownloadFilename 编码下载文件名。
     * 它使用 Java URLEncoder 按 UTF-8 编码，在本项目中让中文附件名可以出现在 Content-Disposition。
     *
     * @param filename 附件原始文件名。
     * @return UTF-8 编码后的文件名。
     */
    public String encodeDownloadFilename(String filename) {
        return URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /**
     * AttachmentDownload 是附件下载接口的内部数据载体。
     * 它把 Spring Resource 和响应头所需元数据组合起来，在本项目中让 Controller 不直接访问磁盘路径。
     *
     * @param resource Spring FileSystemResource，包装本地附件文件。
     * @param originalFilename 用户上传时的原始文件名。
     * @param contentType 附件 MIME 类型。
     * @param sizeBytes 附件大小。
     */
    public record AttachmentDownload(Resource resource, String originalFilename, String contentType, Long sizeBytes) {
    }
}
