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

-- 首页导航栏菜单表（后台可改文案与排序）
CREATE TABLE IF NOT EXISTS `jojo_nav_item` (
    `id`       BIGINT      NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `item_key` VARCHAR(32) NOT NULL COMMENT '菜单标识，如 home/quests/stands/archive',
    `label`    VARCHAR(32) NOT NULL COMMENT '展示文案',
    `sort`     INT         NOT NULL DEFAULT 0 COMMENT '展示顺序（升序）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_item_key` (`item_key`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '首页导航栏菜单表';

-- 首页热门搜索关键词表（搜索框下方「热门」标签的数据来源）
CREATE TABLE IF NOT EXISTS `jojo_hot_keyword` (
    `id`      BIGINT      NOT NULL AUTO_INCREMENT COMMENT '关键词ID',
    `keyword` VARCHAR(64) NOT NULL COMMENT '关键词文本',
    `weight`  INT         NOT NULL DEFAULT 0 COMMENT '权重，越大越靠前',
    `sort`    INT         NOT NULL DEFAULT 0 COMMENT '同权重时的展示顺序（升序）',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_keyword` (`keyword`),
    KEY `idx_weight` (`weight`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '首页热门搜索关键词表';
