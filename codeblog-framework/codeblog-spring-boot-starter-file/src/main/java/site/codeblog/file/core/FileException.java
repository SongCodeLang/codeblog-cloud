package site.codeblog.file.core;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.exception.BusinessException;

/**
 * 文件处理异常
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class FileException extends BusinessException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private Integer code;

    /**
     * 错误信息
     */
    private String message;

    public FileException() {
        super();
    }

    public FileException(String message) {
        super(message);
        this.message = message;
        this.code = 500;
    }

    public FileException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public FileException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 500;
    }

    public FileException(Integer code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 创建文件异常
     */
    public static FileException of(String message) {
        return new FileException(message);
    }

    /**
     * 创建文件异常
     */
    public static FileException of(Integer code, String message) {
        return new FileException(code, message);
    }
}
