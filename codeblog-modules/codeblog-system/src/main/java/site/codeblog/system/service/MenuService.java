package site.codeblog.system.service;

import site.codeblog.system.vo.MenuTreeRespVO;

import java.util.List;

/**
 * 菜单服务接口
 *
 * @author codeblog
 */
public interface MenuService {

    /**
     * 全量菜单树（角色授权选择用；目录→菜单→按钮三层）
     */
    List<MenuTreeRespVO> tree();

}
