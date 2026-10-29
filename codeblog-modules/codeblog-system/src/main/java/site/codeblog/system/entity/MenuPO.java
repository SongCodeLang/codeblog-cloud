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
 * 菜单权限表实体
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "sys_menu", onInsert = AuditListener.class, onUpdate = AuditListener.class, onSet = DictSetListener.class)
public class MenuPO extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单 ID（自增）
     */
    @Id(keyType = KeyType.Auto)
    private Long id;

    /**
     * 父菜单 ID（0=根）
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 类型：1 目录，2 菜单，3 按钮
     */
    private String menuType;

    /**
     * 权限标识（如 system:user:list）
     */
    private String permission;

    /**
     * 路由地址（目录/菜单）
     */
    private String path;

    /**
     * 图标（目录/菜单）
     */
    private String icon;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

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
