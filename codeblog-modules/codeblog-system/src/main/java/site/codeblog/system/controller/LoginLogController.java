package site.codeblog.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.common.core.result.Result;
import site.codeblog.system.service.LoginLogService;
import site.codeblog.system.vo.LoginLogPageReqVO;
import site.codeblog.system.vo.LoginLogRespVO;

/**
 * 登录日志接口
 *
 * @author codeblog
 */
@Tag(name = "登录日志")
@RestController
@RequestMapping("/login-log")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping("/page")
    @SaCheckPermission("system:login-log:list")
    public Result<PageResult<LoginLogRespVO>> page(@Valid LoginLogPageReqVO reqVO) {
        return Result.success(loginLogService.page(reqVO));
    }

}
