package site.codeblog.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.common.core.result.Result;
import site.codeblog.user.service.UserService;
import site.codeblog.user.vo.UserCreateReqVO;
import site.codeblog.user.vo.UserPageReqVO;
import site.codeblog.user.vo.UserResetPasswordReqVO;
import site.codeblog.user.vo.UserRespVO;
import site.codeblog.user.vo.UserUpdateReqVO;

/**
 * 用户管理接口
 *
 * @author codeblog
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<UserRespVO>> page(@Valid UserPageReqVO reqVO) {
        return Result.success(userService.page(reqVO));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/get/{id}")
    public Result<UserRespVO> get(@PathVariable Long id) {
        return Result.success(userService.get(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping("/create")
    public Result<Long> create(@Valid @RequestBody UserCreateReqVO reqVO) {
        return Result.success(userService.create(reqVO));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update")
    public Result<Void> update(@Valid @RequestBody UserUpdateReqVO reqVO) {
        userService.update(reqVO);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody UserResetPasswordReqVO reqVO) {
        userService.resetPassword(reqVO);
        return Result.success();
    }

}
