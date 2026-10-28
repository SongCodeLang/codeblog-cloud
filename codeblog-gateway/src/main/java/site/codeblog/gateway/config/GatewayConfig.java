package site.codeblog.gateway.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.reactor.filter.SaReactorFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.Result;

import java.util.Collections;

/**
 * 网关配置：统一鉴权（Sa-Token Reactor）与跨域处理
 *
 * @author codeblog
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(GatewayProperties.class)
public class GatewayConfig {

    private final ObjectMapper objectMapper;

    /**
     * 网关统一鉴权过滤器
     * <p>
     * 白名单放行，其余路径校验登录；未登录返回统一 JSON。
     * token 校验基于 Redis 共享会话，业务服务无需重复校验登录态。
     */
    @Bean
    public SaReactorFilter saReactorFilter(GatewayProperties properties) {
        return new SaReactorFilter()
                .addInclude("/**")
                .setExcludeList(properties.getSecurity().getExcludes())
                .setAuth(obj -> {
                    if (Boolean.TRUE.equals(properties.getSecurity().getEnabled())) {
                        SaRouter.match("/**").check(r -> StpUtil.checkLogin());
                    }
                })
                .setError(e -> {
                    // NotLoginException 等鉴权异常 → 401；其余按系统异常处理
                    boolean notLogin = e instanceof cn.dev33.satoken.exception.NotLoginException;
                    Result<Void> result = notLogin
                            ? Result.fail(GlobalErrorCode.UNAUTHORIZED)
                            : Result.fail(GlobalErrorCode.INTERNAL_SERVER_ERROR);
                    SaHolder.getResponse()
                            .setStatus(notLogin ? 401 : 500)
                            .setHeader("Content-Type", "application/json;charset=UTF-8");
                    return toJson(result);
                });
    }

    /**
     * 跨域配置（开发期推荐使用前端代理，此配置用于直连场景）
     */
    @Bean
    public CorsWebFilter corsWebFilter(GatewayProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(properties.getCors().getAllowedOrigins());
        config.setAllowedMethods(Collections.singletonList("*"));
        config.setAllowedHeaders(Collections.singletonList("*"));
        config.setAllowCredentials(properties.getCors().getAllowCredentials());
        config.setMaxAge(properties.getCors().getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsWebFilter(source);
    }

    private String toJson(Result<Void> result) {
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException ex) {
            return "{\"code\":500,\"message\":\"系统内部错误\",\"timestamp\":" + System.currentTimeMillis() + "}";
        }
    }

}
