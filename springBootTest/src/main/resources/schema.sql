-- ============================================================================
-- jojo 项目建表脚本（幂等，可重复执行）
-- 数据库：jojo
-- 表：jojo_user（用户）、jojo_quest（首页奇妙冒险任务）
-- ============================================================================

CREATE TABLE IF NOT EXISTS `jojo_user` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(64)  NOT NULL COMMENT '替身使者名',
    `email`       VARCHAR(128) NULL COMMENT '邮箱（注册时可不填）',
    `password`    VARCHAR(128) NOT NULL COMMENT 'BCrypt 密码哈希',
    `create_time` DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '觉醒时间',
    `role`        VARCHAR(20)  NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '替身使者（用户）表';

-- 迁移：兼容已存在的旧表，允许邮箱为空并解除邮箱唯一约束（注册不再强制邮箱）。
-- 重复执行时 MODIFY COLUMN 幂等；DROP INDEX 若已不存在会被 continue-on-error 忽略。
ALTER TABLE `jojo_user` MODIFY COLUMN `email` VARCHAR(128) NULL COMMENT '邮箱（注册时可不填）';
ALTER TABLE `jojo_user` DROP INDEX `uk_email`;

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
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '菜单ID',
    `item_key`    VARCHAR(32) NOT NULL COMMENT '菜单标识，如 home/quests/stands/archive',
    `label`       VARCHAR(32) NOT NULL COMMENT '展示文案',
    `icon`        VARCHAR(32) DEFAULT NULL COMMENT '图标 emoji / 单字符',
    `route`       VARCHAR(64) DEFAULT NULL COMMENT '跳转：页内锚点 id 或 http(s) 外链，留空仅占位',
    `description` VARCHAR(64) DEFAULT NULL COMMENT '副标题 / 悬浮提示',
    `sort`        INT         NOT NULL DEFAULT 0 COMMENT '展示顺序（升序）',
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

-- 替身图鉴表（首页「替身图鉴」区块数据来源，公开浏览）
-- range 为 MySQL 8.0 保留字，列名加反引号。六维属性取值 A~E 或 ∞（无穷）。
CREATE TABLE IF NOT EXISTS `jojo_stand` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '替身ID',
    `name`          VARCHAR(64)  NOT NULL COMMENT '替身名（中文译名）',
    `original_name` VARCHAR(64)  DEFAULT NULL COMMENT '原名（英文/罗马音）',
    `user_name`     VARCHAR(64)  NOT NULL COMMENT '替身使者（使用者）',
    `type`          VARCHAR(32)  NOT NULL DEFAULT '近距离力量型' COMMENT '类型',
    `destroy_power` VARCHAR(2)   NOT NULL DEFAULT 'C' COMMENT '破坏力 A~E/∞',
    `speed`         VARCHAR(2)   NOT NULL DEFAULT 'C' COMMENT '速度 A~E/∞',
    `range`         VARCHAR(2)   NOT NULL DEFAULT 'C' COMMENT '射程距离 A~E/∞',
    `endurance`     VARCHAR(2)   NOT NULL DEFAULT 'C' COMMENT '持续力 A~E/∞',
    `precision`     VARCHAR(2)   NOT NULL DEFAULT 'C' COMMENT '精密度 A~E/∞',
    `growth`        VARCHAR(2)   NOT NULL DEFAULT 'C' COMMENT '成长性 A~E/∞',
    `ability`       VARCHAR(512) NOT NULL COMMENT '能力描述',
    `quote`         VARCHAR(256) DEFAULT NULL COMMENT '经典台词',
    `sort`          INT          NOT NULL DEFAULT 0 COMMENT '展示顺序（升序）',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '替身图鉴表';

-- 冒险档案表（首页「冒险档案」区块数据来源，公开浏览）
CREATE TABLE IF NOT EXISTS `jojo_archive` (
    `id`        BIGINT       NOT NULL AUTO_INCREMENT COMMENT '档案ID',
    `part`      VARCHAR(16)  NOT NULL COMMENT '篇章，如 第一部/第六部',
    `title`     VARCHAR(128) NOT NULL COMMENT '档案标题',
    `era`       VARCHAR(64)  DEFAULT NULL COMMENT '年代/舞台',
    `location`  VARCHAR(64)  DEFAULT NULL COMMENT '主要舞台地点',
    `hero`      VARCHAR(64)  DEFAULT NULL COMMENT '主角（JOJO）',
    `villain`   VARCHAR(64)  DEFAULT NULL COMMENT '宿敌',
    `summary`   VARCHAR(512) NOT NULL COMMENT '摘要',
    `highlight` VARCHAR(256) DEFAULT NULL COMMENT '名场面/亮点',
    `sort`      INT          NOT NULL DEFAULT 0 COMMENT '展示顺序（升序）',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '冒险档案表';

-- 名角档案表（首页「名角档案」区块数据来源，公开浏览）
-- 列名避开 MySQL 保留字（不使用 character / order / key 等作为列名）。
CREATE TABLE IF NOT EXISTS `jojo_character` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '角色ID',
    `name`            VARCHAR(64)  NOT NULL COMMENT '中文名 / 译名',
    `name_original`   VARCHAR(64)  DEFAULT NULL COMMENT '原名 / 罗马音',
    `alias`           VARCHAR(255) DEFAULT NULL COMMENT '别名 / 称号（多个用「·」分隔）',
    `role_type`       VARCHAR(64)  DEFAULT NULL COMMENT '定位',
    `era`             VARCHAR(64)  DEFAULT NULL COMMENT '活跃年代',
    `nationality`     VARCHAR(64)  DEFAULT NULL COMMENT '国籍',
    `debut`           VARCHAR(64)  DEFAULT NULL COMMENT '初登场篇章',
    `personality`     VARCHAR(512) DEFAULT NULL COMMENT '性格描述',
    `vampire_ability` VARCHAR(512) DEFAULT NULL COMMENT '吸血鬼 / 特殊能力（多个用「、」分隔）',
    `stand_name`      VARCHAR(64)  DEFAULT NULL COMMENT '替身名（中文）',
    `stand_original`  VARCHAR(64)  DEFAULT NULL COMMENT '替身原名（英文 / 罗马音）',
    `quotes`          TEXT         DEFAULT NULL COMMENT '经典语录（多条用换行分隔）',
    `biography`       VARCHAR(1024) DEFAULT NULL COMMENT '人物简介 / 生平',
    `highlight`       VARCHAR(512) DEFAULT NULL COMMENT '名场面 / 高光时刻',
    `sort`            INT          NOT NULL DEFAULT 0 COMMENT '展示顺序（升序）',
    PRIMARY KEY (`id`),
    KEY `idx_sort` (`sort`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COMMENT = '名角档案表';
