package com.aiknowledgebase.common;

import java.util.List;

/**
 * PageResponse 是分页接口的统一返回模型。
 * 它使用 Java record 保存列表、总数和分页参数，在本项目后续知识库、文档、用户管理列表中复用。
 *
 * @param items 当前页数据列表，来自具体业务查询结果。
 * @param total 数据总数，来自数据库 count 查询。
 * @param page 当前页码，由前端分页参数传入。
 * @param pageSize 每页数量，由前端分页参数传入。
 */
public record PageResponse<T>(List<T> items, long total, long page, long pageSize) {

    /**
     * of 创建分页响应对象。
     * 它通过 Java 泛型保留列表元素类型，在本项目中让各业务模块统一返回分页数据。
     *
     * @param items 当前页业务数据列表。
     * @param total 数据总数。
     * @param page 当前页码。
     * @param pageSize 每页数量。
     * @return PageResponse 包装后的分页结果。
     */
    public static <T> PageResponse<T> of(List<T> items, long total, long page, long pageSize) {
        return new PageResponse<>(items, total, page, pageSize);
    }
}
