package com.aiknowledgebase.rbac.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * UpdateUserStatusRequest 是用户启停接口的请求 DTO。
 * 它接收 ENABLED 或 DISABLED，在本项目中让管理员控制账号是否允许继续登录和访问接口。
 *
 * @param status 前端传入的账号状态，当前支持 ENABLED、DISABLED。
 */
public record UpdateUserStatusRequest(@NotBlank String status) {
}
