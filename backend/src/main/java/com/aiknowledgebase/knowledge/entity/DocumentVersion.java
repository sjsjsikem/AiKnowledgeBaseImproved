package com.aiknowledgebase.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DocumentVersion 是 document_versions 表对应的 MyBatis-Plus 实体。
 * 它保存文档某次保存后的完整快照，在本项目中支撑版本历史查看和版本回滚。
 */
@Getter
@Setter
@TableName("document_versions")
public class DocumentVersion {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 document_versions 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // documentId 来自 Document.java 的主键，用于把版本快照归属到某一篇文档。
    private Long documentId;

    // versionNo 是同一文档下递增的版本号，用于前端按保存顺序展示历史版本。
    private Integer versionNo;

    // title 保存该版本的文档标题快照，用于回滚时恢复 documents.title。
    private String title;

    // summary 保存该版本的文档摘要快照，用于回滚时恢复 documents.summary。
    private String summary;

    // status 保存该版本的文档状态快照，用于回滚时恢复 documents.status。
    private String status;

    // content 保存该版本的 Markdown 正文快照，用于回滚时恢复 document_contents.content。
    private String content;

    // createdBy 来自 CurrentUser.java 的当前用户 ID，用于记录是谁生成了这个版本。
    private Long createdBy;

    // createdAt 记录版本生成时间，用于前端版本历史排序和审计。
    private LocalDateTime createdAt;
}
