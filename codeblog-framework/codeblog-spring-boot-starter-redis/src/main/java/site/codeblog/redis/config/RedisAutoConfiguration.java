package site.codeblog.redis.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import site.codeblog.redis.core.RedisCache;

/**
 * Redis 自动配置
 * <p>
 * 提供 key 为 String、value 为 JSON（带类型信息）的 {@link RedisTemplate}（bean 名
 * {@code codeblogRedisTemplate}），以及面向业务的 {@link RedisCache} 操作封装。
 * <p>
 * 注意：不使用默认 bean 名 {@code redisTemplate}，避免与 Spring Boot / Redisson
 * 自动配置注册的 {@code RedisTemplate<Object, Object>} 冲突。
 *
 * @author codeblog
 */
@AutoConfiguration
@ConditionalOnClass(RedisOperations.class)
public class RedisAutoConfiguration {

    /**
     * 业务 RedisTemplate：String key + JSON value
     */
    @Bean(name = "codeblogRedisTemplate")
    @ConditionalOnMissingBean(name = "codeblogRedisTemplate")
    public RedisTemplate<String, Object> codeblogRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 序列化时携带类型信息，反序列化可还原具体类型
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        RedisSerializer<Object> jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        StringRedisSerializer stringSerializer = StringRedisSerializer.UTF_8;
        // key 与 hashKey 均使用 String 序列化，便于运维排查
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        // value 与 hashValue 使用 JSON 序列化
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        template.afterPropertiesSet();
        return template;
    }

    /**
     * Redis 操作封装
     */
    @Bean
    @ConditionalOnMissingBean(RedisCache.class)
    public RedisCache redisCache(RedisTemplate<String, Object> codeblogRedisTemplate) {
        return new RedisCache(codeblogRedisTemplate);
    }

}
