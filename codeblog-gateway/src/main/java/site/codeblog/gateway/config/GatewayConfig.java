package site.codeblog.gateway.config;

import org.springframework.context.annotation.Configuration;

/**
 * 网关配置类
 *
 * @author codeblog
 */
@Configuration
public class GatewayConfig {

    // TODO: 待依赖完全解析后启用 Sa-Token 配置
    /*
    @Bean
    public SaReactorFilter getSaReactorFilter() {
        return new SaReactorFilter()
                .addInclude("/**")
                .addExclude("/favicon.ico")
                .setAuth(obj -> {
                    SaRouter.match("/**", "/user/doLogin", r -> StpUtil.checkLogin());
                })
                .setError(e -> {
                    return SaResult.error(e.getMessage());
                });
    }
    */
}