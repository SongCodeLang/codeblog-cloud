package site.codeblog.security.exception;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.Result;

/**
 * Sa-Token 异常处理器
 * <p>
 * 将 Sa-Token 抛出的鉴权异常统一转换为 {@code Result}。
 *
 * @author codeblog
 */
@Slf4j
@RestControllerAdvice
public class SaTokenExceptionHandler {

    /**
     * 未登录 / token 已过期
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        log.warn("未登录访问: {}", e.getMessage());
        return Result.fail(GlobalErrorCode.UNAUTHORIZED);
    }

    /**
     * 缺少权限
     */
    @ExceptionHandler(NotPermissionException.class)
    public Result<Void> handleNotPermissionException(NotPermissionException e) {
        log.warn("缺少权限: {}", e.getPermission());
        return Result.fail(GlobalErrorCode.FORBIDDEN, "没有访问权限: " + e.getPermission());
    }

    /**
     * 缺少角色
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        log.warn("缺少角色: {}", e.getRole());
        return Result.fail(GlobalErrorCode.FORBIDDEN);
    }

}
