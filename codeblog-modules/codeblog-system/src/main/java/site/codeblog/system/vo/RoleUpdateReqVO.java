package site.codeblog.system.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新角色请求
 *
 * @author codeblog
 */
@Data
public class RoleUpdateReqVO {

    @NotNull(message = "角色 ID 不能为空")
    private Long id;

    @NotBlank(message = "角色名称不能为空")
    @Size(max = 30, message = "角色名称最长 30 个字符")
    private String roleName;

    @NotBlank(message = "角色权限字符不能为空")
    @Size(max = 60, message = "角色权限字符最长 60 个字符")
    @Pattern(regexp = "^[a-zA-Z0-9_:]+$", message = "权限字符仅支持字母、数字、下划线、冒号")
    private String roleKey;

    private Integer sortOrder;

    private Integer status;

    @Size(max = 200, message = "备注最长 200 个字符")
    private String remark;

}
