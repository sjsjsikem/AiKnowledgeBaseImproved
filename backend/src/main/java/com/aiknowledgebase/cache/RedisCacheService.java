package com.aiknowledgebase.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Optional;

/**
 * RedisCacheService 是 Stage 5 的 Redis 缓存封装。
 * 它集中处理 JSON 序列化、TTL 和 Redis 异常降级，在本项目中避免业务层直接散落 StringRedisTemplate 调用。
 */
@Service
public class RedisCacheService {

    // log 是 SLF4J 提供的日志对象，用于记录 Redis 失败但不打断主业务流程的降级信息。
    private static final Logger log = LoggerFactory.getLogger(RedisCacheService.class);

    // redisTemplate 是 Spring Data Redis 提供的字符串操作模板，用于读写 Redis key/value。
    private final StringRedisTemplate redisTemplate;
    // objectMapper 是 Jackson 提供的 JSON 工具，用于把业务 DTO 序列化到 Redis。
    private final ObjectMapper objectMapper;

    /**
     * 构造方法由 Spring 注入 Redis 和 JSON 能力。
     * 它把 StringRedisTemplate 与 ObjectMapper 组合起来，在本项目中形成可复用的缓存基础设施。
     *
     * @param redisTemplate Spring Data Redis 提供的 StringRedisTemplate，用于访问 Redis。
     * @param objectMapper Spring Boot 配置好的 Jackson ObjectMapper，用于 JSON 序列化和反序列化。
     */
    public RedisCacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * get 从 Redis 读取 JSON 并反序列化为指定类型。
     * 它使用 Jackson TypeReference 保留泛型信息，在本项目中支撑列表缓存和详情缓存读取。
     *
     * @param key Redis 缓存键，由业务服务按模块约定生成。
     * @param typeReference Jackson TypeReference，用于告诉 ObjectMapper 目标类型。
     * @return Optional 包装的缓存值；未命中、解析失败或 Redis 不可用时返回空。
     */
    public <T> Optional<T> get(String key, TypeReference<T> typeReference) {
        try {
            // json 来自 Redis 字符串值，是业务 DTO 的 JSON 表示。
            String json = redisTemplate.opsForValue().get(key);
            if (!StringUtils.hasText(json)) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(json, typeReference));
        } catch (JsonProcessingException ex) {
            log.warn("Redis cache json parse failed key={}", key, ex);
            delete(key);
            return Optional.empty();
        } catch (RuntimeException ex) {
            log.warn("Redis cache read failed key={}", key, ex);
            return Optional.empty();
        }
    }

    /**
     * put 把业务对象序列化为 JSON 后写入 Redis。
     * 它通过 TTL 控制缓存生命周期，在本项目中实现可自动过期的 Cache Aside 回填。
     *
     * @param key Redis 缓存键，由业务服务按模块约定生成。
     * @param value 需要缓存的业务 DTO 或列表。
     * @param ttl Java Duration，表示缓存存活时间。
     */
    public void put(String key, Object value, Duration ttl) {
        try {
            // json 使用 Jackson 从业务对象转换而来，Redis 只保存字符串。
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException ex) {
            log.warn("Redis cache json write failed key={}", key, ex);
        } catch (RuntimeException ex) {
            log.warn("Redis cache write failed key={}", key, ex);
        }
    }

    /**
     * putString 把普通字符串写入 Redis。
     * 它用于 Token 黑名单等不需要 JSON 的短值缓存，在本项目中复用统一 Redis 降级策略。
     *
     * @param key Redis 缓存键。
     * @param value 要保存的字符串值。
     * @param ttl Java Duration，表示缓存存活时间。
     */
    public void putString(String key, String value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (RuntimeException ex) {
            log.warn("Redis string write failed key={}", key, ex);
        }
    }

    /**
     * hasKey 判断 Redis 中是否存在指定 key。
     * 它调用 Spring Data Redis 的 hasKey，在本项目中用于认证过滤器判断 Token 是否已退出登录。
     *
     * @param key Redis 缓存键。
     * @return true 表示 key 存在；Redis 不可用时返回 false，让主认证链路按 JWT 自身规则继续。
     */
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (RuntimeException ex) {
            log.warn("Redis hasKey failed key={}", key, ex);
            return false;
        }
    }

    /**
     * delete 删除指定 Redis key。
     * 它被写操作后的缓存失效逻辑调用，在本项目中保证用户看到最新知识库和文档数据。
     *
     * @param key Redis 缓存键。
     */
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (RuntimeException ex) {
            log.warn("Redis cache delete failed key={}", key, ex);
        }
    }
}
