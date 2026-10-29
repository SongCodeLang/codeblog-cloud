package site.codeblog.system.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 登录事件发布器（RocketMQ，经 SCA Stream binder）
 * <p>
 * 开关 {@code codeblog.mq.login-event.enabled}（local profile 默认关闭）。
 * 消息发送属旁路增强，任何失败只记日志，不影响登录主流程。
 *
 * @author codeblog
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "codeblog.mq.login-event.enabled", havingValue = "true", matchIfMissing = true)
public class LoginEventPublisher {

    private final StreamBridge streamBridge;

    public LoginEventPublisher(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    /**
     * 发布一次登录事件（fire-and-forget）
     */
    public void publish(String username, boolean success) {
        if (streamBridge == null) {
            return;
        }
        LoginEventMessage event = new LoginEventMessage(username, success, LocalDateTime.now().toString());
        try {
            streamBridge.send("loginEvent-out-0", event);
        } catch (Exception e) {
            // 旁路容错：MQ 不可用不阻断登录
            log.warn("登录事件发送失败（不影响登录）：username={}, err={}", username, e.getMessage());
        }
    }

}
