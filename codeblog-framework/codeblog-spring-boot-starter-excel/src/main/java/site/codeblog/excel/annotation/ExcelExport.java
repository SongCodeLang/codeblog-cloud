package site.codeblog.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel 导出注解
 *
 * @author codeblog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelExport {

    /**
     * 列名
     */
    String value() default "";

    /**
     * 列索引
     */
    int index() default -1;

    /**
     * 是否忽略该字段
     */
    boolean ignore() default false;

    /**
     * 日期格式
     */
    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    /**
     * 数字格式
     */
    String numberFormat() default "#.##";

    /**
     * 列宽
     */
    int width() default 20;

    /**
     * 是否可为空
     */
    boolean nullable() default true;
}