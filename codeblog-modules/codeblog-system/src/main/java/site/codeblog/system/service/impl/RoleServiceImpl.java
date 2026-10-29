package site.codeblog.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codeblog.common.core.exception.BusinessException;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.security.core.LoginHelper;
import site.codeblog.system.constant.CacheConstants;
import site.codeblog.system.constant.ErrorCodeConstants;
import site.codeblog.system.convert.RoleConvert;
import site.codeblog.system.entity.MenuPO;
import site.codeblog.system.entity.RolePO;
import site.codeblog.system.entity.RoleMenuPO;
import site.codeblog.system.entity.UserRolePO;
import site.codeblog.system.mapper.MenuMapper;
import site.codeblog.system.mapper.RoleMapper;
import site.codeblog.system.mapper.RoleMenuMapper;
import site.codeblog.system.mapper.UserRoleMapper;
import site.codeblog.system.service.RoleService;
import site.codeblog.system.vo.RoleCreateReqVO;
import site.codeblog.system.vo.RolePageReqVO;
import site.codeblog.system.vo.RoleRespVO;
import site.codeblog.system.vo.RoleUpdateReqVO;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static site.codeblog.system.entity.table.MenuPOTableDef.MENU_PO;
import static site.codeblog.system.entity.table.RoleMenuPOTableDef.ROLE_MENU_PO;
import static site.codeblog.system.entity.table.RolePOTableDef.ROLE_PO;
import static site.codeblog.system.entity.table.UserRolePOTableDef.USER_ROLE_PO;

/**
 * 角色服务实现
 *
 * @author codeblog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    /**
     * 超级管理员角色权限字符：拥有该角色的用户权限集合为 {"*"}（Sa-Token 通配匹配全部权限标识）
     */
    public static final String SUPER_ADMIN_ROLE_KEY = "admin";

    /**
     * 状态：启用
     */
    private static final int STATUS_ENABLED = 0;

    private final RoleMapper roleMapper;
    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleConvert roleConvert;
    private final CacheManager cacheManager;

    /**
     * 吊销某角色全部绑定用户的会话并失效其用户缓存（角色状态/权限变更后，
     * 旧快照权限不可继续生效，缓存与吊销必须同点联动）
     */
    private void revokeRoleUserSessions(Long roleId) {
        List<Long> userIds = userRoleMapper.selectListByQuery(QueryWrapper.create()
                        .where(USER_ROLE_PO.ROLE_ID.eq(roleId))).stream()
                .map(UserRolePO::getUserId).distinct().toList();
        userIds.forEach(LoginHelper::logoutByUserId);
        Cache userCache = cacheManager.getCache(CacheConstants.USER);
        if (userCache != null) {
            userIds.forEach(userCache::evict);
        }
        if (!userIds.isEmpty()) {
            log.info("角色变更已吊销 {} 个用户会话：roleId={}", userIds.size(), roleId);
        }
    }

    /** 失效角色列表缓存（角色增删改后调用；TTL 10 分钟兑底） */
    private void evictRoleListCache() {
        Cache cache = cacheManager.getCache(CacheConstants.ROLE_LIST);
        if (cache != null) {
            cache.clear();
        }
    }

    @Override
    public PageResult<RoleRespVO> page(RolePageReqVO reqVO) {
        QueryWrapper query = QueryWrapper.create()
                .where(ROLE_PO.ROLE_NAME.like(reqVO.getRoleName(), StrUtil::isNotBlank))
                .and(ROLE_PO.ROLE_KEY.like(reqVO.getRoleKey(), StrUtil::isNotBlank))
                .and(ROLE_PO.STATUS.eq(reqVO.getStatus(), reqVO.getStatus() != null))
                .orderBy(ROLE_PO.SORT_ORDER.asc(), ROLE_PO.ID.asc());

        var page = roleMapper.paginate(reqVO.getPageNo(), reqVO.getPageSize(), query);
        List<RoleRespVO> list = page.getRecords().stream().map(roleConvert::convert).toList();
        return PageResult.of(list, page.getTotalRow());
    }

    @Override
    @Cacheable(cacheNames = CacheConstants.ROLE_LIST)
    public List<RoleRespVO> listEnabled() {
        List<RolePO> roles = roleMapper.selectListByQuery(QueryWrapper.create()
                .where(ROLE_PO.STATUS.eq(STATUS_ENABLED))
                .orderBy(ROLE_PO.SORT_ORDER.asc(), ROLE_PO.ID.asc()));
        return roles.stream().map(roleConvert::convert).toList();
    }

    @Override
    public Long create(RoleCreateReqVO reqVO) {
        checkRoleKeyUnique(reqVO.getRoleKey(), null);

        RolePO role = roleConvert.convert(reqVO);
        role.setStatus(reqVO.getStatus() == null ? STATUS_ENABLED : reqVO.getStatus());
        role.setDelFlag(0);
        roleMapper.insert(role);
        evictRoleListCache();
        return role.getId();
    }

    @Override
    public void update(RoleUpdateReqVO reqVO) {
        RolePO exist = roleMapper.selectOneById(reqVO.getId());
        if (exist == null) {
            throw BusinessException.of(ErrorCodeConstants.ROLE_NOT_FOUND, "角色不存在");
        }
        if (SUPER_ADMIN_ROLE_KEY.equals(exist.getRoleKey())) {
            throw BusinessException.of(ErrorCodeConstants.BUILT_IN_ROLE, "内置超级管理员角色禁止修改");
        }
        checkRoleKeyUnique(reqVO.getRoleKey(), reqVO.getId());

        RolePO role = roleConvert.convert(reqVO);
        int rows = roleMapper.update(role);
        if (rows == 0) {
            throw BusinessException.of(GlobalErrorCode.CONFLICT, "角色已被他人修改，请刷新后重试");
        }
        evictRoleListCache();
        // 角色（含状态/权限字符）变更即吊销绑定用户：会话权限是登录快照
        revokeRoleUserSessions(reqVO.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        RolePO exist = roleMapper.selectOneById(id);
        if (exist == null) {
            throw BusinessException.of(ErrorCodeConstants.ROLE_NOT_FOUND, "角色不存在");
        }
        if (SUPER_ADMIN_ROLE_KEY.equals(exist.getRoleKey())) {
            throw BusinessException.of(ErrorCodeConstants.BUILT_IN_ROLE, "内置超级管理员角色禁止删除");
        }
        Long userCount = userRoleMapper.selectCountByQuery(QueryWrapper.create()
                .where(USER_ROLE_PO.ROLE_ID.eq(id)));
        if (userCount > 0) {
            throw BusinessException.of(ErrorCodeConstants.ROLE_IN_USE, "角色已绑定用户，请先解除绑定");
        }
        roleMenuMapper.deleteByQuery(QueryWrapper.create().where(ROLE_MENU_PO.ROLE_ID.eq(id)));
        roleMapper.deleteById(id);
        evictRoleListCache();
    }

    @Override
    public List<Long> getMenuIdsByRole(Long roleId) {
        requireRoleExists(roleId);
        List<RoleMenuPO> bindings = roleMenuMapper.selectListByQuery(QueryWrapper.create()
                .where(ROLE_MENU_PO.ROLE_ID.eq(roleId)));
        return bindings.stream().map(RoleMenuPO::getMenuId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignMenus(Long roleId, List<Long> menuIds) {
        requireRoleExists(roleId);
        List<Long> ids = menuIds == null ? Collections.<Long>emptyList() : menuIds.stream().distinct().toList();

        roleMenuMapper.deleteByQuery(QueryWrapper.create().where(ROLE_MENU_PO.ROLE_ID.eq(roleId)));
        if (ids.isEmpty()) {
            return;
        }
        List<RoleMenuPO> bindings = ids.stream().map(menuId -> {
            RoleMenuPO binding = new RoleMenuPO();
            binding.setRoleId(roleId);
            binding.setMenuId(menuId);
            binding.setCreateTime(LocalDateTime.now());
            return binding;
        }).toList();
        roleMenuMapper.insertBatch(bindings);
        // 授权变更即吊销绑定用户：旧快照权限不可继续生效
        revokeRoleUserSessions(roleId);
    }

    @Override
    public Set<String> getRoleKeysByUserId(Long userId) {
        List<RolePO> roles = listEnabledRolesByUserId(userId);
        Set<String> roleKeys = new LinkedHashSet<>();
        for (RolePO role : roles) {
            roleKeys.add(role.getRoleKey());
        }
        return roleKeys;
    }

    @Override
    public Map<Long, Set<String>> getRoleKeyMapByUserIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserRolePO> bindings = userRoleMapper.selectListByQuery(QueryWrapper.create()
                .where(USER_ROLE_PO.USER_ID.in(userIds)));
        if (bindings.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Long, List<Long>> userRoleIds = bindings.stream().collect(Collectors.groupingBy(
                UserRolePO::getUserId, Collectors.mapping(UserRolePO::getRoleId, Collectors.toList())));
        List<Long> roleIds = bindings.stream().map(UserRolePO::getRoleId).distinct().toList();
        Map<Long, RolePO> roleById = roleMapper.selectListByIds(roleIds).stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == STATUS_ENABLED)
                .collect(Collectors.toMap(RolePO::getId, r -> r));

        Map<Long, Set<String>> result = new HashMap<>();
        userRoleIds.forEach((userId, ids) -> result.put(userId, ids.stream()
                .map(roleById::get)
                .filter(Objects::nonNull)
                .map(RolePO::getRoleKey)
                .collect(Collectors.toCollection(LinkedHashSet::new))));
        return result;
    }

    @Override
    public Set<String> getPermissionsByUserId(Long userId) {
        List<RolePO> roles = listEnabledRolesByUserId(userId);
        boolean superAdmin = roles.stream().anyMatch(r -> SUPER_ADMIN_ROLE_KEY.equals(r.getRoleKey()));
        if (superAdmin) {
            return Set.of("*");
        }
        if (roles.isEmpty()) {
            return Collections.emptySet();
        }

        List<Long> roleIds = roles.stream().map(RolePO::getId).toList();
        List<RoleMenuPO> roleMenus = roleMenuMapper.selectListByQuery(QueryWrapper.create()
                .where(ROLE_MENU_PO.ROLE_ID.in(roleIds)));
        if (roleMenus.isEmpty()) {
            return Collections.emptySet();
        }
        List<Long> menuIds = roleMenus.stream().map(RoleMenuPO::getMenuId).distinct().toList();
        List<MenuPO> menus = menuMapper.selectListByIds(menuIds);

        Set<String> permissions = new LinkedHashSet<>();
        for (MenuPO menu : menus) {
            if (menu.getStatus() != null && menu.getStatus() == STATUS_ENABLED
                    && StrUtil.isNotBlank(menu.getPermission())) {
                permissions.add(menu.getPermission());
            }
        }
        return permissions;
    }

    private List<RolePO> listEnabledRolesByUserId(Long userId) {
        List<UserRolePO> userRoles = userRoleMapper.selectListByQuery(QueryWrapper.create()
                .where(USER_ROLE_PO.USER_ID.eq(userId)));
        if (userRoles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> roleIds = userRoles.stream().map(UserRolePO::getRoleId).distinct().toList();
        return roleMapper.selectListByIds(roleIds).stream()
                .filter(r -> r.getStatus() != null && r.getStatus() == STATUS_ENABLED)
                .toList();
    }

    private void requireRoleExists(Long roleId) {
        if (roleMapper.selectOneById(roleId) == null) {
            throw BusinessException.of(ErrorCodeConstants.ROLE_NOT_FOUND, "角色不存在");
        }
    }

    private void checkRoleKeyUnique(String roleKey, Long excludeId) {
        Long count = roleMapper.selectCountByQuery(QueryWrapper.create()
                .where(ROLE_PO.ROLE_KEY.eq(roleKey))
                .and(ROLE_PO.ID.ne(excludeId, excludeId != null)));
        if (count > 0) {
            throw BusinessException.of(ErrorCodeConstants.ROLE_KEY_DUPLICATE, "角色权限字符已存在");
        }
    }

}
