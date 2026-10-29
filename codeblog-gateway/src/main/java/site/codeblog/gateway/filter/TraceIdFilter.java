package site.codeblog.gateway.filter;

import cn.hutool.core.util.StrUtil;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * 链路追踪过滤器（G1）：入口生成/透传 X-Trace-Id
 * <p>
 * 必须为 WebFilter 且置于 SaReactorFilter 之前：Sa 鉴权失败在 WebFilter 层直接短路返回，
 * 若做成 GlobalFilter（执行在其后）则 401/403 响应丢失链路头。生成后注入转发请求头
 * （下游 S1 TraceMdcFilter 读取写 MDC）并回写响应头（前端报障可对号入座）；
 * Rpc starter 的 Feign 拦截器负责服务间继续透传。网关自身为 WebFlux 模型，
 * MDC 不跨线程可靠传播，traceId 不进网关日志 pattern（架构文档 8.1/12.4 L1）。
 *
 * @author codeblog
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class TraceIdFilter implements WebFilter {

    /**
     * 链路追踪请求/响应头
     */
    public static final String TRACE_HEADER = "X-Trace-Id";

    /**
     * exchange 属性 key（供后续组件取当前链路 ID）
     */
    public static final String ATTR_TRACE_ID = "codeblog.traceId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String traceId = exchange.getRequest().getHeaders().getFirst(TRACE_HEADER);
        if (StrUtil.isBlank(traceId)) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        ServerHttpRequest mutated = exchange.getRequest().mutate()
                .header(TRACE_HEADER, traceId)
                .build();
        ServerWebExchange mutatedExchange = exchange.mutate().request(mutated).build();
        mutatedExchange.getAttributes().put(ATTR_TRACE_ID, traceId);

        exchange.getResponse().getHeaders().set(TRACE_HEADER, traceId);
        return chain.filter(mutatedExchange);
    }

}
