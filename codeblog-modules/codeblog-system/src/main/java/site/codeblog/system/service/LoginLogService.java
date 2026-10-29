package site.codeblog.system.service;

import site.codeblog.common.core.result.PageResult;
import site.codeblog.system.vo.LoginLogPageReqVO;
import site.codeblog.system.vo.LoginLogRespVO;

/**
 * 登录日志服务接口
 *
 * @author codeblog
 */
public interface LoginLogService {

    /**
     * 记录一次登录尝试（IP/UA 取自当前请求；记录失败不影响主流程）
     *
     * @param username 登录账号
     * @param success  是否成功
     * @param msg      提示消息
     */
    void record(String username, boolean success, String msg);

    /**
     * 分页查询登录日志
     */
    PageResult<LoginLogRespVO> page(LoginLogPageReqVO reqVO);

}
