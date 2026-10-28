package site.codeblog.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 网关配置属性
 *
 * @author codeblog
 */
@Data
@ConfigurationProperties(prefix = "codeblog.gateway")
public class GatewayProperties {

    /**
     * 鉴权配置
     */
    private Security security = new Security();

    /**
     * 跨域配置
     */
    private Cors cors = new Cors();

    @Data
    public static class Security {

        /**
         * 是否开启网关统一鉴权
         */
        private Boolean enabled = Boolean.TRUE;

        /**
         * 白名单路径（Ant 风格，匹配网关收到的原始路径）
         */
        private List<String> excludes = new ArrayList<>();

    }

    @Data
    public static class Cors {

        /**
         * 允许的来源（Origin），支持通配符模式
         */
        private List<String> allowedOrigins = new ArrayList<>(List.of("*"));

        /**
         * 是否允许携带 Cookie
         */
        private Boolean allowCredentials = Boolean.TRUE;

        /**
         * 预检结果缓存时间（秒）
         */
        private Long maxAge = 3600L;

    }

}
