-- --------------------------------------------------------------------
-- CodeBlog Cloud 系统管理服务初始化脚本（全量基线）
-- 数据库：codeblog（需先创建）
-- 默认账号：admin / admin123
-- --------------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS `codeblog` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `codeblog`;

CREATE TABLE IF NOT EXISTS `sys_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
    `username`    VARCHAR(30)  NOT NULL COMMENT '用户名（登录账号）',
    `password`    VARCHAR(100) NOT NULL COMMENT '密码（BCrypt）',
    `nickname`    VARCHAR(30)  NOT NULL DEFAULT '' COMMENT '昵称',
    `email`       VARCHAR(100) NULL     DEFAULT NULL COMMENT '邮箱',
    `mobile`      VARCHAR(20)  NULL     DEFAULT NULL COMMENT '手机号',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 启用，1 停用',
    `del_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
    `create_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '创建者（阶段5 补齐，历史表结构缺失）',
    `update_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '更新者',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, `del_flag`)
) ENGINE = InnoDB COMMENT ='用户表';

-- 默认管理员：admin / admin123（BCrypt）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`, `del_flag`)
SELECT 'admin', '$2a$10$9gkJjAGMB8RevUURYoNme.2g1NUOaMKroVBCzik9qTq5nR0p9I1yi', '管理员', 'admin@codeblog.site', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');

-- --------------------------------------------------------------------
-- RBAC 权限体系 + 登录日志（阶段3 并入基线；增量脚本 rbac_20261028.sql 供存量环境）
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色 ID',
    `role_name`   VARCHAR(30)  NOT NULL COMMENT '角色名称',
    `role_key`    VARCHAR(60)  NOT NULL COMMENT '角色权限字符（如 admin）',
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
    UNIQUE KEY `uk_role_key` (`role_key`, `del_flag`)
) ENGINE = InnoDB COMMENT ='角色表';

CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '菜单 ID',
    `parent_id`   BIGINT       NOT NULL DEFAULT 0 COMMENT '父菜单 ID（0=根）',
    `menu_name`   VARCHAR(30)  NOT NULL COMMENT '菜单名称',
    `menu_type`   CHAR(1)      NOT NULL COMMENT '类型：1 目录，2 菜单，3 按钮',
    `permission`  VARCHAR(60)  NULL     DEFAULT NULL COMMENT '权限标识（如 system:user:list）',
    `path`        VARCHAR(100) NULL     DEFAULT NULL COMMENT '路由地址（目录/菜单）',
    `icon`        VARCHAR(60)  NULL     DEFAULT NULL COMMENT '图标（目录/菜单）',
    `sort_order`  INT          NOT NULL DEFAULT 0 COMMENT '显示顺序',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '状态：0 启用，1 停用',
    `del_flag`    TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0 正常，1 已删除',
    `version`     INT          NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '创建者',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_by`   VARCHAR(64)  NULL     DEFAULT NULL COMMENT '更新者',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE = InnoDB COMMENT ='菜单权限表';

CREATE TABLE IF NOT EXISTS `sys_user_role` (
    `user_id`     BIGINT       NOT NULL COMMENT '用户 ID',
    `role_id`     BIGINT       NOT NULL COMMENT '角色 ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE = InnoDB COMMENT ='用户角色关联表';

CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `role_id`     BIGINT       NOT NULL COMMENT '角色 ID',
    `menu_id`     BIGINT       NOT NULL COMMENT '菜单 ID',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`role_id`, `menu_id`)
) ENGINE = InnoDB COMMENT ='角色菜单关联表';

CREATE TABLE IF NOT EXISTS `sys_login_log` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志 ID',
    `username`    VARCHAR(30)  NOT NULL COMMENT '登录账号',
    `ip`          VARCHAR(64)  NULL     DEFAULT NULL COMMENT '登录 IP',
    `user_agent`  VARCHAR(500) NULL     DEFAULT NULL COMMENT '浏览器标识',
    `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '结果：0 成功，1 失败',
    `msg`         VARCHAR(255) NULL     DEFAULT NULL COMMENT '提示消息',
    `login_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    PRIMARY KEY (`id`),
    KEY `idx_username` (`username`),
    KEY `idx_login_time` (`login_time`)
) ENGINE = InnoDB COMMENT ='登录日志表';

-- 种子数据（固定 ID，便于关联绑定；重复执行不重复插入）

-- 角色：admin 超级管理员（权限侧通配 *，无需逐菜单绑定）；common 普通用户（验证 403 用）
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `sort_order`, `status`, `remark`, `del_flag`)
SELECT 1, '超级管理员', 'admin', 0, 0, '拥有全部权限', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1);
INSERT INTO `sys_role` (`id`, `role_name`, `role_key`, `sort_order`, `status`, `remark`, `del_flag`)
SELECT 2, '普通用户', 'common', 1, 0, '仅基础访问权限', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 2);

-- 菜单：系统管理目录 + 用户/角色/菜单/登录日志及按钮
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `icon`, `sort_order`, `del_flag`)
SELECT 100, 0, '系统管理', '1', NULL, '/system', 'SettingOutlined', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 100);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `sort_order`, `del_flag`)
SELECT 200, 100, '用户管理', '2', 'system:user:list', '/system/users', 1, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 200);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 201, 200, '用户查询', '3', 'system:user:query', 1, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 201);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 202, 200, '用户新增', '3', 'system:user:create', 2, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 202);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 203, 200, '用户修改', '3', 'system:user:update', 3, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 203);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 204, 200, '用户删除', '3', 'system:user:delete', 4, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 204);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 205, 200, '重置密码', '3', 'system:user:reset-password', 5, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 205);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 206, 200, '分配角色', '3', 'system:user:assign-role', 6, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 206);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `sort_order`, `del_flag`)
SELECT 300, 100, '角色管理', '2', 'system:role:list', '/system/roles', 2, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 300);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 301, 300, '角色新增', '3', 'system:role:create', 1, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 301);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 302, 300, '角色修改', '3', 'system:role:update', 2, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 302);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 303, 300, '角色删除', '3', 'system:role:delete', 3, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 303);
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `sort_order`, `del_flag`)
SELECT 304, 300, '分配权限', '3', 'system:role:assign-menu', 4, 0 WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 304);
-- 菜单管理：阶段3 仅树查询用于角色授权，管理页后续阶段开放
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `sort_order`, `del_flag`)
SELECT 400, 100, '菜单管理', '2', 'system:menu:list', '/system/menus', 3, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 400);
-- 登录日志：阶段3 提供查询接口，管理页随阶段6 日志查询统一开放
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `permission`, `path`, `sort_order`, `del_flag`)
SELECT 500, 100, '登录日志', '2', 'system:login-log:list', '/system/login-logs', 4, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 500);

-- 绑定：admin 用户（id=1）→ admin 角色
INSERT INTO `sys_user_role` (`user_id`, `role_id`)
SELECT 1, 1
WHERE NOT EXISTS (SELECT 1 FROM `sys_user_role` WHERE `user_id` = 1 AND `role_id` = 1);

-- 绑定：common 角色 → 仅“系统管理目录”（目录无权限标识，即无任何管理权限，403 验证基准）
INSERT INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 2, 100 WHERE NOT EXISTS (SELECT 1 FROM `sys_role_menu` WHERE `role_id` = 2 AND `menu_id` = 100);

-- --------------------------------------------------------------------
-- 数据字典（dict）：sys_dict_type / sys_dict_item（阶段字典能力并入基线；增量脚本 dict_20261029.sql 供存量环境）
-- 注意：字典表不设 DB 唯一键——含 del_flag 的唯一键在同 key 二次软删时撞键（实测必现）；
-- 唯一性由服务层查重保证（RuoYi 字典表同样无 uk，见 AGENTS 已知坑 10）
-- --------------------------------------------------------------------

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

-- 种子：通用状态 / 登录结果（字典回写首批数据源）
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `del_flag`)
SELECT 1, '通用状态', 'sys_common_status', 0, '0 正常/启用，1 停用', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `id` = 1);
INSERT INTO `sys_dict_type` (`id`, `dict_name`, `dict_type`, `status`, `remark`, `del_flag`)
SELECT 2, '登录结果', 'sys_login_status', 0, '0 成功，1 失败', 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `id` = 2);
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_common_status', '正常', '0', 1, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_common_status' AND `item_value` = '0' AND `del_flag` = 0);
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_common_status', '停用', '1', 2, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_common_status' AND `item_value` = '1' AND `del_flag` = 0);
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_login_status', '成功', '0', 1, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_login_status' AND `item_value` = '0' AND `del_flag` = 0);
INSERT INTO `sys_dict_item` (`dict_type`, `item_label`, `item_value`, `sort_order`, `status`, `del_flag`)
SELECT 'sys_login_status', '失败', '1', 2, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_item` WHERE `dict_type` = 'sys_login_status' AND `item_value` = '1' AND `del_flag` = 0);

-- 菜单：数据字典 + 按钮
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

-- --------------------------------------------------------------------
-- Seata AT 模式 undo_log（阶段2 并入基线；增量脚本 seata_20261028.sql 供存量环境）
-- --------------------------------------------------------------------

CREATE TABLE IF NOT EXISTS `undo_log` (
    `branch_id`     BIGINT     NOT NULL COMMENT 'branch transaction id',
    `xid`           VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    `context`       VARCHAR(128) NOT NULL COMMENT 'undo_log context,such as serialization',
    `rollback_info` LONGBLOB   NOT NULL COMMENT 'rollback info',
    `log_status`    INT        NOT NULL COMMENT '0:normal status,1:defense status',
    `log_created`   DATETIME(6) NOT NULL COMMENT 'create datetime',
    `log_modified`  DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY `ux_undo_log` (`xid`, `branch_id`)
) ENGINE = InnoDB COMMENT = 'AT transaction mode undo table';
