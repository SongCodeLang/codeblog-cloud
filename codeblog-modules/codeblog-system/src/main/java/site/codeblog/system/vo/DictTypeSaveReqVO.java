package site.codeblog.system.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典类型保存请求（id 为空=新增；更新时 dictType 编码不可变更，服务层忽略该字段）
 *
 * @author codeblog
 */
@Data
public class DictTypeSaveReqVO {

    /**
     * 字典类型 ID（新增不传，更新必传）
     */
    private Long id;

    @NotBlank(message = "字典名称不能为空")
    @Size(max = 60, message = "字典名称最长 60 个字符")
    private String dictName;

    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 60, message = "字典类型编码最长 60 个字符")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "字典类型编码仅支持字母、数字、下划线，且以字母开头")
    private String dictType;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

    @Size(max = 200, message = "备注最长 200 个字符")
    private String remark;

    /**
     * 乐观锁版本号（编辑时回传，并发修改冲突返回 409）
     */
    private Integer version;

}
