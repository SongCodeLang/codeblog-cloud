package site.codeblog.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.codeblog.common.core.exception.BusinessException;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.redis.core.RedisCache;
import site.codeblog.security.core.LoginHelper;
import site.codeblog.security.core.LoginUser;
import site.codeblog.system.constant.ErrorCodeConstants;
import site.codeblog.system.convert.UserConvert;
import site.codeblog.system.entity.UserPO;
import site.codeblog.system.entity.UserRolePO;
import site.codeblog.system.mapper.UserMapper;
import site.codeblog.system.mapper.UserRoleMapper;
import site.codeblog.system.mq.LoginEventPublisher;
import site.codeblog.system.service.LoginLogService;
import site.codeblog.system.service.RoleService;
import site.codeblog.system.service.UserService;
import site.codeblog.system.vo.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

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
    /** 登录事件发布（ObjectProvider：local/关闭时 bean 不存在，零依赖） */
    private final ObjectProvider<LoginEventPublisher> loginEventPublisher;

    // ---- 登录失败锁定（Redis 计数，防暴力破解；key 规范待阶段5 CacheConstants 统一收口） ----
    private static final String LOGIN_FAIL_COUNT_KEY = "codeblog:system:auth:login-fail:%s";
    private static final String LOGIN_LOCK_KEY = "codeblog:system:auth:login-lock:%s";
    /** 失败计数窗口（分钟）：窗口内累计达阈值即锁定 */
    private static final long FAIL_WINDOW_MINUTES = 10;
    /** 窗口内允许的最大失败次数 */
    private static final long MAX_FAILS = 5;
    /** 锁定时长（分钟） */
    private static final long LOCK_MINUTES = 10;

    private final RedisCache redisCache;

    /** 登录前检查锁定状态，已锁定抛 1000007 */
    private void checkLoginLock(String username) {
        if (Boolean.TRUE.equals(redisCache.hasKey(String.format(LOGIN_LOCK_KEY, username)))) {
            throw BusinessException.of(ErrorCodeConstants.LOGIN_LOCKED, "登录失败次数过多，账号已临时锁定，请稍后再试");
        }
    }

    /** 登录失败计数，达阈值后锁定并清零计数 */
    private void recordLoginFail(String username) {
        String countKey = String.format(LOGIN_FAIL_COUNT_KEY, username);
        Long fails = redisCache.increment(countKey, 1);
        if (fails != null && fails == 1) {
            redisCache.expire(countKey, FAIL_WINDOW_MINUTES, TimeUnit.MINUTES);
        }
        if (fails != null && fails >= MAX_FAILS) {
            redisCache.set(String.format(LOGIN_LOCK_KEY, username), "1", LOCK_MINUTES, TimeUnit.MINUTES);
            redisCache.delete(countKey);
            log.warn("登录失败锁定：username={}, fails={}", username, fails);
        }
    }

    /** 登录成功清零计数（锁定 key 到期自然解除，无需主动清） */
    private void clearLoginFail(String username) {
        redisCache.delete(String.format(LOGIN_FAIL_COUNT_KEY, username));
    }

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO) {
        try {
            checkLoginLock(reqVO.getUsername());
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
            clearLoginFail(user.getUsername());
            loginEventPublisher.ifAvailable(p -> p.publish(user.getUsername(), true));
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
            recordLoginFail(reqVO.getUsername());
            loginEventPublisher.ifAvailable(p -> p.publish(reqVO.getUsername(), false));
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
        // 角色批量查询：两条 IN 拿全页角色，避免逐行 N+1
        Map<Long, Set<String>> roleKeyMap = roleService.getRoleKeyMapByUserIds(
                page.getRecords().stream().map(UserPO::getId).toList());
        List<UserRespVO> list = page.getRecords().stream()
                .map(po -> {
                    UserRespVO respVO = userConvert.convert(po);
                    // 列表仅展示角色标识，权限集合不查（/auth/me 提供）
                    respVO.setRoles(roleKeyMap.getOrDefault(po.getId(), Set.of()));
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

        // 停用即吊销全部会话：防止旧 token 凭会话快照继续访问
        if (!Integer.valueOf(STATUS_ENABLED).equals(reqVO.getStatus())) {
            LoginHelper.logoutByUserId(reqVO.getId());
        }
    }

    @Override
    public void delete(Long id) {
        if (id.equals(LoginHelper.getUserId())) {
            throw BusinessException.of(GlobalErrorCode.BUSINESS_FAILED, "不能删除当前登录账号");
        }
        userMapper.deleteById(id);
        // 删号即吊销：token 30 天有效期不等于账号已删后仍可用
        LoginHelper.logoutByUserId(id);
    }

    @Override
    public void resetPassword(UserResetPasswordReqVO reqVO) {
        UserPO user = new UserPO();
        user.setId(reqVO.getId());
        user.setPassword(BCrypt.hashpw(reqVO.getPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        // 重置密码即吊销：旧会话一律失效，凭新密码重新登录
        LoginHelper.logoutByUserId(reqVO.getId());
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
        // 角色变更即吊销：会话权限是登录快照，不吊销则旧权限残留到下次登录
        LoginHelper.logoutByUserId(userId);
    }

    private void requireUserExists(Long userId) {
        if (userMapper.selectOneById(userId) == null) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_NOT_EXIST);
        }
    }

}
