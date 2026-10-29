package site.codeblog.security.core.sa;

import cn.dev33.satoken.stp.StpInterface;
import site.codeblog.security.core.LoginHelper;
import site.codeblog.security.core.LoginUser;

import java.util.Collections;
import java.util.List;

/**
 * 默认权限数据源实现
 * <p>
 * 直接从登录用户会话读取角色与权限（登录时由业务侧填充进 {@link LoginUser}）；
 * 未登录或会话缺失时返回空集合。角色/权限存在独立权限表且需实时校验时，业务服务覆盖此 Bean。
 *
 * @author codeblog
 */
public class DefaultStpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUserByLoginId(loginId);
        if (loginUser == null || loginUser.getPermissions() == null) {
            return Collections.emptyList();
        }
        return List.copyOf(loginUser.getPermissions());
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        LoginUser loginUser = LoginHelper.getLoginUserByLoginId(loginId);
        if (loginUser == null || loginUser.getRoles() == null) {
            return Collections.emptyList();
        }
        return List.copyOf(loginUser.getRoles());
    }

}
