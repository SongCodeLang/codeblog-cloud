package site.codeblog.system.service.impl;

import com.mybatisflex.core.query.QueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import site.codeblog.system.constant.CacheConstants;
import site.codeblog.system.entity.MenuPO;
import site.codeblog.system.mapper.MenuMapper;
import site.codeblog.system.service.MenuService;
import site.codeblog.system.vo.MenuTreeRespVO;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static site.codeblog.system.entity.table.MenuPOTableDef.MENU_PO;

/**
 * 菜单服务实现
 *
 * @author codeblog
 */
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    @Cacheable(cacheNames = CacheConstants.MENU_TREE)
    public List<MenuTreeRespVO> tree() {
        List<MenuPO> menus = menuMapper.selectListByQuery(QueryWrapper.create()
                .orderBy(MENU_PO.SORT_ORDER.asc(), MENU_PO.ID.asc()));

        Map<Long, MenuTreeRespVO> nodeMap = new LinkedHashMap<>();
        for (MenuPO menu : menus) {
            MenuTreeRespVO node = new MenuTreeRespVO();
            node.setId(menu.getId());
            node.setParentId(menu.getParentId());
            node.setMenuName(menu.getMenuName());
            node.setMenuType(menu.getMenuType());
            node.setPermission(menu.getPermission());
            node.setPath(menu.getPath());
            node.setIcon(menu.getIcon());
            node.setSortOrder(menu.getSortOrder());
            node.setStatus(menu.getStatus());
            node.setStatusLabel(menu.getStatusLabel());
            nodeMap.put(menu.getId(), node);
        }

        List<MenuTreeRespVO> roots = new ArrayList<>();
        for (MenuTreeRespVO node : nodeMap.values()) {
            MenuTreeRespVO parent = node.getParentId() == null ? null : nodeMap.get(node.getParentId());
            if (parent == null) {
                roots.add(node);
            } else {
                parent.getChildren().add(node);
            }
        }
        return roots;
    }

}
