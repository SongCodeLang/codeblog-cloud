package site.codeblog.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import site.codeblog.common.core.result.PageParam;

/**
 * 登录日志分页查询请求
 *
 * @author codeblog
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class LoginLogPageReqVO extends PageParam {

    /**
     * 登录账号（模糊匹配）
     */
    private String username;

    /**
     * 结果：0 成功，1 失败
     */
    private Integer status;

}
