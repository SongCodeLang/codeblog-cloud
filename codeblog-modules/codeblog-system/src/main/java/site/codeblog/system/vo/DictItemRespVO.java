package site.codeblog.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典项响应（前端下拉与管理页共用）
 *
 * @author codeblog
 */
@Data
public class DictItemRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典项 ID
     */
    private Long id;

    /**
     * 所属字典类型编码
     */
    private String dictType;

    /**
     * 字典标签（展示文案）
     */
    private String itemLabel;

    /**
     * 字典值（存储值）
     */
    private String itemValue;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 乐观锁版本号（编辑时回传）
     */
    private Integer version;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

}
