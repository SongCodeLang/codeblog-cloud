package site.codeblog.system.dict;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 字典回写标注（MyBatis-Flex SetListener 机制，见架构文档回填）
 * <p>
 * 标注在实体的字典值字段（如 status）上，字段被 set（含查询结果映射）时，
 * {@link DictSetListener} 自动从字典缓存取 label 回写到展示字段。
 *
 * @author codeblog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DictLabel {

    /**
     * 字典类型编码（sys_dict_type.dict_type）
     */
    String value();

    /**
     * 回写目标字段名；默认当前字段名 + Label（如 status → statusLabel）
     */
    String labelField() default "";

}
