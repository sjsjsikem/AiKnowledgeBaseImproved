package com.aiknowledgebase.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * KnowledgeBase 是 knowledge_bases 表对应的 MyBatis-Plus 实体。
 * 它表示用户创建的知识库容器，在本项目中作为文档、附件、AI 和 RAG 能力的业务归属边界。
 */
@Getter
@Setter
@TableName("knowledge_bases")
public class KnowledgeBase {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 knowledge_bases 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // ownerId 来自 users 表主键，用于判断当前登录用户是否拥有这个知识库。
    private Long ownerId;

    // name 是知识库名称，由前端知识库表单提交，在列表和文档编辑页展示。
    private String name;

    // description 是知识库说明，用于帮助用户区分不同知识库的业务范围。
    private String description;

    // visibility 是知识库可见性，Stage 3 先使用 PRIVATE，为后续协作共享预留字段。
    private String visibility;

    // deleted 使用 MyBatis-Plus 自带逻辑删除注解，避免误删知识库时物理删除文档关系。
    @TableLogic
    private Integer deleted;

    // createdAt 记录知识库创建时间，用于列表排序和后续审计。
    private LocalDateTime createdAt;

    // updatedAt 记录知识库更新时间，用于前端展示最近修改状态。
    private LocalDateTime updatedAt;
}
