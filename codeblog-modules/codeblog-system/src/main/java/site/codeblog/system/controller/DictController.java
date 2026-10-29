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
import site.codeblog.system.service.DictService;
import site.codeblog.system.vo.DictItemPageReqVO;
import site.codeblog.system.vo.DictItemRespVO;
import site.codeblog.system.vo.DictItemSaveReqVO;
import site.codeblog.system.vo.DictTypePageReqVO;
import site.codeblog.system.vo.DictTypeRespVO;
import site.codeblog.system.vo.DictTypeSaveReqVO;

import java.util.List;

/**
 * 数据字典接口
 *
 * @author codeblog
 */
@Tag(name = "数据字典")
@RestController
@RequestMapping("/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictService dictService;

    // ---- 字典类型 ----

    @Operation(summary = "分页查询字典类型")
    @GetMapping("/type/page")
    @SaCheckPermission("system:dict:list")
    public Result<PageResult<DictTypeRespVO>> pageType(@Valid DictTypePageReqVO reqVO) {
        return Result.success(dictService.pageType(reqVO));
    }

    @Operation(summary = "创建字典类型")
    @PostMapping("/type/create")
    @SaCheckPermission("system:dict:create")
    public Result<Long> createType(@Valid @RequestBody DictTypeSaveReqVO reqVO) {
        return Result.success(dictService.createType(reqVO));
    }

    @Operation(summary = "更新字典类型")
    @PutMapping("/type/update")
    @SaCheckPermission("system:dict:update")
    public Result<Void> updateType(@Valid @RequestBody DictTypeSaveReqVO reqVO) {
        dictService.updateType(reqVO);
        return Result.success();
    }

    @Operation(summary = "删除字典类型")
    @DeleteMapping("/type/delete/{id}")
    @SaCheckPermission("system:dict:delete")
    public Result<Void> deleteType(@PathVariable Long id) {
        dictService.deleteType(id);
        return Result.success();
    }

    // ---- 字典项 ----

    @Operation(summary = "分页查询字典项")
    @GetMapping("/item/page")
    @SaCheckPermission("system:dict:query")
    public Result<PageResult<DictItemRespVO>> pageItem(@Valid DictItemPageReqVO reqVO) {
        return Result.success(dictService.pageItem(reqVO));
    }

    @Operation(summary = "创建字典项")
    @PostMapping("/item/create")
    @SaCheckPermission("system:dict:create")
    public Result<Long> createItem(@Valid @RequestBody DictItemSaveReqVO reqVO) {
        return Result.success(dictService.createItem(reqVO));
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/item/update")
    @SaCheckPermission("system:dict:update")
    public Result<Void> updateItem(@Valid @RequestBody DictItemSaveReqVO reqVO) {
        dictService.updateItem(reqVO);
        return Result.success();
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/item/delete/{id}")
    @SaCheckPermission("system:dict:delete")
    public Result<Void> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return Result.success();
    }

    // ---- 字典数据（前端下拉，登录即可）----

    @Operation(summary = "按字典类型查启用项（下拉数据源）")
    @GetMapping("/data/{dictType}")
    public Result<List<DictItemRespVO>> data(@PathVariable String dictType) {
        return Result.success(dictService.listByType(dictType));
    }

}
