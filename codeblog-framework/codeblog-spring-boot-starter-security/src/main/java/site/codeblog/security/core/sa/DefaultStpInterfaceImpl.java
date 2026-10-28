package site.codeblog.security.core.sa;

import cn.dev33.satoken.stp.StpInterface;

import java.util.Collections;
import java.util.List;

/**
 * 默认权限数据源实现
 * <p>
 * 直接从登录用户会话读取角色与权限；未登录或会话缺失时返回空集合。
 * 业务服务存在独立权限表时，覆盖此 Bean 即可。
 *
 * @author codeblog
 */
public class DefaultStpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

}
