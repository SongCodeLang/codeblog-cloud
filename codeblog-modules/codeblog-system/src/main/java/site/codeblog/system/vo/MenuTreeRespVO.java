package site.codeblog.system.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树节点响应
 *
 * @author codeblog
 */
@Data
public class MenuTreeRespVO {

    /**
     * 菜单 ID
     */
    private Long id;

    /**
     * 父菜单 ID（0=根）
     */
    private Long parentId;

    /**
     * 菜单名称
     */
    private String menuName;

    /**
     * 类型：1 目录，2 菜单，3 按钮
     */
    private String menuType;

    /**
     * 权限标识
     */
    private String permission;

    /**
     * 路由地址
     */
    private String path;

    /**
     * 图标
     */
    private String icon;

    /**
     * 显示顺序
     */
    private Integer sortOrder;

    /**
     * 状态：0 启用，1 停用
     */
    private Integer status;

    /**
     * 状态展示（字典回写）
     */
    private String statusLabel;

    /**
     * 子菜单
     */
    private List<MenuTreeRespVO> children = new ArrayList<>();

}
