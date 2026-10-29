package site.codeblog.system.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

/**
 * 登录事件消费配置（RocketMQ，经 SCA Stream binder）
 * <p>
 * 消费者 bean 名 loginEvent → 绑定 loginEvent-in-0（destination codeblog-login-event，
 * 消费组 codeblog-system）。开关 {@code codeblog.mq.login-event.enabled} 关闭时不注册
 * bean，Spring Cloud Stream 不会创建消费绑定，即零 MQ 连接（local 降级机制）。
 * 当前消费动作为日志落地（集成验证 + 后续风控/通知的挂点）。
 *
 * @author codeblog
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "codeblog.mq.login-event.enabled", havingValue = "true", matchIfMissing = true)
public class LoginEventConsumerConfig {

    @Bean
    public Consumer<LoginEventMessage> loginEvent() {
        return event -> log.info("收到登录事件：username={}, success={}, time={}",
                event.username(), event.success(), event.loginTime());
    }

}
