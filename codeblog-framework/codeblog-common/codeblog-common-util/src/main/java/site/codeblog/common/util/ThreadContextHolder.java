package site.codeblog.common.util;

import com.alibaba.ttl.TransmittableThreadLocal;

/**
 * 线程上下文工具类
 *
 * @author codeblog
 */
public class ThreadContextHolder {
    
    /**
     * 用户ID上下文
     */
    private static final TransmittableThreadLocal<Long> USER_ID_HOLDER = new TransmittableThreadLocal<>();
    
    /**
     * 用户名上下文
     */
    private static final TransmittableThreadLocal<String> USERNAME_HOLDER = new TransmittableThreadLocal<>();
    
    /**
     * 请求ID上下文
     */
    private static final TransmittableThreadLocal<String> REQUEST_ID_HOLDER = new TransmittableThreadLocal<>();
    
    /**
     * 设置用户ID
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }
    
    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }
    
    /**
     * 设置用户名
     */
    public static void setUsername(String username) {
        USERNAME_HOLDER.set(username);
    }
    
    /**
     * 获取用户名
     */
    public static String getUsername() {
        return USERNAME_HOLDER.get();
    }
    
    /**
     * 设置请求ID
     */
    public static void setRequestId(String requestId) {
        REQUEST_ID_HOLDER.set(requestId);
    }
    
    /**
     * 获取请求ID
     */
    public static String getRequestId() {
        return REQUEST_ID_HOLDER.get();
    }
    
    /**
     * 清除所有上下文
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USERNAME_HOLDER.remove();
        REQUEST_ID_HOLDER.remove();
    }
}