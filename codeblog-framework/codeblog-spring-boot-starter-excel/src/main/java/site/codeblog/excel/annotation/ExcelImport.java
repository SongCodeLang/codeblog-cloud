package site.codeblog.excel.annotation;

import java.lang.annotation.*;

/**
 * Excel 导入注解
 *
 * @author codeblog
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ExcelImport {

    /**
     * 列名
     */
    String value() default "";

    /**
     * 列索引
     */
    int index() default -1;

    /**
     * 是否必填
     */
    boolean required() default false;

    /**
     * 正则表达式验证
     */
    String regexp() default "";

    /**
     * 验证失败提示信息
     */
    String errorMsg() default "";
}