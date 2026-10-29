package site.codeblog.mybatis.core.domain;

import com.mybatisflex.annotation.Column;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 实体公共审计字段基类（阶段5）
 * <p>
 * 由 {@code @Table(onInsert/onUpdate = AuditListener.class)} 自动填充，
 * 服务层无需手工 set 审计字段。createBy/updateBy 取当前登录用户
 * （ThreadContextHolder，由 security starter 的 ContextInterceptor 填充），
 * 未登录上下文（系统任务等）填 "system"。version 列因表而异，不放在基类
 * （AGENTS 约定：表里有该列才生效），需要乐观锁的实体自行声明
 * {@code @Column(version = true)}。
 *
 * @author codeblog
 */
@Data
public abstract class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 创建者（登录账号，系统任务为 system）
     */
    private String createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者（登录账号，系统任务为 system）
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}
