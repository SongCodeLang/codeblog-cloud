package site.codeblog.excel.test;

import site.codeblog.common.core.exception.BusinessException;
import site.codeblog.excel.core.ExcelException;

/**
 * 依赖关系验证测试类
 *
 * @author codeblog
 */
public class DependencyTest {
    
    public static void main(String[] args) {
        // 验证 ExcelException 是否继承了 BusinessException
        ExcelException excelException = new ExcelException("Excel处理错误");
        
        // 验证类型转换
        BusinessException businessException = excelException;
        
        System.out.println("ExcelException instanceof BusinessException: " + 
                          (excelException instanceof BusinessException));
        System.out.println("ExcelException instanceof RuntimeException: " + 
                          (excelException instanceof RuntimeException));
        System.out.println("验证通过：ExcelException 可以被全局异常处理器捕获");
    }
}