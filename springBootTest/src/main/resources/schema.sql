-- ============================================================================
-- jojo 项目建表脚本（幂等，可重复执行）
-- 数据库：jojo
-- 表：jojo_user（用户）、jojo_quest（首页奇妙冒险任务）
-- ============================================================================

CREATE TABLE IF NOT EXISTS `jojo_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '替身使者名',
    `email`       VARCHAR(128) NOT NULL COMMENT '邮箱',
    `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码哈希',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '觉醒时间',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '替身使者（用户）表';

-- 兼容已存在的旧表：补充 role 列（首次执行成功，后续执行因列已存在报错，
-- 但 spring.sql.init.continue-on-error=true 会忽略该错误，保证幂等）。
ALTER TABLE `jojo_user` ADD COLUMN `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN';

CREATE TABLE IF NOT EXISTS `jojo_quest` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '任务ID',
    `rank`        VARCHAR(8)   NOT NULL COMMENT '任务等级 D/C/B/A',
    `title`       VARCHAR(128) NOT NULL COMMENT '任务标题',
    `description` VARCHAR(512) NOT NULL COMMENT '任务描述',
    `sort`        INT          NOT NULL DEFAULT 0 COMMENT '展示顺序（升序）',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`),
    KEY `idx_title` (`title`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '奇妙冒险任务表';

-- 兼容已存在的旧表：为 description 补充默认值，避免后台新增任务时
-- 该列无默认值导致 DataIntegrityViolationException（continue-on-error 保证幂等）。
ALTER TABLE `jojo_quest` MODIFY COLUMN `description` VARCHAR(512) NOT NULL DEFAULT '' COMMENT '任务描述';
