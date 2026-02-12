package site.codeblog.mybatis.util;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Map;

/**
 * MyBatis-Flex 工具类
 * 提供常用的工具方法
 *
 * @author codeblog
 */
@Slf4j
public class MybatisFlexUtils {

    /**
     * 创建查询包装器
     * @param entityClass 实体类
     * @param <T> 实体类型
     * @return 查询包装器
     */
    public static <T> QueryWrapper createQuery(Class<T> entityClass) {
        return QueryWrapper.create(entityClass);
    }

    /**
     * 创建空的查询包装器
     * @return 查询包装器
     */
    public static QueryWrapper createEmptyQuery() {
        return QueryWrapper.create();
    }

    /**
     * 构建LIKE查询条件
     * @param column 字段名
     * @param value 查询值
     * @return LIKE条件字符串
     */
    public static String buildLikeCondition(String column, String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        return column + " LIKE '%" + value.trim() + "%'";
    }

    /**
     * 构建IN查询条件
     * @param column 字段名
     * @param values 值集合
     * @return IN条件字符串
     */
    public static String buildInCondition(String column, Collection<?> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(column).append(" IN (");
        boolean first = true;
        for (Object value : values) {
            if (!first) {
                sb.append(",");
            }
            sb.append("'").append(value).append("'");
            first = false;
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 格式化SQL语句
     * @param sql 原始SQL
     * @return 格式化后的SQL
     */
    public static String formatSql(String sql) {
        if (!StringUtils.hasText(sql)) {
            return "";
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 安全获取查询结果数量
     * @param result 查询结果
     * @return 结果数量
     */
    public static int getResultCount(Object result) {
        if (result == null) {
            return 0;
        }
        if (result instanceof Collection) {
            return ((Collection<?>) result).size();
        }
        if (result instanceof Map) {
            return ((Map<?, ?>) result).size();
        }
        return 1;
    }

    /**
     * 构建分页查询的OFFSET和LIMIT
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return OFFSET和LIMIT数组 [offset, limit]
     */
    public static int[] buildPageOffset(int pageNum, int pageSize) {
        if (pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize <= 0) {
            pageSize = 10;
        }
        int offset = (pageNum - 1) * pageSize;
        return new int[]{offset, pageSize};
    }

    /**
     * 验证实体对象是否有效
     * @param entity 实体对象
     * @param requiredFields 必填字段
     * @return 验证结果
     */
    public static ValidationResult validateEntity(Object entity, String... requiredFields) {
        ValidationResult result = new ValidationResult();
        
        if (entity == null) {
            result.setValid(false);
            result.addError("Entity cannot be null");
            return result;
        }

        // 这里可以添加更复杂的验证逻辑
        // 比如使用反射检查必填字段是否为空
        
        result.setValid(true);
        return result;
    }

    /**
     * 验证结果类
     */
    public static class ValidationResult {
        private boolean valid;
        private StringBuilder errors = new StringBuilder();

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public void addError(String error) {
            if (errors.length() > 0) {
                errors.append("; ");
            }
            errors.append(error);
        }

        public String getErrors() {
            return errors.toString();
        }

        public boolean hasErrors() {
            return errors.length() > 0;
        }
    }
}