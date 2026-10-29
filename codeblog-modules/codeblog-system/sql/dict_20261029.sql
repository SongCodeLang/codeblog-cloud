-- --------------------------------------------------------------------
-- 数据字典（dict）：sys_dict_type / sys_dict_item（含 Flex 字典回写数据源）
-- 幂等：IF NOT EXISTS / WHERE NOT EXISTS，可重复执行
-- --------------------------------------------------------------------

USE `codeblog`;

CREATE TABLE IF NOT EXISTS `sys_dict_type` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典类型 ID',
    `dict_name`   VARCHAR(60)  NOT NULL COMMENT '字典名称（展示名）',
    `dict_type`   VARCHAR(60)  NOT NULL COMMENT '字典类型编码（如 sys_user_status，@DictLabel 引用值）',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 启用，1 停用',
    `remark`      VARCHAR(200) NULL     DEFAULT NULL COMMENT '备注',
    `del_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type_code` (`dict_type`)
) ENGINE = InnoDB COMMENT ='字典类型表';
-- 注意：不设 uk_dict_type ——含 del_flag 的唯一键在同 key 二次软删时撞键（实测必现），
-- 唯一性由服务层查重保证（RuoYi 字典表同样无 uk），见 AGENTS 已知坑 10

CREATE TABLE IF NOT EXISTS `sys_dict_item` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '字典项 ID',
    `dict_type`   VARCHAR(60)  NOT NULL COMMENT '所属字典类型编码',
    `item_label`  VARCHAR(100) NOT NULL COMMENT '字典标签（展示文案，字典回写目标）',
    `item_value`  VARCHAR(60)  NOT NULL COMMENT '字典值（存储值）',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 启用，1 停用',
    `remark`      VARCHAR(200) NULL     DEFAULT NULL COMMENT '备注',
    `del_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_dict_type` (`dict_type`)
) ENGINE = InnoDB COMMENT ='字典项表';
-- 注意：不设 uk_type_value，理由同上（字典值删除后复用再删属高频操作）

-- --------------------------------------------------------------------
-- 种子数据（幂等）
-- --------------------------------------------------------------------

-- 类型：通用状态（user/role/menu 的 status 复用）
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `del_flag`)
SELECT 1, '通用状态', 'sys_common_status', 0, '0 正常/启用，1 停用', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `id` = 1);
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_common_status', '正常', '0', 1, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_common_status' AND `item_value` = '0');
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_common_status', '停用', '1', 2, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_common_status' AND `item_value` = '1');

-- 类型：登录结果
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `del_flag`)
SELECT 2, '登录结果', 'sys_login_status', 0, '0 成功，1 失败', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `id` = 2);
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_login_status', '成功', '0', 1, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_login_status' AND `item_value` = '0');
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_login_status', '失败', '1', 2, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_login_status' AND `item_value` = '1');

-- 菜单：数据字典 + 按钮（管理页随本批次一并开放）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `sort_order`, `del_flag`)
SELECT 600, 100, '数据字典', '2', 'system:dict:list', '/system/dicts', 5, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 600);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 601, 600, '字典查询', '3', 'system:dict:query', 1, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 601);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 602, 600, '字典新增', '3', 'system:dict:create', 2, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 602);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 603, 600, '字典修改', '3', 'system:dict:update', 3, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 603);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 604, 600, '字典删除', '3', 'system:dict:delete', 4, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 604);
