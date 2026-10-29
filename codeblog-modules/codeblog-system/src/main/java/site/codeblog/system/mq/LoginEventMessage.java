package site.codeblog.system.mq;

/**
 * 登录事件消息（RocketMQ topic：codeblog-login-event）
 *
 * @param username 登录账号
 * @param success  是否成功
 * @param loginTime 登录时间（ISO-8601）
 * @author codeblog
 */
public record LoginEventMessage(String username, boolean success, String loginTime) {
}
