package site.codeblog.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志响应
 *
 * @author codeblog
 */
@Data
public class LoginLogRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID
     */
    private Long id;

    /**
     * 登录账号
     */
    private String username;

    /**
     * 登录 IP
     */
    private String ip;

    /**
     * 浏览器标识
     */
    private String userAgent;

    /**
     * 结果：0 成功，1 失败
     */
    private Integer status;

    /**
     * 结果展示（字典回写）
     */
    private String statusLabel;

    /**
     * 提示消息
     */
    private String msg;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

}
