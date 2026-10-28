package site.codeblog.security.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * 登录用户信息（存于 Sa-Token 会话中）
 *
 * @author codeblog
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 昵称（展示名）
     */
    private String nickname;

    /**
     * 角色标识集合
     */
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    /**
     * 权限标识集合
     */
    @Builder.Default
    private Set<String> permissions = new HashSet<>();

}
