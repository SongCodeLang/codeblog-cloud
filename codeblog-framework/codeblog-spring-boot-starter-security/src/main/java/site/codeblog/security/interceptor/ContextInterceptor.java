package site.codeblog.security.interceptor;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;
import site.codeblog.common.util.ThreadContextHolder;
import site.codeblog.security.core.LoginHelper;
import site.codeblog.security.core.LoginUser;

/**
 * 请求上下文拦截器（S3）：登录校验通过后填充 ThreadContextHolder
 * <p>
 * 注册在 SaInterceptor 之后：业务代码经 ThreadContextHolder 取当前用户
 * 无需再依赖 Sa-Token API；TTL 透传到 @Async/线程池（需配套 TTL 装饰，阶段后按需）。
 *
 * @author codeblog
 */
public class ContextInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (StpUtil.isLogin()) {
            LoginUser loginUser = LoginHelper.getLoginUserOrNull();
            if (loginUser != null) {
                ThreadContextHolder.setUserId(loginUser.getUserId());
                ThreadContextHolder.setUsername(loginUser.getUsername());
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // 必须清理，防止线程池复用时串号
        ThreadContextHolder.clear();
    }

}
