package site.codeblog.web.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Web 自动配置类
 *
 * @author codeblog
 */
@Configuration
@ComponentScan(basePackages = "site.codeblog.web")
public class WebAutoConfiguration {
    
}