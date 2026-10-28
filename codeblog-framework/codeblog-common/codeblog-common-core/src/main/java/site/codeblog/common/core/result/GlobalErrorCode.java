package site.codeblog.common.core.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局错误码枚举
 * <p>
 * 约定：code 与 HTTP 状态码语义对齐，便于前端统一处理。
 *
 * @author codeblog
 */
@Getter
@AllArgsConstructor
public enum GlobalErrorCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),

    /** 客户端错误 */
    BAD_REQUEST(400, "请求参数不正确"),
    UNAUTHORIZED(401, "账号未登录或登录已过期"),
    FORBIDDEN(403, "没有该操作权限"),
    NOT_FOUND(404, "请求地址不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "数据冲突，请刷新后重试"),
    PAYLOAD_TOO_LARGE(413, "上传文件超出大小限制"),

    /** 服务端错误 */
    INTERNAL_SERVER_ERROR(500, "系统内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂不可用，请稍后重试"),

    /** 业务错误 */
    BUSINESS_FAILED(600, "业务处理失败"),

    /** 认证相关 */
    LOGIN_FAILED(1001, "账号或密码不正确"),
    ACCOUNT_DISABLED(1002, "账号已被停用"),
    ACCOUNT_NOT_EXIST(1003, "账号不存在"),
    DUPLICATE_USERNAME(1004, "用户名已存在");

    /**
     * 错误码
     */
    private final Integer code;

    /**
     * 错误提示
     */
    private final String message;

}
