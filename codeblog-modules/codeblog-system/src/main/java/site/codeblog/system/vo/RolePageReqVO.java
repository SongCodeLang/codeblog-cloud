package site.codeblog.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.result.PageParam;

/**
 * 角色分页查询请求
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RolePageReqVO extends PageParam {

    /**
     * 角色名称（模糊匹配）
     */
    private String roleName;

    /**
     * 角色权限字符（模糊匹配）
     */
    private String roleKey;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

}
