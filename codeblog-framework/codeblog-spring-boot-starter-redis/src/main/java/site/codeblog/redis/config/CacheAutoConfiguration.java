package site.codeblog.redis.config;

import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import site.codeblog.redis.lock.LockTemplate;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache 自动配置（阶段5）
 * <p>
 * RedisCacheManager 复用 {@code codeblogRedisTemplate} 同款序列化（String key +
 * JSON 带类型 value），缓存名即 key 前缀（单冒号拼接，对齐全局约定
 * {@code codeblog:{service}:{domain}:{bizId}}）；TTL 全局默认 + 按缓存名覆盖
 * （{@code codeblog.cache.specs}），所有缓存强制带过期。
 * 选型说明：MyBatis-Flex 官方同样推荐 Spring Cache 路线（其 CacheableServiceImpl
 * 模式需迁移 IService 基类体系，本项目服务层为手写 Service，不采纳，见架构文档回填）。
 *
 * @author codeblog
 */
@AutoConfiguration
@ConditionalOnClass({RedisOperations.class, CacheManager.class})
@EnableCaching
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager codeblogCacheManager(RedisConnectionFactory connectionFactory,
                                             CacheProperties properties) {
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(name -> name + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(jsonSerializer()))
                .entryTtl(properties.getDefaultTtl());

        Map<String, RedisCacheConfiguration> perCache = new HashMap<>();
        properties.getSpecs().forEach((cacheName, ttl) ->
                perCache.put(cacheName, baseConfig.entryTtl(ttl)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(baseConfig)
                .withInitialCacheConfigurations(perCache)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean(LockTemplate.class)
    public LockTemplate lockTemplate(RedissonClient redissonClient) {
        return new LockTemplate(redissonClient);
    }

    /**
     * 与 codeblogRedisTemplate 一致的 JSON 序列化（带类型信息；jsr310 支持
     * LocalDateTime——缓存值含时间字段的 VO 必需，否则写缓存报
     * "Java 8 date/time type not supported"）
     */
    private RedisSerializer<Object> jsonSerializer() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = buildJsonObjectMapper();
        return new org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer(objectMapper);
    }

    /** JSON ObjectMapper：带类型 + jsr310，RedisTemplate 与 CacheManager 共用 */
    static com.fasterxml.jackson.databind.ObjectMapper buildJsonObjectMapper() {
        com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        objectMapper.setVisibility(com.fasterxml.jackson.annotation.PropertyAccessor.ALL,
                com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(
                com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance,
                com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY);
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }

}
