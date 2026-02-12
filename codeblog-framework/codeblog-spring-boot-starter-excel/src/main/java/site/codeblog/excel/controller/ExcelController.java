package site.codeblog.excel.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import site.codeblog.excel.model.UserExcelModel;
import site.codeblog.excel.utils.ExcelUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Excel 控制器示例
 *
 * @author codeblog
 */
@Slf4j
@RestController
@RequestMapping("/excel")
@RequiredArgsConstructor
public class ExcelController {

    /**
     * 导出示例数据
     */
    @GetMapping("/export")
    public void exportExcel(HttpServletResponse response) {
        try {
            // 生成示例数据
            List<UserExcelModel> dataList = generateSampleData();
            
            // 导出 Excel
            ExcelUtils.exportExcel(response, "用户数据.xlsx", "用户信息", dataList, UserExcelModel.class);
            
        } catch (Exception e) {
            log.error("导出 Excel 失败", e);
            throw e;
        }
    }

    /**
     * 导入 Excel 数据
     */
    @PostMapping("/import")
    public String importExcel(@RequestParam("file") MultipartFile file) {
        try {
            // 导入 Excel 数据
            List<UserExcelModel> dataList = ExcelUtils.importExcel(file, UserExcelModel.class);
            
            // 验证数据
            for (UserExcelModel user : dataList) {
                ExcelUtils.validateImportData(user);
            }
            
            log.info("成功导入 {} 条数据", dataList.size());
            return "成功导入 " + dataList.size() + " 条数据";
            
        } catch (Exception e) {
            log.error("导入 Excel 失败", e);
            throw e;
        }
    }

    /**
     * 生成导入模板
     */
    @GetMapping("/template")
    public void generateTemplate(HttpServletResponse response) {
        try {
            ExcelUtils.generateTemplate(response, "用户导入模板.xlsx", "用户信息", UserExcelModel.class);
        } catch (Exception e) {
            log.error("生成模板失败", e);
            throw e;
        }
    }

    /**
     * 生成示例数据
     */
    private List<UserExcelModel> generateSampleData() {
        List<UserExcelModel> list = new ArrayList<>();
        
        for (int i = 1; i <= 10; i++) {
            UserExcelModel user = new UserExcelModel();
            user.setUserId((long) i);
            user.setUsername("用户" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPhone("1380013800" + (i % 10));
            user.setAge(20 + (i % 30));
            user.setBalance(new BigDecimal("1000.5" + (i % 10)));
            user.setCreateTime(new Date());
            user.setPassword("password" + i); // 这个字段会被忽略
            list.add(user);
        }
        
        return list;
    }
}