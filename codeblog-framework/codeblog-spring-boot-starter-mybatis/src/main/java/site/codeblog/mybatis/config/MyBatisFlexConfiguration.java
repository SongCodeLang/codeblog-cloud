package site.codeblog.mybatis.config;

import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * MyBatis-Flex 自动配置
 * <p>
 * 统一约定：逻辑删除列 del_flag（0 正常 / 1 已删除）、乐观锁列 version；
 * SQL 审计默认关闭，通过 {@code codeblog.mybatis.audit-console-enabled=true} 开启（仅建议开发环境使用）。
 *
 * @author codeblog
 */
@Slf4j
@AutoConfiguration
public class MyBatisFlexConfiguration implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig globalConfig) {
        // 设置数据库正常时的值
        globalConfig.setNormalValueOfLogicDelete("0");
        // 设置数据已被删除时的值
        globalConfig.setDeletedValueOfLogicDelete("1");
        globalConfig.setLogicDeleteColumn("del_flag");
        globalConfig.setVersionColumn("version");
        log.info("[codeblog-mybatis] MyBatis-Flex 全局配置完成：逻辑删除列 del_flag，乐观锁列 version");
    }

    /**
     * SQL 审计：控制台打印执行 SQL，默认关闭
     */
    @Bean
    @ConditionalOnProperty(prefix = "codeblog.mybatis", name = "audit-console-enabled", havingValue = "true")
    public ConsoleMessageCollector consoleMessageCollector() {
        ConsoleMessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
        AuditManager.setAuditEnable(true);
        log.info("[codeblog-mybatis] SQL 控制台审计已开启");
        return collector;
    }

}
