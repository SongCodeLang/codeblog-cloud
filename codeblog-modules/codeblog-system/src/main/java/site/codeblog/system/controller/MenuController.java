package site.codeblog.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import site.codeblog.common.core.result.Result;
import site.codeblog.system.service.MenuService;
import site.codeblog.system.vo.MenuTreeRespVO;

import java.util.List;

/**
 * 菜单管理接口
 *
 * @author codeblog
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/menu")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "全量菜单树（角色授权选择用）")
    @GetMapping("/tree")
    @SaCheckPermission("system:menu:list")
    public Result<List<MenuTreeRespVO>> tree() {
        return Result.success(menuService.tree());
    }

}
