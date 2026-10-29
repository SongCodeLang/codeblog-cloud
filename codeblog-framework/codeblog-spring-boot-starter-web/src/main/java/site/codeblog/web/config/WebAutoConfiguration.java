package site.codeblog.web.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import site.codeblog.common.util.SpringContextHolder;
import site.codeblog.web.exception.GlobalExceptionHandler;
import site.codeblog.web.filter.TraceIdFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Web 基础自动配置
 * <p>
 * 提供：全局异常处理、统一 JSON 序列化（Long 转字符串防止前端精度丢失、日期格式化）。
 *
 * @author codeblog
 */
@AutoConfiguration
public class WebAutoConfiguration {

    /**
     * 链路追踪 MDC 过滤器（S1）：注册为最前 Filter，先于 SaInterceptor 等一切 MVC 拦截器
     */
    @Bean
    @ConditionalOnMissingBean(TraceIdFilter.class)
    public FilterRegistrationBean<TraceIdFilter> codeblogTraceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registration = new FilterRegistrationBean<>(new TraceIdFilter());
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        registration.setName("codeblogTraceIdFilter");
        return registration;
    }

    /**
     * Spring 上下文工具：随 web starter 自动装配（原先 @Component 无包扫描永远不生效，阶段4 修复）
     */
    @Bean
    @ConditionalOnMissingBean(SpringContextHolder.class)
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    /**
     * 全局异常处理器
     */
    @Bean
    @ConditionalOnMissingBean(GlobalExceptionHandler.class)
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * JSON 序列化定制：
     * 1. Long 序列化为字符串，避免 JS Number 精度丢失（雪花 ID 等 19 位长整数）
     * 2. LocalDateTime / LocalDate 统一格式
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer codeblogJacksonCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance)
                .serializerByType(LocalDateTime.class,
                        new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .serializerByType(LocalDate.class,
                        new com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer(
                                DateTimeFormatter.ofPattern("yyyy-MM-dd")));
    }

}
