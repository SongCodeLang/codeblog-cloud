package site.codeblog.system.service;

import site.codeblog.common.core.result.PageResult;
import site.codeblog.system.vo.RoleCreateReqVO;
import site.codeblog.system.vo.RolePageReqVO;
import site.codeblog.system.vo.RoleRespVO;
import site.codeblog.system.vo.RoleUpdateReqVO;

import java.util.List;
import java.util.Set;

/**
 * 角色服务接口
 *
 * @author codeblog
 */
public interface RoleService {

    /**
     * 分页查询角色
     */
    PageResult<RoleRespVO> page(RolePageReqVO reqVO);

    /**
     * 全部启用角色（用户分配角色下拉用）
     */
    List<RoleRespVO> listEnabled();

    /**
     * 创建角色
     *
     * @return 角色 ID
     */
    Long create(RoleCreateReqVO reqVO);

    /**
     * 更新角色
     */
    void update(RoleUpdateReqVO reqVO);

    /**
     * 删除角色（逻辑删除，需先解除用户绑定）
     */
    void delete(Long id);

    /**
     * 查询角色已绑定的菜单 ID 列表
     */
    List<Long> getMenuIdsByRole(Long roleId);

    /**
     * 覆盖式保存角色的菜单绑定
     */
    void assignMenus(Long roleId, List<Long> menuIds);

    /**
     * 查询用户的角色权限字符集合（仅启用角色）
     */
    Set<String> getRoleKeysByUserId(Long userId);

    /**
     * 查询用户的权限标识集合；admin 角色返回 {"*"}
     */
    Set<String> getPermissionsByUserId(Long userId);

}
