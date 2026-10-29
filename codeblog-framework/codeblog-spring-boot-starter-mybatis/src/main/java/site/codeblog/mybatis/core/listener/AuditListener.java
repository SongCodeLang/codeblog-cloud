package site.codeblog.mybatis.core.listener;

import com.mybatisflex.annotation.InsertListener;
import com.mybatisflex.annotation.UpdateListener;
import site.codeblog.common.util.ThreadContextHolder;
import site.codeblog.mybatis.core.domain.BaseEntity;

import java.time.LocalDateTime;

/**
 * 审计字段自动填充监听器（阶段5）
 * <p>
 * 实体经 {@code @Table(onInsert = AuditListener.class, onUpdate = AuditListener.class)}
 * 挂载。操作人取 ThreadContextHolder（security starter 的 ContextInterceptor
 * 在登录校验后填充），无登录上下文填 "system"。
 *
 * @author codeblog
 */
public class AuditListener implements InsertListener, UpdateListener {

    /**
     * 无登录上下文时的操作人标识
     */
    private static final String SYSTEM_OPERATOR = "system";

    @Override
    public void onInsert(Object entity) {
        if (entity instanceof BaseEntity base) {
            LocalDateTime now = LocalDateTime.now();
            base.setCreateBy(currentOperator());
            base.setCreateTime(now);
            base.setUpdateBy(currentOperator());
            base.setUpdateTime(now);
        }
    }

    @Override
    public void onUpdate(Object entity) {
        if (entity instanceof BaseEntity base) {
            base.setUpdateBy(currentOperator());
            base.setUpdateTime(LocalDateTime.now());
        }
    }

    private String currentOperator() {
        String username = ThreadContextHolder.getUsername();
        return (username == null || username.isBlank()) ? SYSTEM_OPERATOR : username;
    }

}
