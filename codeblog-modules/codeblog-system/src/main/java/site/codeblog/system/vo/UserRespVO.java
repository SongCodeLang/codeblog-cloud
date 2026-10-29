package site.codeblog.system.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 用户信息响应
 *
 * @author codeblog
 */
@Data
public class UserRespVO {

    /**
     * 用户 ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 角色权限字符集合（admin 角色用户为 {"admin"}）
     */
    private Set<String> roles = new LinkedHashSet<>();

    /**
     * 权限标识集合（admin 角色用户为 {"*"}，前端据此做按钮/页面级控制）
     */
    private Set<String> permissions = new LinkedHashSet<>();

}
