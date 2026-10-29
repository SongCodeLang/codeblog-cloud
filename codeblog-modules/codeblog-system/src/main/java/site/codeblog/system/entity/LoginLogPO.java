package site.codeblog.system.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志表实体
 *
 * @author codeblog
 */
@Data
@Table("sys_login_log")
public class LoginLogPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 日志 ID（自增）
     */
    @Id(keyType = KeyType.Auto)
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
     * 提示消息
     */
    private String msg;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

}
