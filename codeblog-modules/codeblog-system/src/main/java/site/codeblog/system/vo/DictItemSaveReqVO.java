package site.codeblog.system.vo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 字典项保存请求（id 为空=新增；更新时 dictType/itemValue 不可变更，服务层忽略）
 *
 * @author codeblog
 */
@Data
public class DictItemSaveReqVO {

    /**
     * 字典项 ID（新增不传，更新必传）
     */
    private Long id;

    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 60, message = "字典类型编码最长 60 个字符")
    private String dictType;

    @NotBlank(message = "字典标签不能为空")
    @Size(max = 100, message = "字典标签最长 100 个字符")
    private String itemLabel;

    @NotBlank(message = "字典值不能为空")
    @Size(max = 60, message = "字典值最长 60 个字符")
    private String itemValue;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

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
