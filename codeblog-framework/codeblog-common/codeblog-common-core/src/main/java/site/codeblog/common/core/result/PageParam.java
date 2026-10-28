package site.codeblog.common.core.result;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分页查询基础参数
 *
 * @author codeblog
 */
@Data
public class PageParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 默认页码
     */
    private static final int DEFAULT_PAGE_NO = 1;

    /**
     * 默认每页条数
     */
    private static final int DEFAULT_PAGE_SIZE = 10;

    /**
     * 页码，从 1 开始
     */
    @Min(value = 1, message = "页码最小为 1")
    private Integer pageNo = DEFAULT_PAGE_NO;

    /**
     * 每页条数，1 ~ 100
     */
    @Min(value = 1, message = "每页条数最小为 1")
    @Max(value = 100, message = "每页条数最大为 100")
    private Integer pageSize = DEFAULT_PAGE_SIZE;

    /**
     * 获取偏移量（供 LIMIT/OFFSET 使用）
     */
    public int getOffset() {
        return (pageNo - 1) * pageSize;
    }

}
