package site.codeblog.system.dict;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import site.codeblog.system.constant.CacheConstants;
import site.codeblog.system.entity.DictItemPO;
import site.codeblog.system.mapper.DictItemMapper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static site.codeblog.system.entity.table.DictItemPOTableDef.DICT_ITEM_PO;

/**
 * 字典数据缓存服务（{@link DictSetListener} 与前端下拉共用的数据源）
 * <p>
 * 经 CacheManager 直读直写（不用 @Cacheable：getLabel 内部调 getLabelMap
 * 会绕过代理失效缓存），缓存名 codeblog:system:dict、key 为字典类型编码，
 * TTL 10 分钟兜底；字典项/类型变更由 {@code DictServiceImpl} evict。
 *
 * @author codeblog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DictCacheService {

    private final CacheManager cacheManager;
    private final DictItemMapper dictItemMapper;

    /**
     * 取单值的字典标签（value → label 查不到返回 null）
     */
    public String getLabel(String dictType, String itemValue) {
        return getLabelMap(dictType).get(itemValue);
    }

    /**
     * 取字典类型的 value → label 映射（仅启用项，按 sort_order 排序）
     */
    @SuppressWarnings("unchecked")
    public Map<String, String> getLabelMap(String dictType) {
        Cache cache = cacheManager.getCache(CacheConstants.DICT);
        if (cache == null) {
            return loadLabelMap(dictType);
        }
        Map<String, String> cached = cache.get(dictType, Map.class);
        return cached != null ? cached : loadLabelMap(dictType);
    }

    /**
     * 失效指定字典类型的缓存（字典项/类型变更后调用）
     */
    public void evict(String dictType) {
        Cache cache = cacheManager.getCache(CacheConstants.DICT);
        if (cache != null) {
            cache.evict(dictType);
        }
    }

    private Map<String, String> loadLabelMap(String dictType) {
        List<DictItemPO> items = dictItemMapper.selectListByQuery(QueryWrapper.create()
                .where(DICT_ITEM_PO.DICT_TYPE.eq(dictType))
                .and(DICT_ITEM_PO.STATUS.eq(0))
                .orderBy(DICT_ITEM_PO.SORT_ORDER.asc(), DICT_ITEM_PO.ID.asc()));
        Map<String, String> map = items.stream().collect(Collectors.toMap(
                DictItemPO::getItemValue, DictItemPO::getItemLabel, (a, b) -> a, LinkedHashMap::new));
        Cache cache = cacheManager.getCache(CacheConstants.DICT);
        if (cache != null) {
            cache.put(dictType, map);
        }
        return map;
    }

}
