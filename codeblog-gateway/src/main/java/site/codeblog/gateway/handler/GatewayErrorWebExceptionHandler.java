package site.codeblog.gateway.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.Result;

import java.nio.charset.StandardCharsets;

/**
 * 网关统一异常处理（G5）：非 Sa-Token 异常也输出 Result JSON
 * <p>
 * SaReactorFilter 的 setError 已覆盖鉴权异常；本处理器接管其余网关异常
 * （路由不存在、下游不可达等），避免 Spring 默认错误体格式不一致。
 *
 * @author codeblog
 */
@Slf4j
@RequiredArgsConstructor
public class GatewayErrorWebExceptionHandler implements ErrorWebExceptionHandler {

    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        if (exchange.getResponse().isCommitted()) {
            return Mono.error(ex);
        }
        // 路由不存在（404）保持 404 语义，其余按 500 处理；均输出 Result JSON
        boolean notFound = ex instanceof NoResourceFoundException
                || (ex instanceof ResponseStatusException rse && rse.getStatusCode().value() == 404);
        ServerHttpResponse response = exchange.getResponse();
        response.setRawStatusCode(notFound ? 404 : 500);
        response.getHeaders().setContentType(
                org.springframework.http.MediaType.parseMediaType("application/json;charset=UTF-8"));

        log.error("网关异常：path={}, type={}, message={}",
                exchange.getRequest().getPath(), ex.getClass().getSimpleName(), ex.getMessage());

        byte[] body;
        try {
            Result<Void> result = Result.fail(notFound ? GlobalErrorCode.NOT_FOUND : GlobalErrorCode.INTERNAL_SERVER_ERROR);
            body = objectMapper.writeValueAsBytes(result);
        } catch (JsonProcessingException e) {
            body = "{\"code\":500,\"message\":\"系统内部错误\"}".getBytes(StandardCharsets.UTF_8);
        }
        DataBuffer buffer = response.bufferFactory().wrap(body);
        return response.writeWith(Mono.just(buffer));
    }

    /**
     * 以 @Order(-2) 注册，先于 Boot 默认 DefaultErrorWebExceptionHandler 生效
     */
    @Configuration
    static class GatewayErrorConfiguration {

        @Bean
        @Order(-2)
        public ErrorWebExceptionHandler gatewayErrorWebExceptionHandler(ObjectMapper objectMapper) {
            return new GatewayErrorWebExceptionHandler(objectMapper);
        }
    }
}
