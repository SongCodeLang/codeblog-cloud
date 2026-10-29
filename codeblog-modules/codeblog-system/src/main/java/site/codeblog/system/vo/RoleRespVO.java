package site.codeblog.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色信息响应
 *
 * @author codeblog
 */
@Data
public class RoleRespVO {

    /**
     * 角色 ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符
     */
    private String roleKey;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
