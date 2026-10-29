package site.codeblog.system.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 登录响应
 *
 * @author codeblog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginRespVO {

    /**
     * 访问令牌（请求头 Authorization: Bearer <token>）
     */
    private String token;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 角色权限字符集合
     */
    @Builder.Default
    private Set<String> roles = new LinkedHashSet<>();

    /**
     * 权限标识集合（admin 为 {"*"}）
     */
    @Builder.Default
    private Set<String> permissions = new LinkedHashSet<>();

}
