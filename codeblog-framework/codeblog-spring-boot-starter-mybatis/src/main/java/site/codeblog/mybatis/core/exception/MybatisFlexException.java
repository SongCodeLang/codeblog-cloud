package site.codeblog.mybatis.core.exception;

import lombok.Getter;

/**
 * MyBatis-Flex 自定义异常类
 *
 * @author codeblog
 */
@Getter
public class MybatisFlexException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private final String errorCode;

    /**
     * 错误信息
     */
    private final String errorMessage;

    public MybatisFlexException(String message) {
        super(message);
        this.errorCode = "MYBATIS_FLEX_ERROR";
        this.errorMessage = message;
    }

    public MybatisFlexException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.errorMessage = message;
    }

    public MybatisFlexException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "MYBATIS_FLEX_ERROR";
        this.errorMessage = message;
    }

    public MybatisFlexException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errorMessage = message;
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
     * SQL审计相关异常
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