package site.codeblog.security.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.strategy.SaJsonStrategy;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import site.codeblog.security.core.LoginUser;
import site.codeblog.security.core.sa.DefaultStpInterfaceImpl;
import site.codeblog.security.exception.SaTokenExceptionHandler;

/**
 * Sa-Token 安全自动配置（Servlet 环境）
 * <p>
 * 功能：
 * 1. 注册路由拦截器，按 {@link SecurityProperties#getExcludes()} 白名单放行；
 * 2. 提供默认 {@link StpInterface}（返回空权限，业务服务按需覆盖）；
 * 3. 注册 Sa-Token 异常处理器，将未登录/无权限统一转换为 {@code Result}。
 *
 * @author codeblog
 */
@AutoConfiguration
@ConditionalOnClass(StpUtil.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(SecurityProperties.class)
public class SecurityAutoConfiguration {

    static {
        // Sa-Token 1.46+ 的 Jackson 序列化带全局类型白名单，
        // 会话中存储的 LoginUser 必须先注册才能正常反序列化
        SaJsonStrategy.instance.registerAllowType(LoginUser.class);
    }

    /**
     * 登录校验拦截器（codeblog.security.enabled=false 可整体关闭校验）
     */
    @Bean
    public WebMvcConfigurer codeblogSaInterceptorConfigurer(SecurityProperties properties) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(new SaInterceptor(handle -> {
                            if (Boolean.TRUE.equals(properties.getEnabled())) {
                                StpUtil.checkLogin();
                            }
                        }))
                        .addPathPatterns("/**")
                        .excludePathPatterns(properties.getExcludes());
            }
        };
    }

    /**
     * 默认权限数据源：无角色无权限，业务服务按需覆盖
     */
    @Bean
    @ConditionalOnMissingBean(StpInterface.class)
    public StpInterface stpInterface() {
        return new DefaultStpInterfaceImpl();
    }

    /**
     * Sa-Token 异常处理
     */
    @Bean
    @ConditionalOnMissingBean(SaTokenExceptionHandler.class)
    public SaTokenExceptionHandler saTokenExceptionHandler() {
        return new SaTokenExceptionHandler();
    }

}
