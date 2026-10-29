package site.codeblog.system.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import com.mybatisflex.core.query.QueryWrapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import site.codeblog.common.core.result.PageResult;
import site.codeblog.system.entity.LoginLogPO;
import site.codeblog.system.mapper.LoginLogMapper;
import site.codeblog.system.service.LoginLogService;
import site.codeblog.system.vo.LoginLogPageReqVO;
import site.codeblog.system.vo.LoginLogRespVO;

import java.time.LocalDateTime;
import java.util.List;

import static site.codeblog.system.entity.table.LoginLogPOTableDef.LOGIN_LOG_PO;

/**
 * 登录日志服务实现
 *
 * @author codeblog
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    /**
     * 结果：成功
     */
    private static final int STATUS_SUCCESS = 0;

    /**
     * 结果：失败
     */
    private static final int STATUS_FAIL = 1;

    private final LoginLogMapper loginLogMapper;

    @Override
    public PageResult<LoginLogRespVO> page(LoginLogPageReqVO reqVO) {
        QueryWrapper query = QueryWrapper.create()
                .where(LOGIN_LOG_PO.USERNAME.like(reqVO.getUsername(), StrUtil::isNotBlank))
                .and(LOGIN_LOG_PO.STATUS.eq(reqVO.getStatus(), reqVO.getStatus() != null))
                .orderBy(LOGIN_LOG_PO.ID.desc());

        var page = loginLogMapper.paginate(reqVO.getPageNo(), reqVO.getPageSize(), query);
        List<LoginLogRespVO> list = page.getRecords().stream().map(this::convert).toList();
        return PageResult.of(list, page.getTotalRow());
    }

    private LoginLogRespVO convert(LoginLogPO po) {
        LoginLogRespVO respVO = new LoginLogRespVO();
        respVO.setId(po.getId());
        respVO.setUsername(po.getUsername());
        respVO.setIp(po.getIp());
        respVO.setUserAgent(po.getUserAgent());
        respVO.setStatus(po.getStatus());
        respVO.setMsg(po.getMsg());
        respVO.setLoginTime(po.getLoginTime());
        return respVO;
    }

    @Override
    public void record(String username, boolean success, String msg) {
        try {
            LoginLogPO loginLog = new LoginLogPO();
            loginLog.setUsername(username);
            loginLog.setStatus(success ? STATUS_SUCCESS : STATUS_FAIL);
            loginLog.setMsg(msg);
            loginLog.setLoginTime(LocalDateTime.now());

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                loginLog.setIp(JakartaServletUtil.getClientIP(request, null));
                loginLog.setUserAgent(request.getHeader("User-Agent"));
            }
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            // 登录日志属于旁路审计，记录失败不影响登录主流程
            log.warn("登录日志记录失败：username={}, msg={}", username, e.getMessage());
        }
    }

}
