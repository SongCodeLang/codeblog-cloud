package site.codeblog.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.codeblog.common.core.exception.BusinessException;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.system.constant.ErrorCodeConstants;
import site.codeblog.system.dict.DictCacheService;
import site.codeblog.system.entity.DictItemPO;
import site.codeblog.system.entity.DictTypePO;
import site.codeblog.system.mapper.DictItemMapper;
import site.codeblog.system.mapper.DictTypeMapper;
import site.codeblog.system.service.DictService;
import site.codeblog.system.vo.DictItemPageReqVO;
import site.codeblog.system.vo.DictItemRespVO;
import site.codeblog.system.vo.DictItemSaveReqVO;
import site.codeblog.system.vo.DictTypePageReqVO;
import site.codeblog.system.vo.DictTypeRespVO;
import site.codeblog.system.vo.DictTypeSaveReqVO;

import java.util.List;

import static site.codeblog.system.entity.table.DictItemPOTableDef.DICT_ITEM_PO;
import static site.codeblog.system.entity.table.DictTypePOTableDef.DICT_TYPE_PO;

/**
 * 数据字典服务实现
 *
 * @author codeblog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictServiceImpl implements DictService {

    /**
     * 状态：启用
     */
    private static final int STATUS_ENABLED = 0;

    private final DictTypeMapper dictTypeMapper;
    private final DictItemMapper dictItemMapper;
    private final DictCacheService dictCacheService;

    @Override
    public PageResult<DictTypeRespVO> pageType(DictTypePageReqVO reqVO) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICT_TYPE_PO.DICT_NAME.like(reqVO.getDictName(), StrUtil::isNotBlank))
                .and(DICT_TYPE_PO.DICT_TYPE.like(reqVO.getDictType(), StrUtil::isNotBlank))
                .and(DICT_TYPE_PO.STATUS.eq(reqVO.getStatus(), reqVO.getStatus() != null))
                .orderBy(DICT_TYPE_PO.ID.asc());

        var page = dictTypeMapper.paginate(reqVO.getPageNo(), reqVO.getPageSize(), query);
        List<DictTypeRespVO> list = page.getRecords().stream().map(this::convertType).toList();
        return PageResult.of(list, page.getTotalRow());
    }

    @Override
    public Long createType(DictTypeSaveReqVO reqVO) {
        checkTypeUnique(reqVO.getDictType(), null);

        DictTypePO type = new DictTypePO();
        type.setDictName(reqVO.getDictName());
        type.setDictType(reqVO.getDictType());
        type.setStatus(reqVO.getStatus() == null ? STATUS_ENABLED : reqVO.getStatus());
        type.setRemark(reqVO.getRemark());
        type.setDelFlag(0);
        dictTypeMapper.insert(type);
        return type.getId();
    }

    @Override
    public void updateType(DictTypeSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw BusinessException.of(GlobalErrorCode.BAD_REQUEST, "字典类型 ID 不能为空");
        }
        DictTypePO exist = dictTypeMapper.selectOneById(reqVO.getId());
        if (exist == null) {
            throw BusinessException.of(ErrorCodeConstants.DICT_TYPE_NOT_FOUND, "字典类型不存在");
        }

        // 编码不可变更（字典项按编码归属；改码需先迁移项，本接口不支持）
        DictTypePO type = new DictTypePO();
        type.setId(reqVO.getId());
        type.setDictName(reqVO.getDictName());
        type.setStatus(reqVO.getStatus());
        type.setRemark(reqVO.getRemark());
        type.setVersion(reqVO.getVersion());
        int rows = dictTypeMapper.update(type);
        if (rows == 0) {
            throw BusinessException.of(GlobalErrorCode.CONFLICT, "字典类型已被他人修改，请刷新后重试");
        }
        // 状态变化影响启用项集合，缓存一并失效
        dictCacheService.evict(exist.getDictType());
    }

    @Override
    public void deleteType(Long id) {
        DictTypePO exist = dictTypeMapper.selectOneById(id);
        if (exist == null) {
            throw BusinessException.of(ErrorCodeConstants.DICT_TYPE_NOT_FOUND, "字典类型不存在");
        }
        Long itemCount = dictItemMapper.selectCountByQuery(QueryWrapper.create()
                .where(DICT_ITEM_PO.DICT_TYPE.eq(exist.getDictType())));
        if (itemCount > 0) {
            throw BusinessException.of(ErrorCodeConstants.DICT_TYPE_IN_USE, "该字典类型下存在字典项，请先删除字典项");
        }
        dictTypeMapper.deleteById(id);
        dictCacheService.evict(exist.getDictType());
    }

    @Override
    public PageResult<DictItemRespVO> pageItem(DictItemPageReqVO reqVO) {
        QueryWrapper query = QueryWrapper.create()
                .where(DICT_ITEM_PO.DICT_TYPE.eq(reqVO.getDictType(), StrUtil::isNotBlank))
                .and(DICT_ITEM_PO.ITEM_LABEL.like(reqVO.getItemLabel(), StrUtil::isNotBlank))
                .and(DICT_ITEM_PO.STATUS.eq(reqVO.getStatus(), reqVO.getStatus() != null))
                .orderBy(DICT_ITEM_PO.SORT_ORDER.asc(), DICT_ITEM_PO.ID.asc());

        var page = dictItemMapper.paginate(reqVO.getPageNo(), reqVO.getPageSize(), query);
        List<DictItemRespVO> list = page.getRecords().stream().map(this::convertItem).toList();
        return PageResult.of(list, page.getTotalRow());
    }

    @Override
    public Long createItem(DictItemSaveReqVO reqVO) {
        requireTypeExists(reqVO.getDictType());
        checkItemValueUnique(reqVO.getDictType(), reqVO.getItemValue(), null);

        DictItemPO item = new DictItemPO();
        item.setDictType(reqVO.getDictType());
        item.setItemLabel(reqVO.getItemLabel());
        item.setItemValue(reqVO.getItemValue());
        item.setSortOrder(reqVO.getSortOrder() == null ? 0 : reqVO.getSortOrder());
        item.setStatus(reqVO.getStatus() == null ? STATUS_ENABLED : reqVO.getStatus());
        item.setRemark(reqVO.getRemark());
        item.setDelFlag(0);
        dictItemMapper.insert(item);
        dictCacheService.evict(reqVO.getDictType());
        return item.getId();
    }

    @Override
    public void updateItem(DictItemSaveReqVO reqVO) {
        if (reqVO.getId() == null) {
            throw BusinessException.of(GlobalErrorCode.BAD_REQUEST, "字典项 ID 不能为空");
        }
        DictItemPO exist = dictItemMapper.selectOneById(reqVO.getId());
        if (exist == null) {
            throw BusinessException.of(ErrorCodeConstants.DICT_ITEM_NOT_FOUND, "字典项不存在");
        }

        // dictType/itemValue 不可变更（字典回写按 value→label 映射，改值会破坏历史数据语义）
        DictItemPO item = new DictItemPO();
        item.setId(reqVO.getId());
        item.setItemLabel(reqVO.getItemLabel());
        item.setSortOrder(reqVO.getSortOrder());
        item.setStatus(reqVO.getStatus());
        item.setRemark(reqVO.getRemark());
        item.setVersion(reqVO.getVersion());
        int rows = dictItemMapper.update(item);
        if (rows == 0) {
            throw BusinessException.of(GlobalErrorCode.CONFLICT, "字典项已被他人修改，请刷新后重试");
        }
        dictCacheService.evict(exist.getDictType());
    }

    @Override
    public void deleteItem(Long id) {
        DictItemPO exist = dictItemMapper.selectOneById(id);
        if (exist == null) {
            throw BusinessException.of(ErrorCodeConstants.DICT_ITEM_NOT_FOUND, "字典项不存在");
        }
        dictItemMapper.deleteById(id);
        dictCacheService.evict(exist.getDictType());
    }

    @Override
    public List<DictItemRespVO> listByType(String dictType) {
        List<DictItemPO> items = dictItemMapper.selectListByQuery(QueryWrapper.create()
                .where(DICT_ITEM_PO.DICT_TYPE.eq(dictType))
                .and(DICT_ITEM_PO.STATUS.eq(STATUS_ENABLED))
                .orderBy(DICT_ITEM_PO.SORT_ORDER.asc(), DICT_ITEM_PO.ID.asc()));
        return items.stream().map(this::convertItem).toList();
    }

    private void requireTypeExists(String dictType) {
        Long count = dictTypeMapper.selectCountByQuery(QueryWrapper.create()
                .where(DICT_TYPE_PO.DICT_TYPE.eq(dictType)));
        if (count == 0) {
            throw BusinessException.of(ErrorCodeConstants.DICT_TYPE_NOT_FOUND, "字典类型不存在");
        }
    }

    private void checkTypeUnique(String dictType, Long excludeId) {
        Long count = dictTypeMapper.selectCountByQuery(QueryWrapper.create()
                .where(DICT_TYPE_PO.DICT_TYPE.eq(dictType))
                .and(DICT_TYPE_PO.ID.ne(excludeId, excludeId != null)));
        if (count > 0) {
            throw BusinessException.of(ErrorCodeConstants.DICT_TYPE_DUPLICATE, "字典类型编码已存在");
        }
    }

    private void checkItemValueUnique(String dictType, String itemValue, Long excludeId) {
        Long count = dictItemMapper.selectCountByQuery(QueryWrapper.create()
                .where(DICT_ITEM_PO.DICT_TYPE.eq(dictType))
                .and(DICT_ITEM_PO.ITEM_VALUE.eq(itemValue))
                .and(DICT_ITEM_PO.ID.ne(excludeId, excludeId != null)));
        if (count > 0) {
            throw BusinessException.of(ErrorCodeConstants.DICT_ITEM_DUPLICATE, "该字典值已存在");
        }
    }

    private DictTypeRespVO convertType(DictTypePO po) {
        DictTypeRespVO respVO = new DictTypeRespVO();
        respVO.setId(po.getId());
        respVO.setDictName(po.getDictName());
        respVO.setDictType(po.getDictType());
        respVO.setStatus(po.getStatus());
        respVO.setRemark(po.getRemark());
        respVO.setVersion(po.getVersion());
        respVO.setCreateTime(po.getCreateTime());
        return respVO;
    }

    private DictItemRespVO convertItem(DictItemPO po) {
        DictItemRespVO respVO = new DictItemRespVO();
        respVO.setId(po.getId());
        respVO.setDictType(po.getDictType());
        respVO.setItemLabel(po.getItemLabel());
        respVO.setItemValue(po.getItemValue());
        respVO.setSortOrder(po.getSortOrder());
        respVO.setStatus(po.getStatus());
        respVO.setRemark(po.getRemark());
        respVO.setVersion(po.getVersion());
        respVO.setCreateTime(po.getCreateTime());
        return respVO;
    }

}
