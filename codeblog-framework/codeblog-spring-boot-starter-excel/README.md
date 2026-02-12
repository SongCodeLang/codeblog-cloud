# Excel 组件使用说明

## 功能特性

- ✅ 基于 FastExcel 实现高性能 Excel 读写
- ✅ 支持注解驱动的字段映射
- ✅ 自动生成导入模板
- ✅ 数据验证和错误处理
- ✅ 支持大文件处理
- ✅ 简单易用的 API

## 依赖说明

该组件依赖 `codeblog-common-core` 模块，确保异常能够被全局异常处理器捕获。

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>site.codeblog.cloud</groupId>
    <artifactId>codeblog-spring-boot-starter-excel</artifactId>
    <version>${revision}</version>
</dependency>
```

### 2. 创建数据模型

```java
@Data
public class UserExcelModel {
    
    @ExcelExport(value = "用户ID", index = 0)
    @ExcelImport(value = "用户ID", index = 0, required = true)
    private Long userId;
    
    @ExcelExport(value = "用户名", index = 1)
    @ExcelImport(value = "用户名", index = 1, required = true)
    private String username;
    
    @ExcelExport(value = "邮箱", index = 2)
    @ExcelImport(value = "邮箱", index = 2, required = true, 
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
                errorMsg = "邮箱格式不正确")
    private String email;
}
```

### 3. 使用工具类

```java
// 导出 Excel
ExcelUtils.exportExcel(response, "用户数据.xlsx", "用户信息", dataList, UserExcelModel.class);

// 导入 Excel
List<UserExcelModel> dataList = ExcelUtils.importExcel(file, UserExcelModel.class);

// 生成模板
ExcelUtils.generateTemplate(response, "用户导入模板.xlsx", "用户信息", UserExcelModel.class);

// 验证导入数据
ExcelUtils.validateImportData(userModel);
```

## 注解说明

### @ExcelExport
用于标记需要导出的字段

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | "" | 列名 |
| index | int | -1 | 列索引 |
| ignore | boolean | false | 是否忽略该字段 |
| dateFormat | String | "yyyy-MM-dd HH:mm:ss" | 日期格式 |
| numberFormat | String | "#.##" | 数字格式 |
| width | int | 20 | 列宽 |
| nullable | boolean | true | 是否可为空 |

### @ExcelImport
用于标记需要导入的字段

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| value | String | "" | 列名 |
| index | int | -1 | 列索引 |
| required | boolean | false | 是否必填 |
| regexp | String | "" | 正则表达式验证 |
| errorMsg | String | "" | 验证失败提示信息 |

## 异常处理

组件提供了 `ExcelException` 异常类，继承自 `BusinessException`，可被全局异常处理器自动捕获：

```java
try {
    ExcelUtils.importExcel(file, UserExcelModel.class);
} catch (ExcelException e) {
    // 处理 Excel 相关异常
    log.error("Excel 处理失败: {}", e.getMessage());
}
```

## 最佳实践

1. **字段顺序**: 建议明确指定 `index` 属性来控制列的顺序
2. **数据验证**: 导入时务必调用 `validateImportData()` 进行数据验证
3. **大文件处理**: 组件支持流式处理，适合大文件导入导出
4. **模板生成**: 提供标准的导入模板，降低用户使用门槛
5. **异常处理**: 合理处理各种异常情况，提供友好的错误提示

## 性能优化

- 使用 FastExcel 底层库，性能优异
- 支持大数据量的流式处理
- 合理设置列宽和格式以减小文件大小