package site.codeblog.system.service;

import site.codeblog.common.core.result.PageResult;
import site.codeblog.system.vo.*;

/**
 * 用户服务接口
 *
 * @author codeblog
 */
public interface UserService {

    /**
     * 登录
     *
     * @return token 与用户基础信息
     */
    AuthLoginRespVO login(AuthLoginReqVO reqVO);

    /**
     * 登出当前会话
     */
    void logout();

    /**
     * 当前登录用户信息
     */
    UserRespVO currentUser();

    /**
     * 分页查询用户
     */
    PageResult<UserRespVO> page(UserPageReqVO reqVO);

    /**
     * 查询用户详情
     */
    UserRespVO get(Long id);

    /**
     * 创建用户
     *
     * @return 用户 ID
     */
    Long create(UserCreateReqVO reqVO);

    /**
     * 更新用户信息
     */
    void update(UserUpdateReqVO reqVO);

    /**
     * 删除用户（逻辑删除）
     */
    void delete(Long id);

    /**
     * 重置用户密码
     */
    void resetPassword(UserResetPasswordReqVO reqVO);

}
