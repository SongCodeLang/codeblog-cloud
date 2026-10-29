package site.codeblog.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codeblog.common.core.exception.BusinessException;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.security.core.LoginHelper;
import site.codeblog.security.core.LoginUser;
import site.codeblog.system.convert.UserConvert;
import site.codeblog.system.entity.UserPO;
import site.codeblog.system.entity.UserRolePO;
import site.codeblog.system.mapper.UserMapper;
import site.codeblog.system.mapper.UserRoleMapper;
import site.codeblog.system.service.LoginLogService;
import site.codeblog.system.service.RoleService;
import site.codeblog.system.service.UserService;
import site.codeblog.system.vo.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static site.codeblog.system.entity.table.UserPOTableDef.USER_PO;
import static site.codeblog.system.entity.table.UserRolePOTableDef.USER_ROLE_PO;

/**
 * 用户服务实现
 *
 * @author codeblog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * 状态：启用
     */
    private static final int STATUS_ENABLED = 0;

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserConvert userConvert;
    private final RoleService roleService;
    private final LoginLogService loginLogService;

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO) {
        try {
            UserPO user = userMapper.selectOneByQuery(QueryWrapper.create()
                    .where(USER_PO.USERNAME.eq(reqVO.getUsername())));
            if (user == null || !BCrypt.checkpw(reqVO.getPassword(), user.getPassword())) {
                throw BusinessException.of(GlobalErrorCode.LOGIN_FAILED);
            }
            if (user.getStatus() != STATUS_ENABLED) {
                throw BusinessException.of(GlobalErrorCode.ACCOUNT_DISABLED);
            }

            // 登录即装配角色/权限：会话是 StpInterface 注解鉴权的唯一数据源
            Set<String> roleKeys = roleService.getRoleKeysByUserId(user.getId());
            Set<String> permissions = roleService.getPermissionsByUserId(user.getId());

            LoginUser loginUser = LoginUser.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .roles(roleKeys)
                    .permissions(permissions)
                    .build();
            String token = LoginHelper.login(loginUser);

            loginLogService.record(user.getUsername(), true, "登录成功");
            return AuthLoginRespVO.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .roles(roleKeys)
                    .permissions(permissions)
                    .build();
        } catch (BusinessException e) {
            loginLogService.record(reqVO.getUsername(), false, e.getMessage());
            throw e;
        }
    }

    @Override
    public void logout() {
        LoginHelper.logout();
    }

    @Override
    public UserRespVO currentUser() {
        Long userId = LoginHelper.getUserId();
        UserPO user = userMapper.selectOneById(userId);
        if (user == null) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_NOT_EXIST);
        }
        UserRespVO respVO = userConvert.convert(user);
        respVO.setRoles(roleService.getRoleKeysByUserId(userId));
        respVO.setPermissions(roleService.getPermissionsByUserId(userId));
        return respVO;
    }

    @Override
    public PageResult<UserRespVO> page(UserPageReqVO reqVO) {
        QueryWrapper query = QueryWrapper.create()
                .where(USER_PO.USERNAME.like(reqVO.getUsername(), StrUtil::isNotBlank))
                .and(USER_PO.NICKNAME.like(reqVO.getNickname(), StrUtil::isNotBlank))
                .and(USER_PO.STATUS.eq(reqVO.getStatus(), reqVO.getStatus() != null))
                .orderBy(USER_PO.ID.desc());

        var page = userMapper.paginate(reqVO.getPageNo(), reqVO.getPageSize(), query);
        List<UserRespVO> list = page.getRecords().stream()
                .map(po -> {
                    UserRespVO respVO = userConvert.convert(po);
                    // 列表仅展示角色标识，权限集合不查（/auth/me 提供）
                    respVO.setRoles(roleService.getRoleKeysByUserId(po.getId()));
                    return respVO;
                })
                .toList();
        return PageResult.of(list, page.getTotalRow());
    }

    @Override
    public UserRespVO get(Long id) {
        UserPO user = userMapper.selectOneById(id);
        if (user == null) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_NOT_EXIST);
        }
        UserRespVO respVO = userConvert.convert(user);
        respVO.setRoles(roleService.getRoleKeysByUserId(id));
        return respVO;
    }

    @Override
    public Long create(UserCreateReqVO reqVO) {
        Long count = userMapper.selectCountByQuery(QueryWrapper.create()
                .where(USER_PO.USERNAME.eq(reqVO.getUsername())));
        if (count > 0) {
            throw BusinessException.of(GlobalErrorCode.DUPLICATE_USERNAME);
        }

        UserPO user = userConvert.convert(reqVO);
        user.setPassword(BCrypt.hashpw(reqVO.getPassword()));
        user.setStatus(STATUS_ENABLED);
        user.setDelFlag(0);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void update(UserUpdateReqVO reqVO) {
        UserPO exist = userMapper.selectOneById(reqVO.getId());
        if (exist == null) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_NOT_EXIST);
        }
        // 停用自己时拒绝，避免误操作把自己锁在门外
        if (!Integer.valueOf(STATUS_ENABLED).equals(reqVO.getStatus())
                && reqVO.getId().equals(LoginHelper.getUserId())) {
            throw BusinessException.of(GlobalErrorCode.BUSINESS_FAILED, "不能停用当前登录账号");
        }

        UserPO user = userConvert.convert(reqVO);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public void delete(Long id) {
        if (id.equals(LoginHelper.getUserId())) {
            throw BusinessException.of(GlobalErrorCode.BUSINESS_FAILED, "不能删除当前登录账号");
        }
        userMapper.deleteById(id);
    }

    @Override
    public void resetPassword(UserResetPasswordReqVO reqVO) {
        UserPO user = new UserPO();
        user.setId(reqVO.getId());
        user.setPassword(BCrypt.hashpw(reqVO.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
    }

    @Override
    public List<Long> getRoleIds(Long userId) {
        requireUserExists(userId);
        List<UserRolePO> bindings = userRoleMapper.selectListByQuery(QueryWrapper.create()
                .where(USER_ROLE_PO.USER_ID.eq(userId)));
        return bindings.stream().map(UserRolePO::getRoleId).toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        requireUserExists(userId);
        List<Long> ids = roleIds == null ? Collections.<Long>emptyList() : roleIds.stream().distinct().toList();

        userRoleMapper.deleteByQuery(QueryWrapper.create().where(USER_ROLE_PO.USER_ID.eq(userId)));
        if (ids.isEmpty()) {
            return;
        }
        List<UserRolePO> bindings = ids.stream().map(roleId -> {
            UserRolePO binding = new UserRolePO();
            binding.setUserId(userId);
            binding.setRoleId(roleId);
            binding.setCreateTime(LocalDateTime.now());
            return binding;
        }).toList();
        userRoleMapper.insertBatch(bindings);
    }

    private void requireUserExists(Long userId) {
        if (userMapper.selectOneById(userId) == null) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_NOT_EXIST);
        }
    }

}
