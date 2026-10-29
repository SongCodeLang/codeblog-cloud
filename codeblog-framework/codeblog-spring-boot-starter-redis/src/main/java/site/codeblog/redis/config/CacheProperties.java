package site.codeblog.redis.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Spring Cache 缓存配置属性（阶段5）
 *
 * @author codeblog
 */
@Data
@ConfigurationProperties(prefix = "codeblog.cache")
public class CacheProperties {

    /**
     * 默认 TTL（所有缓存必须有兜底过期，防陈旧）
     */
    private Duration defaultTtl = Duration.ofMinutes(30);

    /**
     * 按缓存名覆盖 TTL：缓存名建议 codeblog:{service}:{domain} 格式，
     * 如 codeblog:system:user → PT30M
     */
    private Map<String, Duration> specs = new HashMap<>();

}
