package site.codeblog.system.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.mybatis.core.domain.BaseEntity;
import site.codeblog.mybatis.core.listener.AuditListener;

import java.io.Serial;

/**
 * 角色表实体
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "sys_role", onInsert = AuditListener.class, onUpdate = AuditListener.class)
public class RolePO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID（自增）
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色权限字符（如 admin）
     */
    private String roleKey;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

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
