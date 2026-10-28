package site.codeblog.user.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.BCrypt;
import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.codeblog.common.core.exception.BusinessException;
import site.codeblog.common.core.result.GlobalErrorCode;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.security.core.LoginHelper;
import site.codeblog.security.core.LoginUser;
import site.codeblog.user.convert.UserConvert;
import site.codeblog.user.entity.UserPO;
import site.codeblog.user.mapper.UserMapper;
import site.codeblog.user.service.UserService;
import site.codeblog.user.vo.*;

import java.time.LocalDateTime;
import java.util.List;

import static site.codeblog.user.entity.table.UserPOTableDef.USER_PO;

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

    private final UserConvert userConvert;

    @Override
    public AuthLoginRespVO login(AuthLoginReqVO reqVO) {
        UserPO user = userMapper.selectOneByQuery(QueryWrapper.create()
                .where(USER_PO.USERNAME.eq(reqVO.getUsername())));
        if (user == null || !BCrypt.checkpw(reqVO.getPassword(), user.getPassword())) {
            throw BusinessException.of(GlobalErrorCode.LOGIN_FAILED);
        }
        if (user.getStatus() != STATUS_ENABLED) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_DISABLED);
        }

        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
        String token = LoginHelper.login(loginUser);

        return AuthLoginRespVO.builder()
                .token(token)
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
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
        return userConvert.convert(user);
    }

    @Override
    public PageResult<UserRespVO> page(UserPageReqVO reqVO) {
        QueryWrapper query = QueryWrapper.create()
                .where(USER_PO.USERNAME.like(reqVO.getUsername(), StrUtil::isNotBlank))
                .and(USER_PO.NICKNAME.like(reqVO.getNickname(), StrUtil::isNotBlank))
                .and(USER_PO.STATUS.eq(reqVO.getStatus(), reqVO.getStatus() != null))
                .orderBy(USER_PO.ID.desc());

        var page = userMapper.paginate(reqVO.getPageNo(), reqVO.getPageSize(), query);
        List<UserRespVO> list = page.getRecords().stream().map(userConvert::convert).toList();
        return PageResult.of(list, page.getTotalRow());
    }

    @Override
    public UserRespVO get(Long id) {
        UserPO user = userMapper.selectOneById(id);
        if (user == null) {
            throw BusinessException.of(GlobalErrorCode.ACCOUNT_NOT_EXIST);
        }
        return userConvert.convert(user);
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

}
