package site.codeblog.system.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codeblog.common.core.result.Result;
import site.codeblog.system.service.UserService;
import site.codeblog.system.vo.AuthLoginReqVO;
import site.codeblog.system.vo.AuthLoginRespVO;
import site.codeblog.system.vo.UserRespVO;

/**
 * 认证接口
 *
 * @author codeblog
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public Result<AuthLoginRespVO> login(@Valid @RequestBody AuthLoginReqVO reqVO) {
        return Result.success(userService.login(reqVO));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        userService.logout();
        return Result.success();
    }

    @Operation(summary = "当前登录用户信息")
    @GetMapping("/me")
    public Result<UserRespVO> me() {
        return Result.success(userService.currentUser());
    }

}
