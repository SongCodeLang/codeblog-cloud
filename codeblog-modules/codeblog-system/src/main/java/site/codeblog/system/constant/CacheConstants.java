package site.codeblog.system.constant;

import java.time.Duration;

/**
 * 系统管理服务缓存常量（阶段5）
 * <p>
 * 缓存名即 Redis key 前缀（单冒号拼接，对齐全局约定
 * {@code codeblog:{service}:{domain}:{bizId}}）；TTL 在 application.yml
 * {@code codeblog.cache.specs} 覆盖。登录锁定 key 为状态类（非缓存），
 * 一并收口在此管理。
 *
 * @author codeblog
 */
public interface CacheConstants {

    /**
     * 当前用户信息缓存（/auth/me 组装结果：UserRespVO+roles+permissions）
     */
    String USER = "codeblog:system:user";

    /**
     * 菜单树缓存（角色授权抽屉用）
     */
    String MENU_TREE = "codeblog:system:menu-tree";

    /**
     * 启用角色列表缓存（分配角色下拉用）
     */
    String ROLE_LIST = "codeblog:system:role-list";

    // ---- 登录失败锁定（状态 key，非缓存；10 分钟窗口 5 次锁 10 分钟） ----

    String LOGIN_FAIL_COUNT_KEY = "codeblog:system:auth:login-fail:%s";

    String LOGIN_LOCK_KEY = "codeblog:system:auth:login-lock:%s";

    long FAIL_WINDOW_MINUTES = 10;

    long MAX_FAILS = 5;

    long LOCK_MINUTES = 10;

    Duration MENU_TREE_TTL = Duration.ofMinutes(10);

    Duration ROLE_LIST_TTL = Duration.ofMinutes(10);

    Duration USER_TTL = Duration.ofMinutes(30);

}
