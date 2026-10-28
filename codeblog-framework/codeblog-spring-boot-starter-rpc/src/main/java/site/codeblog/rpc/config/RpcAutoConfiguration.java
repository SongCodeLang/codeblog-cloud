package site.codeblog.rpc.config;

import cn.hutool.core.util.IdUtil;
import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

/**
 * OpenFeign RPC 自动配置
 * <p>
 * 功能：
 * 1. 请求拦截器：服务间调用透传认证头与链路追踪 ID；
 * 2. Feign 日志级别：通过 {@code codeblog.rpc.feign-log-level} 配置（NONE/BASIC/HEADERS/FULL）。
 *
 * @author codeblog
 */
@AutoConfiguration
@ConditionalOnClass(RequestInterceptor.class)
public class RpcAutoConfiguration {

    /**
     * 需要透传的认证头
     */
    private static final List<String> PROPAGATE_HEADERS = List.of("Authorization", "satoken", "Sa-Token");

    /**
     * 链路追踪头
     */
    private static final String TRACE_HEADER = "X-Trace-Id";

    /**
     * 服务间调用请求拦截器：透传认证头与链路追踪 ID
     */
    @Bean
    public RequestInterceptor codeblogFeignRequestInterceptor() {
        return template -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                PROPAGATE_HEADERS.forEach(header -> {
                    String value = request.getHeader(header);
                    if (value != null && !value.isEmpty()) {
                        template.header(header, value);
                    }
                });
                String traceId = request.getHeader(TRACE_HEADER);
                if (traceId == null || traceId.isEmpty()) {
                    traceId = IdUtil.fastSimpleUUID();
                }
                template.header(TRACE_HEADER, traceId);
            }
        };
    }

    /**
     * Feign 日志级别（默认 BASIC，仅 dev 排障时建议 FULL）
     */
    @Bean
    @ConditionalOnProperty(prefix = "codeblog.rpc", name = "feign-log-level", havingValue = "BASIC")
    public Logger.Level feignLoggerLevelBasic() {
        return Logger.Level.BASIC;
    }

    @Bean
    @ConditionalOnProperty(prefix = "codeblog.rpc", name = "feign-log-level", havingValue = "HEADERS")
    public Logger.Level feignLoggerLevelHeaders() {
        return Logger.Level.HEADERS;
    }

    @Bean
    @ConditionalOnProperty(prefix = "codeblog.rpc", name = "feign-log-level", havingValue = "FULL")
    public Logger.Level feignLoggerLevelFull() {
        return Logger.Level.FULL;
    }

}
