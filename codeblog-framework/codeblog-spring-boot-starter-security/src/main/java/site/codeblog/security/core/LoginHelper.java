package site.codeblog.security.core;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;

/**
 * 登录用户存取工具
 * <p>
 * 登录用户信息存放于 Sa-Token 会话（配合 Redis 可实现分布式会话），
 * 业务代码通过本工具获取当前登录用户，避免直接依赖 Sa-Token API。
 *
 * @author codeblog
 */
public final class LoginHelper {

    /**
     * 会话中存放登录用户的 key
     */
    private static final String LOGIN_USER_KEY = "loginUser";

    private LoginHelper() {
    }

    /**
     * 登录：为指定用户生成 token 并写入会话
     *
     * @return 本次登录的 token
     */
    public static String login(LoginUser loginUser) {
        StpUtil.login(loginUser.getUserId());
        StpUtil.getSession().set(LOGIN_USER_KEY, loginUser);
        return StpUtil.getTokenValue();
    }

    /**
     * 登出当前用户
     */
    public static void logout() {
        StpUtil.logout();
    }

    /**
     * 吊销指定用户的全部会话（删号/停用/重置密码/权限变更后调用，
     * 防止旧会话凭快照权限继续访问）
     */
    public static void logoutByUserId(Object loginId) {
        StpUtil.logout(loginId);
    }

    /**
     * 获取当前登录用户（未登录抛出 NotLoginException）
     */
    public static LoginUser getLoginUser() {
        SaSession session = StpUtil.getSession();
        LoginUser loginUser = (LoginUser) session.get(LOGIN_USER_KEY);
        if (loginUser == null) {
            throw new IllegalStateException("登录会话数据缺失，请重新登录");
        }
        return loginUser;
    }

    /**
     * 获取当前登录用户（未登录返回 null）
     */
    public static LoginUser getLoginUserOrNull() {
        if (!StpUtil.isLogin()) {
            return null;
        }
        return (LoginUser) StpUtil.getSession().get(LOGIN_USER_KEY);
    }

    /**
     * 按登录 ID 获取会话中的登录用户（会话不存在返回 null）。
     * 供 StpInterface 注解鉴权回调使用（入参是 loginId 而非当前线程）。
     */
    public static LoginUser getLoginUserByLoginId(Object loginId) {
        SaSession session = StpUtil.getSessionByLoginId(loginId, false);
        if (session == null) {
            return null;
        }
        return (LoginUser) session.get(LOGIN_USER_KEY);
    }

    /**
     * 获取当前登录用户 ID（未登录抛出 NotLoginException）
     */
    public static Long getUserId() {
        return StpUtil.getLoginIdAsLong();
    }

    /**
     * 刷新会话中的登录用户信息（资料修改后调用）
     */
    public static void refreshLoginUser(LoginUser loginUser) {
        if (StpUtil.isLogin()) {
            StpUtil.getSession().set(LOGIN_USER_KEY, loginUser);
        }
    }

}
