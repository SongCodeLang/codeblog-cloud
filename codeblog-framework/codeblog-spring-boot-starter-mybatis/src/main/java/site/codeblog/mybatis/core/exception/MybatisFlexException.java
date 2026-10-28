package site.codeblog.mybatis.core.exception;

import lombok.Getter;
import site.codeblog.common.core.exception.BusinessException;

/**
 * MyBatis-Flex 组件异常
 * <p>
 * 继承 {@link BusinessException}，可被全局异常处理器统一转换为 Result 响应。
 *
 * @author codeblog
 */
@Getter
public class MybatisFlexException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 组件错误标识（如 DATASOURCE_ERROR / AUDIT_ERROR / CONFIGURATION_ERROR）
     */
    private final String errorCode;

    public MybatisFlexException(String message) {
        super(message);
        this.errorCode = "MYBATIS_FLEX_ERROR";
    }

    public MybatisFlexException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public MybatisFlexException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MYBATIS_FLEX_ERROR";
    }

    public MybatisFlexException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 数据源相关异常
     */
    public static class DataSourceException extends MybatisFlexException {
        public DataSourceException(String message) {
            super("DATASOURCE_ERROR", message);
        }

        public DataSourceException(String message, Throwable cause) {
            super("DATASOURCE_ERROR", message, cause);
        }
    }

    /**
     * SQL 审计相关异常
     */
    public static class AuditException extends MybatisFlexException {
        public AuditException(String message) {
            super("AUDIT_ERROR", message);
        }

        public AuditException(String message, Throwable cause) {
            super("AUDIT_ERROR", message, cause);
        }
    }

    /**
     * 配置相关异常
     */
    public static class ConfigurationException extends MybatisFlexException {
        public ConfigurationException(String message) {
            super("CONFIGURATION_ERROR", message);
        }

        public ConfigurationException(String message, Throwable cause) {
            super("CONFIGURATION_ERROR", message, cause);
        }
    }
}
