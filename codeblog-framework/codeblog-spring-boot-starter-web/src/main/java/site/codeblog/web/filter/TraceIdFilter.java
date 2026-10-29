package site.codeblog.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 链路追踪 MDC 过滤器（S1，服务侧最前）
 * <p>
 * 读取网关注入的 X-Trace-Id（无则自生成，兼容服务直连场景）写入 MDC
 * （日志 pattern %X{traceId} 输出）并回写响应头；请求结束 finally 清理，
 * 防止线程池复用串号。上游生成方为网关 TraceIdFilter（G1）。
 *
 * @author codeblog
 */
public class TraceIdFilter extends OncePerRequestFilter {

    /**
     * 链路追踪请求/响应头（与网关、rpc starter 保持一致）
     */
    public static final String TRACE_HEADER = "X-Trace-Id";

    /**
     * MDC key（logback pattern 中 %X{traceId}）
     */
    public static final String MDC_TRACE_ID = "traceId";

    /**
     * 入站 traceId 只信规范格式（防日志注入/污染），否则自生成（兼容服务直连无头场景）
     */
    private static final Pattern TRACE_ID_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{8,64}$");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_HEADER);
        if (traceId == null || !TRACE_ID_PATTERN.matcher(traceId).matches()) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }
        MDC.put(MDC_TRACE_ID, traceId);
        response.setHeader(TRACE_HEADER, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(MDC_TRACE_ID);
        }
    }

}
