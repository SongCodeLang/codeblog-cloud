package site.codeblog.user.vo;

import lombok.Data;

import java.time.LocalDateTime;

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

}
