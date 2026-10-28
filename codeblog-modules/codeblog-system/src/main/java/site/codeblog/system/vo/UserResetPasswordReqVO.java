package site.codeblog.system.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 重置密码请求
 *
 * @author codeblog
 */
@Data
public class UserResetPasswordReqVO {

    @NotNull(message = "用户 ID 不能为空")
    private Long id;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度需在 6~32 位之间")
    private String password;

}
