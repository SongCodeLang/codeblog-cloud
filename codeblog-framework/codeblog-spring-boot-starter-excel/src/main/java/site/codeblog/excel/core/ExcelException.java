package site.codeblog.excel.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.exception.BusinessException;

/**
 * Excel 处理异常
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ExcelException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public ExcelException() {
        super();
    }

    public ExcelException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    public ExcelException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public ExcelException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }

    public ExcelException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 创建 Excel 异常
     */
    public static ExcelException of(String message) {
        return new ExcelException(message);
    }

    /**
     * 创建 Excel 异常
     */
    public static ExcelException of(Integer code, String message) {
        return new ExcelException(code, message);
    }
}