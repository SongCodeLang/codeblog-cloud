-- --------------------------------------------------------------------
-- 数据与缓存（阶段5）：乐观锁 version 列
-- 表：sys_user / sys_role / sys_menu（关联表为覆盖式重写语义，不加）
-- 幂等：information_schema 判存在，可重复执行
-- --------------------------------------------------------------------

USE `codeblog`;

SET @dbname = DATABASE();
SET @tablename = 'sys_user';
SET @colname = 'version';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @colname) > 0,
    'SELECT 1',
    'ALTER TABLE `sys_user` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER `del_flag`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @tablename = 'sys_role';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @colname) > 0,
    'SELECT 1',
    'ALTER TABLE `sys_role` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER `del_flag`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @tablename = 'sys_menu';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = @tablename AND COLUMN_NAME = @colname) > 0,
    'SELECT 1',
    'ALTER TABLE `sys_menu` ADD COLUMN `version` INT NOT NULL DEFAULT 0 COMMENT ''乐观锁版本号'' AFTER `del_flag`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

-- sys_user 历史表结构缺审计列（sys_role/sys_menu 均有），随 BaseEntity 落地补齐
SET @colname = 'create_by';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = @colname) > 0,
    'SELECT 1',
    'ALTER TABLE `sys_user` ADD COLUMN `create_by` VARCHAR(64) NULL DEFAULT NULL COMMENT ''创建者'' AFTER `del_flag`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SET @colname = 'update_by';
SET @preparedStatement = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = @dbname AND TABLE_NAME = 'sys_user' AND COLUMN_NAME = @colname) > 0,
    'SELECT 1',
    'ALTER TABLE `sys_user` ADD COLUMN `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT ''更新者'' AFTER `create_by`'
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;
