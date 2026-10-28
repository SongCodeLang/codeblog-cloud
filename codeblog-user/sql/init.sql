-- --------------------------------------------------------------------
-- CodeBlog Cloud 用户服务初始化脚本
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
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`, `del_flag`)
) ENGINE = InnoDB COMMENT ='用户表';

-- 默认管理员：admin / admin123（BCrypt）
INSERT INTO `sys_user` (`username`, `password`, `nickname`, `email`, `status`, `del_flag`)
SELECT 'admin', '$2a$10$9gkJjAGMB8RevUURYoNme.2g1NUOaMKroVBCzik9qTq5nR0p9I1yi', '管理员', 'admin@codeblog.site', 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_user` WHERE `username` = 'admin');
