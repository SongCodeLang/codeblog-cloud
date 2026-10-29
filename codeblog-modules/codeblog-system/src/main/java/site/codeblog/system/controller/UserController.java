package site.codeblog.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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
import site.codeblog.system.service.UserService;
import site.codeblog.system.vo.UserCreateReqVO;
import site.codeblog.system.vo.UserPageReqVO;
import site.codeblog.system.vo.UserResetPasswordReqVO;
import site.codeblog.system.vo.UserRespVO;
import site.codeblog.system.vo.UserUpdateReqVO;

import java.util.List;

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
    @SaCheckPermission("system:user:list")
    public Result<PageResult<UserRespVO>> page(@Valid UserPageReqVO reqVO) {
        return Result.success(userService.page(reqVO));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/get/{id}")
    @SaCheckPermission("system:user:query")
    public Result<UserRespVO> get(@PathVariable Long id) {
        return Result.success(userService.get(id));
    }

    @Operation(summary = "创建用户")
    @PostMapping("/create")
    @SaCheckPermission("system:user:create")
    public Result<Long> create(@Valid @RequestBody UserCreateReqVO reqVO) {
        return Result.success(userService.create(reqVO));
    }

    @Operation(summary = "更新用户")
    @PutMapping("/update")
    @SaCheckPermission("system:user:update")
    public Result<Void> update(@Valid @RequestBody UserUpdateReqVO reqVO) {
        userService.update(reqVO);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:user:delete")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/reset-password")
    @SaCheckPermission("system:user:reset-password")
    public Result<Void> resetPassword(@Valid @RequestBody UserResetPasswordReqVO reqVO) {
        userService.resetPassword(reqVO);
        return Result.success();
    }

    @Operation(summary = "查询用户已绑定角色")
    @GetMapping("/role-ids/{userId}")
    @SaCheckPermission("system:user:assign-role")
    public Result<List<Long>> roleIds(@PathVariable Long userId) {
        return Result.success(userService.getRoleIds(userId));
    }

    @Operation(summary = "保存用户角色分配")
    @PutMapping("/assign-roles/{userId}")
    @SaCheckPermission("system:user:assign-role")
    public Result<Void> assignRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        userService.assignRoles(userId, roleIds);
        return Result.success();
    }

}
