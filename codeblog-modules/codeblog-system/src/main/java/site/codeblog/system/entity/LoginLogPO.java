package site.codeblog.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import site.codeblog.system.dict.DictLabel;
import site.codeblog.system.dict.DictSetListener;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志表实体
 *
 * @author codeblog
 */
@Data
@Table(value = "sys_login_log", onSet = DictSetListener.class)
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
    @DictLabel("sys_login_status")
    private Integer status;

    /**
     * 结果展示（字典回写，非数据库字段）
     */
    @Column(ignore = true)
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
