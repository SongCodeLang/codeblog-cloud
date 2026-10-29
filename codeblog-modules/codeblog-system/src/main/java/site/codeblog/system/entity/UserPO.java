package site.codeblog.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.mybatis.core.domain.BaseEntity;
import site.codeblog.mybatis.core.listener.AuditListener;
import site.codeblog.system.dict.DictLabel;
import site.codeblog.system.dict.DictSetListener;

import java.io.Serial;

/**
 * 用户表实体
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "sys_user", onInsert = AuditListener.class, onUpdate = AuditListener.class, onSet = DictSetListener.class)
public class UserPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID（自增）
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 用户名（登录账号）
     */
    private String username;

    /**
     * 密码（BCrypt）
     */
    private String password;

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
    @DictLabel("sys_common_status")
    private Integer status;

    /**
     * 状态展示（字典回写，非数据库字段）
     */
    @Column(ignore = true)
    private String statusLabel;

    /**
     * 逻辑删除：0 正常，1 已删除
     */
    @Column(isLogicDelete = true)
    private Integer delFlag;

    /**
     * 乐观锁版本号（更新条件携带，冲突时影响行数为 0）
     */
    @Column(version = true)
    private Integer version;

}
