package site.codeblog.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.result.PageParam;

/**
 * 字典项分页查询请求
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DictItemPageReqVO extends PageParam {

    /**
     * 所属字典类型编码（管理页必填，用于隔离项列表）
     */
    private String dictType;

    /**
     * 字典标签（模糊匹配）
     */
    private String itemLabel;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

}
