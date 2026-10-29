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
import site.codeblog.system.service.RoleService;
import site.codeblog.system.vo.RoleCreateReqVO;
import site.codeblog.system.vo.RolePageReqVO;
import site.codeblog.system.vo.RoleRespVO;
import site.codeblog.system.vo.RoleUpdateReqVO;

import java.util.List;

/**
 * 角色管理接口
 *
 * @author codeblog
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "分页查询角色")
    @GetMapping("/page")
    @SaCheckPermission("system:role:list")
    public Result<PageResult<RoleRespVO>> page(@Valid RolePageReqVO reqVO) {
        return Result.success(roleService.page(reqVO));
    }

    @Operation(summary = "启用角色列表（下拉）")
    @GetMapping("/list")
    @SaCheckPermission("system:role:list")
    public Result<List<RoleRespVO>> list() {
        return Result.success(roleService.listEnabled());
    }

    @Operation(summary = "创建角色")
    @PostMapping("/create")
    @SaCheckPermission("system:role:create")
    public Result<Long> create(@Valid @RequestBody RoleCreateReqVO reqVO) {
        return Result.success(roleService.create(reqVO));
    }

    @Operation(summary = "更新角色")
    @PutMapping("/update")
    @SaCheckPermission("system:role:update")
    public Result<Void> update(@Valid @RequestBody RoleUpdateReqVO reqVO) {
        roleService.update(reqVO);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/delete/{id}")
    @SaCheckPermission("system:role:delete")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.success();
    }

    @Operation(summary = "查询角色已绑定菜单")
    @GetMapping("/menu-ids/{roleId}")
    @SaCheckPermission("system:role:assign-menu")
    public Result<List<Long>> menuIds(@PathVariable Long roleId) {
        return Result.success(roleService.getMenuIdsByRole(roleId));
    }

    @Operation(summary = "保存角色菜单授权")
    @PutMapping("/assign-menus/{roleId}")
    @SaCheckPermission("system:role:assign-menu")
    public Result<Void> assignMenus(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        roleService.assignMenus(roleId, menuIds);
        return Result.success();
    }

}
