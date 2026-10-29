package site.codeblog.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.result.PageParam;

/**
 * 字典类型分页查询请求
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictTypePageReqVO extends PageParam {

    /**
     * 字典名称（模糊匹配）
     */
    private String dictName;

    /**
     * 字典类型编码（模糊匹配）
     */
    private String dictType;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

}
