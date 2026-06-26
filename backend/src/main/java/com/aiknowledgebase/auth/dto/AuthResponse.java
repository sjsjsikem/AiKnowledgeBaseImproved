package com.aiknowledgebase.auth.dto;

/**
 * AuthResponse 是认证接口返回给前端的登录结果 DTO。
 * 它把 JwtService 生成的 accessToken 和 UserProfile 用户资料组合起来，在本项目中支撑前端保存登录态。
 *
 * @param accessToken 来自 JwtService.java 生成的 JWT 字符串，用于后续请求的 Authorization Bearer 头。
 * @param user 来自 UserProfile.java 的安全用户资料，用于前端展示和权限判断。
 */
public record AuthResponse(String accessToken, UserProfile user) {
}
