package site.codeblog.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.result.PageParam;

/**
 * 用户分页查询请求
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserPageReqVO extends PageParam {

    /**
     * 用户名（模糊匹配）
     */
    private String username;

    /**
     * 昵称（模糊匹配）
     */
    private String nickname;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

}
