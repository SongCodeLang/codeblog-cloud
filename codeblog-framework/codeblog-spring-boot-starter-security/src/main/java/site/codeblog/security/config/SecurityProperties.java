package site.codeblog.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 安全组件配置属性
 *
 * @author codeblog
 */
@Data
@ConfigurationProperties(prefix = "codeblog.security")
public class SecurityProperties {

    /**
     * 是否开启登录校验（本地联调时可关闭）
     */
    private Boolean enabled = Boolean.TRUE;

    /**
     * 无需登录即可访问的路径白名单（Ant 风格）
     * <p>
     * 例如：/auth/login、/doc.html、/v3/api-docs/** 等
     */
    private List<String> excludes = new ArrayList<>();

}
