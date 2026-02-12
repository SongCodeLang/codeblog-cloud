package site.codeblog.excel.utils;

import cn.idev.excel.EasyExcel;
import cn.idev.excel.ExcelWriter;
import cn.idev.excel.write.metadata.WriteSheet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;
import site.codeblog.excel.annotation.ExcelExport;
import site.codeblog.excel.annotation.ExcelImport;
import site.codeblog.excel.core.ExcelException;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Excel 工具类
 *
 * @author codeblog
 */
@Slf4j
public class ExcelUtils {

    /**
     * 导出 Excel 文件
     *
     * @param response HTTP 响应
     * @param fileName 文件名
     * @param sheetName sheet 名称
     * @param data 数据列表
     * @param clazz 数据类型
     * @param <T> 泛型
     */
    public static <T> void exportExcel(HttpServletResponse response, String fileName,
                                       String sheetName, List<T> data, Class<T> clazz) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 创建 ExcelWriter
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), clazz).build();
            
            // 创建 sheet
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();
            
            // 写入数据
            excelWriter.write(data, writeSheet);
            
            // 关闭 writer
            excelWriter.finish();
            
        } catch (IOException e) {
            log.error("导出 Excel 失败", e);
            throw ExcelException.of("导出 Excel 失败: " + e.getMessage());
        }
    }

    /**
     * 导入 Excel 文件
     *
     * @param file 上传的文件
     * @param clazz 数据类型
     * @param <T> 泛型
     * @return 数据列表
     */
    public static <T> List<T> importExcel(MultipartFile file, Class<T> clazz) {
        try {
            return importExcel(file.getInputStream(), clazz);
        } catch (IOException e) {
            log.error("导入 Excel 失败", e);
            throw ExcelException.of("导入 Excel 失败: " + e.getMessage());
        }
    }

    /**
     * 从 InputStream 导入 Excel
     *
     * @param inputStream 输入流
     * @param clazz 数据类型
     * @param <T> 泛型
     * @return 数据列表
     */
    public static <T> List<T> importExcel(InputStream inputStream, Class<T> clazz) {
        try {
            // 使用同步读取方法
            return EasyExcel.read(inputStream, clazz, null)
                    .sheet()
                    .doReadSync();
        } catch (Exception e) {
            log.error("导入 Excel 失败", e);
            throw ExcelException.of("导入 Excel 失败: " + e.getMessage());
        }
    }

    /**
     * 生成模板文件
     *
     * @param response HTTP 响应
     * @param fileName 文件名
     * @param sheetName sheet 名称
     * @param clazz 数据类型
     * @param <T> 泛型
     */
    public static <T> void generateTemplate(HttpServletResponse response, String fileName, 
                                          String sheetName, Class<T> clazz) {
        try {
            // 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString())
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);

            // 创建 ExcelWriter
            ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build();
            
            // 创建 sheet
            WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).head(clazz).build();
            
            // 写入空数据（只写入表头）
            excelWriter.write(new ArrayList<>(), writeSheet);
            
            // 关闭 writer
            excelWriter.finish();
            
        } catch (IOException e) {
            log.error("生成模板失败", e);
            throw ExcelException.of("生成模板失败: " + e.getMessage());
        }
    }

    /**
     * 获取字段上的 ExcelExport 注解信息
     *
     * @param field 字段
     * @return 注解信息
     */
    public static ExcelExport getExcelExportAnnotation(Field field) {
        return field.getAnnotation(ExcelExport.class);
    }

    /**
     * 获取字段上的 ExcelImport 注解信息
     *
     * @param field 字段
     * @return 注解信息
     */
    public static ExcelImport getExcelImportAnnotation(Field field) {
        return field.getAnnotation(ExcelImport.class);
    }

    /**
     * 验证导入数据
     *
     * @param data 数据对象
     * @param <T> 泛型
     */
    public static <T> void validateImportData(T data) {
        Class<?> clazz = data.getClass();
        Field[] fields = clazz.getDeclaredFields();
        
        for (Field field : fields) {
            field.setAccessible(true);
            ExcelImport importAnnotation = getExcelImportAnnotation(field);
            
            if (importAnnotation != null && importAnnotation.required()) {
                try {
                    Object value = field.get(data);
                    if (value == null || "".equals(value.toString().trim())) {
                        throw ExcelException.of(
                            String.format("字段 [%s] 不能为空", 
                                importAnnotation.value().isEmpty() ? field.getName() : importAnnotation.value())
                        );
                    }
                    
                    // 正则表达式验证
                    if (!importAnnotation.regexp().isEmpty() && value != null) {
                        if (!value.toString().matches(importAnnotation.regexp())) {
                            throw ExcelException.of(
                                importAnnotation.errorMsg().isEmpty() ? 
                                    String.format("字段 [%s] 格式不正确", 
                                        importAnnotation.value().isEmpty() ? field.getName() : importAnnotation.value()) :
                                    importAnnotation.errorMsg()
                            );
                        }
                    }
                } catch (IllegalAccessException e) {
                    log.error("验证导入数据时发生错误", e);
                    throw ExcelException.of("验证导入数据时发生错误: " + e.getMessage());
                }
            }
        }
    }
}