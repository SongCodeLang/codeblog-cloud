package site.codeblog.system.constant;

/**
 * 系统管理服务业务错误码
 * <p>
 * 分段约定（见架构文档第 7 节）：system = 1_000_xxx；全局语义码仍在 GlobalErrorCode。
 *
 * @author codeblog
 */
public interface ErrorCodeConstants {

    /**
     * 角色不存在
     */
    int ROLE_NOT_FOUND = 1_000_001;

    /**
     * 角色权限字符已存在
     */
    int ROLE_KEY_DUPLICATE = 1_000_002;

    /**
     * 角色已绑定用户，禁止删除
     */
    int ROLE_IN_USE = 1_000_003;

    /**
     * 菜单不存在
     */
    int MENU_NOT_FOUND = 1_000_004;

    /**
     * 存在子菜单，禁止删除
     */
    int MENU_HAS_CHILDREN = 1_000_005;

    /**
     * 内置角色（admin）禁止修改或删除
     */
    int BUILT_IN_ROLE = 1_000_006;

    /**
     * 登录失败次数过多，账号已锁定
     */
    int LOGIN_LOCKED = 1_000_007;

}
