package site.codeblog.system.dict;

import com.mybatisflex.annotation.SetListener;
import lombok.extern.slf4j.Slf4j;
import site.codeblog.common.util.SpringContextHolder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 字典回写监听器（MyBatis-Flex SetListener 机制）
 * <p>
 * 实体经 {@code @Table(onSet = DictSetListener.class)} 挂载；字段被 set
 * （含查询结果映射）时按 {@link DictLabel} 注解从字典缓存取 label 回写到展示字段。
 * Flex 以无参构造静态实例化 listener，字典查询经 {@link SpringContextHolder}
 * 获取 bean；注解元数据按类缓存，回写失败仅告警不影响主流程（展示增强）。
 *
 * @author codeblog
 */
@Slf4j
public class DictSetListener implements SetListener {

    /**
     * 类 → (属性名 → 回写元数据)，避免每次 set 反射扫描
     */
    private static final Map<Class<?>, Map<String, DictFieldMeta>> META_CACHE = new ConcurrentHashMap<>();

    @Override
    public Object onSet(Object entity, String property, Object value) {
        try {
            DictFieldMeta meta = META_CACHE
                    .computeIfAbsent(entity.getClass(), DictSetListener::resolve)
                    .get(property);
            if (meta != null && value != null) {
                DictCacheService dictCacheService = SpringContextHolder.getBean(DictCacheService.class);
                String label = dictCacheService.getLabel(meta.dictType, String.valueOf(value));
                if (label != null) {
                    meta.labelField.set(entity, label);
                }
            }
        } catch (Exception e) {
            // 字典回写属展示增强，失败不影响数据落库/查询
            log.warn("字典回写失败：class={}, property={}, value={}, err={}",
                    entity.getClass().getSimpleName(), property, value, e.getMessage());
        }
        return value;
    }

    /** 扫描实体字段（含父类）上的 {@link DictLabel} 注解 */
    private static Map<String, DictFieldMeta> resolve(Class<?> entityClass) {
        Map<String, DictFieldMeta> metas = new ConcurrentHashMap<>();
        for (Class<?> clazz = entityClass; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                DictLabel annotation = field.getAnnotation(DictLabel.class);
                if (annotation == null) {
                    continue;
                }
                String labelName = annotation.labelField().isBlank()
                        ? field.getName() + "Label" : annotation.labelField();
                try {
                    Field labelField = entityClass.getDeclaredField(labelName);
                    labelField.setAccessible(true);
                    metas.put(field.getName(), new DictFieldMeta(annotation.value(), labelField));
                } catch (NoSuchFieldException e) {
                    log.warn("字典回写目标字段缺失：class={}, field={}, labelField={}",
                            entityClass.getSimpleName(), field.getName(), labelName);
                }
            }
        }
        return metas;
    }

    /**
     * 单个回写配置
     */
    private record DictFieldMeta(String dictType, Field labelField) {
    }

}
