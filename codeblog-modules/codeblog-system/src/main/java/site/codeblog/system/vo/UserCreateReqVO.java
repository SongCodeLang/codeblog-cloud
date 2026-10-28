package site.codeblog.system.vo;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建用户请求
 *
 * @author codeblog
 */
@Data
public class UserCreateReqVO {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度需在 3~30 位之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名仅支持字母、数字、下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6~32 位之间")
    private String password;

    @NotBlank(message = "昵称不能为空")
    @Size(max = 30, message = "昵称最长 30 个字符")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱最长 100 个字符")
    private String email;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String mobile;

}
