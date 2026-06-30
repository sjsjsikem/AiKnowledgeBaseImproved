package com.aiknowledgebase.knowledge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Attachment 是 attachments 表对应的 MyBatis-Plus 实体。
 * 它保存上传文件的元数据和本地存储路径，在本项目中把文件内容和文档业务关系分开管理。
 */
@Getter
@Setter
@TableName("attachments")
public class Attachment {

    // id 使用 MyBatis-Plus 自带 @TableId 和数据库自增策略，作为 attachments 表主键。
    @TableId(type = IdType.AUTO)
    private Long id;

    // documentId 来自 Document.java 的主键，用于把附件归属到某一篇文档。
    private Long documentId;

    // originalFilename 保存用户上传时的原始文件名，用于前端展示和下载文件名。
    private String originalFilename;

    // storedFilename 保存服务端生成的安全文件名，用于避免用户文件名直接参与磁盘路径。
    private String storedFilename;

    // contentType 保存浏览器上传的 MIME 类型，用于下载时设置响应头。
    private String contentType;

    // sizeBytes 保存文件大小，前端可用于展示附件体积。
    private Long sizeBytes;

    // storagePath 保存相对存储路径，用于从配置的附件根目录定位文件。
    private String storagePath;

    // deleted 使用 MyBatis-Plus 自带逻辑删除注解，删除附件时先隐藏元数据。
    @TableLogic
    private Integer deleted;

    // createdAt 记录附件上传时间，用于前端附件列表排序。
    private LocalDateTime createdAt;

    // updatedAt 记录附件元数据更新时间，用于后续附件安全扫描或重命名扩展。
    private LocalDateTime updatedAt;
}
