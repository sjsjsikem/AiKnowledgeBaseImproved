package com.aiknowledgebase.security;

import com.aiknowledgebase.cache.RedisCacheService;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;

/**
 * TokenBlacklistService 负责退出登录后的 JWT 黑名单。
 * 它使用 Redis 保存 Token 哈希和剩余有效期，在本项目中让服务端可以拒绝已经退出登录的旧 Bearer Token。
 */
@Service
public class TokenBlacklistService {

    // BLACKLIST_KEY_PREFIX 是 Redis 黑名单 key 前缀，用于和业务缓存 key 做命名空间隔离。
    private static final String BLACKLIST_KEY_PREFIX = "auth:blacklist:";
    // BLACKLIST_VALUE 是 Redis 黑名单 value，实际语义只依赖 key 是否存在。
    private static final String BLACKLIST_VALUE = "true";

    // redisCacheService 来自 RedisCacheService.java，用于写入和查询 Redis 黑名单 key。
    private final RedisCacheService redisCacheService;

    /**
     * 构造方法由 Spring 注入 Redis 缓存封装。
     * 它让认证模块复用统一 Redis 降级策略，而不是直接操作 StringRedisTemplate。
     *
     * @param redisCacheService 来自 RedisCacheService.java，用于读写 Redis。
     */
    public TokenBlacklistService(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    /**
     * blacklist 把 Token 写入 Redis 黑名单。
     * 它使用 SHA-256 哈希避免保存原始 JWT，并用 JWT 剩余有效期作为 TTL，在本项目中完成服务端退出登录。
     *
     * @param token 来自 Authorization Bearer 头的 JWT 字符串。
     * @param ttl 来自 JwtService.java 计算的 Token 剩余有效期。
     */
    public void blacklist(String token, Duration ttl) {
        if (ttl.isZero() || ttl.isNegative()) {
            return;
        }
        redisCacheService.putString(toKey(token), BLACKLIST_VALUE, ttl);
    }

    /**
     * isBlacklisted 判断 Token 是否已经退出登录。
     * 它把原始 Token 转换为同样的 SHA-256 key 后查询 Redis，在本项目中供 JWT 过滤器阻断旧 Token。
     *
     * @param token 来自 Authorization Bearer 头的 JWT 字符串。
     * @return true 表示 Token 已在黑名单中。
     */
    public boolean isBlacklisted(String token) {
        return redisCacheService.hasKey(toKey(token));
    }

    /**
     * toKey 把原始 Token 转换为 Redis key。
     * 它通过 hashToken 隐藏原始 JWT 内容，在本项目中减少 Redis 泄漏时的安全风险。
     *
     * @param token 来自 Authorization Bearer 头的 JWT 字符串。
     * @return Redis 黑名单 key。
     */
    private String toKey(String token) {
        return BLACKLIST_KEY_PREFIX + hashToken(token);
    }

    /**
     * hashToken 计算 JWT 的 SHA-256 十六进制摘要。
     * 它使用 Java 标准库 MessageDigest，在本项目中避免把完整 Token 存进 Redis。
     *
     * @param token 原始 JWT 字符串。
     * @return SHA-256 十六进制字符串。
     */
    private String hashToken(String token) {
        try {
            // digest 是 Java MessageDigest 对 Token 字节计算出的哈希结果。
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", ex);
        }
    }
}
