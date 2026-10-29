package site.codeblog.system.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 角色菜单关联表实体（联合主键，无自增 ID）
 *
 * @author codeblog
 */
@Data
@Table("sys_role_menu")
public class RoleMenuPO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 菜单 ID
     */
    private Long menuId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
