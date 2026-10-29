package site.codeblog.system.entity;

import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户角色关联表实体（联合主键，无自增 ID）
 *
 * @author codeblog
 */
@Data
@Table("sys_user_role")
public class UserRolePO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 角色 ID
     */
    private Long roleId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
