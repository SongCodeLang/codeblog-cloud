package site.codeblog.user.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户请求
 *
 * @author codeblog
 */
@Data
public class UserUpdateReqVO {

    @NotNull(message = "用户 ID 不能为空")
    private Long id;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称最长 30 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长 100 个字符")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

    /**
     * 状态：0 启用，1 停用
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

}
