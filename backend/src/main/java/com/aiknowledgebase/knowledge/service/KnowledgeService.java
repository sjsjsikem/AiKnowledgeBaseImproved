package com.aiknowledgebase.knowledge.service;

import com.aiknowledgebase.common.BusinessException;
import com.aiknowledgebase.common.ErrorCode;
import com.aiknowledgebase.knowledge.dto.CreateDocumentRequest;
import com.aiknowledgebase.knowledge.dto.CreateKnowledgeBaseRequest;
import com.aiknowledgebase.knowledge.dto.DocumentDetailResponse;
import com.aiknowledgebase.knowledge.dto.DocumentSummaryResponse;
import com.aiknowledgebase.knowledge.dto.KnowledgeBaseResponse;
import com.aiknowledgebase.knowledge.dto.UpdateDocumentRequest;
import com.aiknowledgebase.knowledge.dto.UpdateKnowledgeBaseRequest;
import com.aiknowledgebase.knowledge.entity.Document;
import com.aiknowledgebase.knowledge.entity.DocumentContent;
import com.aiknowledgebase.knowledge.entity.KnowledgeBase;
import com.aiknowledgebase.knowledge.mapper.DocumentContentMapper;
import com.aiknowledgebase.knowledge.mapper.DocumentMapper;
import com.aiknowledgebase.knowledge.mapper.KnowledgeBaseMapper;
import com.aiknowledgebase.security.CurrentUser;
import com.aiknowledgebase.security.SecurityUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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

    /**
     * 构造方法由 Spring 注入知识库模块需要的 Mapper。
     * 它把知识库、文档元数据和文档正文的数据访问能力组合到一个业务服务中。
     *
     * @param knowledgeBaseMapper 来自 KnowledgeBaseMapper.java，用于 knowledge_bases 表 CRUD。
     * @param documentMapper 来自 DocumentMapper.java，用于 documents 表 CRUD。
     * @param documentContentMapper 来自 DocumentContentMapper.java，用于 document_contents 表 CRUD。
     */
    public KnowledgeService(
            KnowledgeBaseMapper knowledgeBaseMapper,
            DocumentMapper documentMapper,
            DocumentContentMapper documentContentMapper
    ) {
        this.knowledgeBaseMapper = knowledgeBaseMapper;
        this.documentMapper = documentMapper;
        this.documentContentMapper = documentContentMapper;
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
        documentMapper.deleteById(documentId);
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
}
