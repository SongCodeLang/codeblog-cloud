package site.codeblog.system.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 字典类型响应
 *
 * @author codeblog
 */
@Data
public class DictTypeRespVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 字典类型 ID
     */
    private Long id;

    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典类型编码
     */
    private String dictType;

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
