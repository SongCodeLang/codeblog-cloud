package site.codeblog.mybatis.config;


import com.mybatisflex.core.FlexGlobalConfig;
import com.mybatisflex.core.audit.AuditManager;
import com.mybatisflex.core.audit.ConsoleMessageCollector;
import com.mybatisflex.core.audit.MessageCollector;
import com.mybatisflex.spring.boot.MyBatisFlexCustomizer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;


/**
 * MyBatis-Flex 自定义配置器
 * 负责配置MyBatis-Flex的各种特性
 *
 * @author codeblog
 */

@Slf4j
@Configuration
public class MyBatisFlexConfiguration implements MyBatisFlexCustomizer {

    @Override
    public void customize(FlexGlobalConfig globalConfig) {

        //设置数据库正常时的值
        globalConfig.setNormalValueOfLogicDelete("0");
        //设置数据已被删除时的值
        globalConfig.setDeletedValueOfLogicDelete("1");
        globalConfig.setLogicDeleteColumn("del_flag");
        globalConfig.setVersionColumn("version");

        MessageCollector collector = new ConsoleMessageCollector();
        AuditManager.setMessageCollector(collector);
        AuditManager.setAuditEnable(true);

    }

}