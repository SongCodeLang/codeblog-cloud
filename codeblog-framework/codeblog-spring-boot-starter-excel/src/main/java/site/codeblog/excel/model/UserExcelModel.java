package site.codeblog.excel.model;

import lombok.Data;
import site.codeblog.excel.annotation.ExcelExport;
import site.codeblog.excel.annotation.ExcelImport;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户 Excel 模型示例
 *
 * @author codeblog
 */
@Data
public class UserExcelModel {

    @ExcelExport(value = "用户ID", index = 0, width = 15)
    @ExcelImport(value = "用户ID", index = 0, required = true)
    private Long userId;

    @ExcelExport(value = "用户名", index = 1, width = 20)
    @ExcelImport(value = "用户名", index = 1, required = true)
    private String username;

    @ExcelExport(value = "邮箱", index = 2, width = 25)
    @ExcelImport(value = "邮箱", index = 2, required = true, 
                regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", 
                errorMsg = "邮箱格式不正确")
    private String email;

    @ExcelExport(value = "手机号", index = 3, width = 15)
    @ExcelImport(value = "手机号", index = 3, required = true,
                regexp = "^1[3-9]\\d{9}$",
                errorMsg = "手机号格式不正确")
    private String phone;

    @ExcelExport(value = "年龄", index = 4, width = 10)
    @ExcelImport(value = "年龄", index = 4)
    private Integer age;

    @ExcelExport(value = "余额", index = 5, width = 15, numberFormat = "#.##")
    @ExcelImport(value = "余额", index = 5)
    private BigDecimal balance;

    @ExcelExport(value = "注册时间", index = 6, width = 20, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    // 忽略字段示例
    @ExcelExport(ignore = true)
    private String password;
}