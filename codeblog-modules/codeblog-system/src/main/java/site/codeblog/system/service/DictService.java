package site.codeblog.system.service;

import site.codeblog.common.core.result.PageResult;
import site.codeblog.system.vo.DictItemPageReqVO;
import site.codeblog.system.vo.DictItemRespVO;
import site.codeblog.system.vo.DictItemSaveReqVO;
import site.codeblog.system.vo.DictTypePageReqVO;
import site.codeblog.system.vo.DictTypeRespVO;
import site.codeblog.system.vo.DictTypeSaveReqVO;

import java.util.List;

/**
 * 数据字典服务接口
 *
 * @author codeblog
 */
public interface DictService {

    /**
     * 分页查询字典类型
     */
    PageResult<DictTypeRespVO> pageType(DictTypePageReqVO reqVO);

    /**
     * 创建字典类型
     *
     * @return 字典类型 ID
     */
    Long createType(DictTypeSaveReqVO reqVO);

    /**
     * 更新字典类型（字典类型编码不可变更，服务层忽略该字段）
     */
    void updateType(DictTypeSaveReqVO reqVO);

    /**
     * 删除字典类型（该类型下有字典项时拒绝）
     */
    void deleteType(Long id);

    /**
     * 分页查询字典项
     */
    PageResult<DictItemRespVO> pageItem(DictItemPageReqVO reqVO);

    /**
     * 创建字典项
     *
     * @return 字典项 ID
     */
    Long createItem(DictItemSaveReqVO reqVO);

    /**
     * 更新字典项（dictType/itemValue 不可变更，服务层忽略）
     */
    void updateItem(DictItemSaveReqVO reqVO);

    /**
     * 删除字典项
     */
    void deleteItem(Long id);

    /**
     * 按字典类型查全部启用项（前端下拉，登录即可）
     */
    List<DictItemRespVO> listByType(String dictType);

}
