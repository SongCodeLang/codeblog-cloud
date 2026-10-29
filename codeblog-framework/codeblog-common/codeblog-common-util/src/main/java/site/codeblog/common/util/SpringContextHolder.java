package site.codeblog.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring 上下文工具类
 * <p>
 * 由 web starter 的 WebAutoConfiguration 注册（阶段4），不再依赖包扫描 @Component。
 *
 * @author codeblog
 */
@Slf4j
public class SpringContextHolder implements ApplicationContextAware {
    
    private static ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringContextHolder.applicationContext = applicationContext;
        log.info("ApplicationContext 已注入");
    }
    
    /**
     * 获取 ApplicationContext
     */
    public static ApplicationContext getApplicationContext() {
        assertApplicationContext();
        return applicationContext;
    }
    
    /**
     * 根据名称获取 Bean
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) {
        assertApplicationContext();
        return (T) applicationContext.getBean(beanName);
    }
    
    /**
     * 根据类型获取 Bean
     */
    public static <T> T getBean(Class<T> clazz) {
        assertApplicationContext();
        return applicationContext.getBean(clazz);
    }
    
    /**
     * 根据名称和类型获取 Bean
     */
    public static <T> T getBean(String beanName, Class<T> clazz) {
        assertApplicationContext();
        return applicationContext.getBean(beanName, clazz);
    }
    
    /**
     * 检查 ApplicationContext 是否已注入
     */
    private static void assertApplicationContext() {
        if (applicationContext == null) {
            throw new RuntimeException("ApplicationContext 未注入，请检查是否在 Spring 环境中运行");
        }
    }
}