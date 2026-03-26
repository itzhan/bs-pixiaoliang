-- 智能停车场管理系统 - 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS smart_parking DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE smart_parking;

SET NAMES utf8mb4;
SET CHARACTER_SET_CLIENT = utf8mb4;
SET CHARACTER_SET_RESULTS = utf8mb4;
SET CHARACTER_SET_CONNECTION = utf8mb4;

-- ============================================================
--  按外键依赖 **逆序** 删除表，保证幂等
-- ============================================================
DROP TABLE IF EXISTS audit_log;
DROP TABLE IF EXISTS system_setting;
DROP TABLE IF EXISTS entry_exit_log;
DROP TABLE IF EXISTS message;
DROP TABLE IF EXISTS review;
DROP TABLE IF EXISTS announcement;
DROP TABLE IF EXISTS payment;
DROP TABLE IF EXISTS blacklist;
DROP TABLE IF EXISTS billing_rule;
DROP TABLE IF EXISTS parking_order;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS parking_space;
DROP TABLE IF EXISTS parking_area;
DROP TABLE IF EXISTS vehicle;
DROP TABLE IF EXISTS sys_user;

-- ============================================================
-- 1. sys_user - 用户表
-- ============================================================
CREATE TABLE sys_user (
    id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username    VARCHAR(50)  NOT NULL                  COMMENT '用户名',
    password    VARCHAR(255) NOT NULL                  COMMENT '密码（BCrypt 编码）',
    real_name   VARCHAR(50)  DEFAULT NULL              COMMENT '真实姓名',
    phone       VARCHAR(20)  DEFAULT NULL              COMMENT '手机号',
    email       VARCHAR(100) DEFAULT NULL              COMMENT '邮箱',
    avatar      VARCHAR(255) DEFAULT NULL              COMMENT '头像地址',
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER'   COMMENT '角色：ADMIN / OPERATOR / USER',
    status      TINYINT      NOT NULL DEFAULT 1        COMMENT '状态：0=禁用 1=启用',
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted     TINYINT      NOT NULL DEFAULT 0        COMMENT '逻辑删除：0=未删 1=已删',
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_phone (phone),
    KEY idx_role (role),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户表';

-- ============================================================
-- 2. vehicle - 车辆表
-- ============================================================
CREATE TABLE vehicle (
    id            BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id       BIGINT      NOT NULL                 COMMENT '所属用户',
    plate_number  VARCHAR(20) NOT NULL                 COMMENT '车牌号',
    vehicle_type  VARCHAR(20) DEFAULT 'SEDAN'          COMMENT '车型：SEDAN/SUV/TRUCK/MOTORCYCLE/ELECTRIC',
    brand         VARCHAR(50) DEFAULT NULL              COMMENT '品牌',
    color         VARCHAR(20) DEFAULT NULL              COMMENT '颜色',
    is_default    TINYINT     DEFAULT 0                 COMMENT '是否默认车辆',
    created_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT     NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_plate_deleted (plate_number, deleted),
    KEY idx_user_id (user_id),
    KEY idx_plate_number (plate_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车辆表';

-- ============================================================
-- 3. parking_area - 停车区域表
-- ============================================================
CREATE TABLE parking_area (
    id               BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name             VARCHAR(100) NOT NULL                  COMMENT '区域名称',
    code             VARCHAR(50)  NOT NULL                  COMMENT '区域编码',
    description      TEXT         DEFAULT NULL              COMMENT '描述',
    total_spaces     INT          NOT NULL DEFAULT 0        COMMENT '总车位数',
    available_spaces INT          NOT NULL DEFAULT 0        COMMENT '可用车位数',
    floor_number     VARCHAR(20)  DEFAULT '1'               COMMENT '楼层',
    area_type        VARCHAR(20)  DEFAULT 'INDOOR'          COMMENT '类型：INDOOR/OUTDOOR/UNDERGROUND',
    status           TINYINT      NOT NULL DEFAULT 1        COMMENT '状态：0=禁用 1=启用',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted          TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_code (code),
    KEY idx_area_type (area_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='停车区域表';

-- ============================================================
-- 4. parking_space - 车位表
-- ============================================================
CREATE TABLE parking_space (
    id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    area_id       BIGINT        NOT NULL                 COMMENT '所属区域',
    space_number  VARCHAR(20)   NOT NULL                 COMMENT '车位编号',
    space_type    VARCHAR(20)   DEFAULT 'STANDARD'       COMMENT '车位类型：STANDARD/VIP/CHARGING/DISABLED',
    status        TINYINT       NOT NULL DEFAULT 0       COMMENT '状态：0=空闲 1=占用 2=预约 3=维护',
    hourly_rate   DECIMAL(10,2) DEFAULT 5.00             COMMENT '每小时费率',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_area_space (area_id, space_number),
    KEY idx_area_id (area_id),
    KEY idx_space_type (space_type),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='车位表';

-- ============================================================
-- 5. reservation - 预约表
-- ============================================================
CREATE TABLE reservation (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    reservation_no  VARCHAR(50)   NOT NULL                 COMMENT '预约编号',
    user_id         BIGINT        NOT NULL                 COMMENT '预约用户',
    vehicle_id      BIGINT        NOT NULL                 COMMENT '预约车辆',
    space_id        BIGINT        NOT NULL                 COMMENT '预约车位',
    start_time      DATETIME      NOT NULL                 COMMENT '预约开始时间',
    end_time        DATETIME      NOT NULL                 COMMENT '预约结束时间',
    status          TINYINT       NOT NULL DEFAULT 0       COMMENT '状态：0=待确认 1=已确认 2=已取消 3=已过期 4=已完成',
    cancel_reason   VARCHAR(255)  DEFAULT NULL             COMMENT '取消原因',
    amount          DECIMAL(10,2) DEFAULT NULL             COMMENT '预约费用',
    payment_status  TINYINT       NOT NULL DEFAULT 0       COMMENT '支付状态：0=待支付 1=已支付',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_reservation_no (reservation_no),
    KEY idx_user_id (user_id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_space_id (space_id),
    KEY idx_status (status),
    KEY idx_start_time (start_time),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='预约表';

-- ============================================================
-- 6. parking_order - 停车订单表
-- ============================================================
CREATE TABLE parking_order (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    order_no        VARCHAR(50)   NOT NULL                 COMMENT '订单编号',
    user_id         BIGINT        DEFAULT NULL             COMMENT '用户（临时车可为空）',
    vehicle_id      BIGINT        DEFAULT NULL             COMMENT '车辆',
    space_id        BIGINT        NOT NULL                 COMMENT '车位',
    plate_number    VARCHAR(20)   NOT NULL                 COMMENT '车牌号',
    entry_time      DATETIME      NOT NULL                 COMMENT '入场时间',
    exit_time       DATETIME      DEFAULT NULL             COMMENT '出场时间',
    duration        INT           DEFAULT NULL             COMMENT '停车时长（分钟）',
    amount          DECIMAL(10,2) DEFAULT NULL             COMMENT '订单金额',
    status          TINYINT       NOT NULL DEFAULT 0       COMMENT '状态：0=停车中 1=已完成 2=异常',
    payment_status  TINYINT       NOT NULL DEFAULT 0       COMMENT '支付状态：0=未支付 1=已支付 2=已退款',
    reservation_id  BIGINT        DEFAULT NULL             COMMENT '关联预约',
    remark          VARCHAR(255)  DEFAULT NULL             COMMENT '备注',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_user_id (user_id),
    KEY idx_vehicle_id (vehicle_id),
    KEY idx_space_id (space_id),
    KEY idx_plate_number (plate_number),
    KEY idx_status (status),
    KEY idx_payment_status (payment_status),
    KEY idx_entry_time (entry_time),
    KEY idx_reservation_id (reservation_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='停车订单表';

-- ============================================================
-- 7. billing_rule - 计费规则表
-- ============================================================
CREATE TABLE billing_rule (
    id          BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    name        VARCHAR(100)  NOT NULL                 COMMENT '规则名称',
    space_type  VARCHAR(20)   NOT NULL DEFAULT 'STANDARD' COMMENT '适用车位类型',
    hourly_rate DECIMAL(10,2) NOT NULL                 COMMENT '每小时费率',
    daily_cap   DECIMAL(10,2) DEFAULT NULL             COMMENT '每日最高收费',
    free_minutes INT          DEFAULT 15               COMMENT '免费时长（分钟）',
    is_active   TINYINT       DEFAULT 1                COMMENT '是否启用',
    description TEXT          DEFAULT NULL             COMMENT '规则描述',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted     TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_space_type (space_type),
    KEY idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='计费规则表';

-- ============================================================
-- 8. blacklist - 黑名单表
-- ============================================================
CREATE TABLE blacklist (
    id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    plate_number  VARCHAR(20)  NOT NULL                 COMMENT '车牌号',
    reason        VARCHAR(255) NOT NULL                 COMMENT '拉黑原因',
    status        TINYINT      NOT NULL DEFAULT 1       COMMENT '状态：0=已移除 1=生效中',
    blocked_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
    removed_at    DATETIME     DEFAULT NULL             COMMENT '移除时间',
    operator_id   BIGINT       DEFAULT NULL             COMMENT '操作人',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted       TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_plate_number (plate_number),
    KEY idx_status (status),
    KEY idx_operator_id (operator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='黑名单表';

-- ============================================================
-- 9. payment - 支付记录表
-- ============================================================
CREATE TABLE payment (
    id              BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    payment_no      VARCHAR(50)   NOT NULL                 COMMENT '支付单号',
    order_id        BIGINT        NOT NULL                 COMMENT '关联订单',
    user_id         BIGINT        DEFAULT NULL             COMMENT '支付用户',
    amount          DECIMAL(10,2) NOT NULL                 COMMENT '支付金额',
    payment_method  VARCHAR(20)   NOT NULL DEFAULT 'WECHAT' COMMENT '支付方式：WECHAT/ALIPAY/CASH/CARD',
    status          TINYINT       NOT NULL DEFAULT 0       COMMENT '状态：0=待支付 1=支付成功 2=已退款',
    paid_at         DATETIME      DEFAULT NULL             COMMENT '支付时间',
    remark          VARCHAR(255)  DEFAULT NULL             COMMENT '备注',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted         TINYINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_id (order_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status),
    KEY idx_payment_method (payment_method),
    KEY idx_paid_at (paid_at),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='支付记录表';

-- ============================================================
-- 10. announcement - 公告表
-- ============================================================
CREATE TABLE announcement (
    id                BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    title             VARCHAR(200) NOT NULL                 COMMENT '标题',
    content           TEXT         NOT NULL                 COMMENT '内容',
    announcement_type VARCHAR(20)  DEFAULT 'NOTICE'         COMMENT '类型：NOTICE/MAINTENANCE/PROMOTION',
    status            TINYINT      NOT NULL DEFAULT 0       COMMENT '状态：0=草稿 1=已发布',
    publisher_id      BIGINT       DEFAULT NULL             COMMENT '发布人',
    published_at      DATETIME     DEFAULT NULL             COMMENT '发布时间',
    created_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted           TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_announcement_type (announcement_type),
    KEY idx_publisher_id (publisher_id),
    KEY idx_published_at (published_at),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='公告表';

-- ============================================================
-- 11. review - 评价/反馈表
-- ============================================================
CREATE TABLE review (
    id         BIGINT   NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id    BIGINT   NOT NULL                 COMMENT '评价用户',
    order_id   BIGINT   DEFAULT NULL             COMMENT '关联订单',
    rating     TINYINT  NOT NULL                 COMMENT '评分（1-5）',
    content    TEXT     DEFAULT NULL              COMMENT '评价内容',
    reply      TEXT     DEFAULT NULL              COMMENT '回复内容',
    reply_at   DATETIME DEFAULT NULL             COMMENT '回复时间',
    status     TINYINT  NOT NULL DEFAULT 1       COMMENT '状态：0=隐藏 1=显示',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted    TINYINT  NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_order_id (order_id),
    KEY idx_rating (rating),
    KEY idx_status (status),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='评价反馈表';

-- ============================================================
-- 12. message - 用户消息表
-- ============================================================
CREATE TABLE message (
    id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id      BIGINT       NOT NULL                 COMMENT '接收用户',
    title        VARCHAR(200) NOT NULL                 COMMENT '消息标题',
    content      TEXT         NOT NULL                 COMMENT '消息内容',
    message_type VARCHAR(20)  DEFAULT 'SYSTEM'         COMMENT '类型：SYSTEM/ORDER/RESERVATION/PAYMENT',
    is_read      TINYINT      NOT NULL DEFAULT 0       COMMENT '是否已读：0=未读 1=已读',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted      TINYINT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_message_type (message_type),
    KEY idx_is_read (is_read),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='用户消息表';

-- ============================================================
-- 13. entry_exit_log - 出入场记录表
-- ============================================================
CREATE TABLE entry_exit_log (
    id              BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    plate_number    VARCHAR(20) NOT NULL                 COMMENT '车牌号',
    log_type        VARCHAR(10) NOT NULL                 COMMENT '类型：ENTRY/EXIT',
    gate_name       VARCHAR(50) DEFAULT NULL             COMMENT '闸口名称',
    captured_image  VARCHAR(255) DEFAULT NULL            COMMENT '抓拍图片地址',
    order_id        BIGINT      DEFAULT NULL             COMMENT '关联订单',
    created_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_plate_number (plate_number),
    KEY idx_log_type (log_type),
    KEY idx_order_id (order_id),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='出入场记录表';

-- ============================================================
-- 14. system_setting - 系统设置表
-- ============================================================
CREATE TABLE system_setting (
    id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键',
    setting_key   VARCHAR(100) NOT NULL                 COMMENT '配置键',
    setting_value TEXT         DEFAULT NULL             COMMENT '配置值',
    description   VARCHAR(255) DEFAULT NULL             COMMENT '描述',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='系统设置表';

-- ============================================================
-- 15. audit_log - 审计日志表
-- ============================================================
CREATE TABLE audit_log (
    id         BIGINT      NOT NULL AUTO_INCREMENT  COMMENT '主键',
    user_id    BIGINT      DEFAULT NULL             COMMENT '操作用户',
    username   VARCHAR(50) DEFAULT NULL             COMMENT '用户名',
    module     VARCHAR(50) NOT NULL                 COMMENT '模块',
    action     VARCHAR(50) NOT NULL                 COMMENT '操作',
    detail     TEXT        DEFAULT NULL             COMMENT '详情',
    ip         VARCHAR(50) DEFAULT NULL             COMMENT 'IP 地址',
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (id),
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_action (action),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='审计日志表';

-- ============================================================
--  初始数据
-- ============================================================

-- 默认管理员（密码：admin123）
INSERT INTO sys_user (username, password, real_name, phone, role, status)
VALUES ('admin', '$2a$10$8WPgDfclLizTIruqh3o98ODvaFBhwqMYsk6xWBIiyE2indeGR5bfq', '系统管理员', '13800000000', 'ADMIN', 1);

-- 默认计费规则
INSERT INTO billing_rule (name, space_type, hourly_rate, daily_cap, free_minutes, is_active, description)
VALUES
    ('标准车位计费', 'STANDARD', 5.00, 50.00, 15, 1, '标准车位：5 元/小时，每日封顶 50 元，前 15 分钟免费'),
    ('VIP 车位计费',  'VIP',      8.00, 80.00, 15, 1, 'VIP 车位：8 元/小时，每日封顶 80 元，前 15 分钟免费'),
    ('充电车位计费',  'CHARGING', 6.00, 60.00, 15, 1, '充电车位：6 元/小时，每日封顶 60 元，前 15 分钟免费'),
    ('无障碍车位计费','DISABLED',  3.00, 30.00, 30, 1, '无障碍车位：3 元/小时，每日封顶 30 元，前 30 分钟免费');

-- 默认系统设置
INSERT INTO system_setting (setting_key, setting_value, description)
VALUES
    ('parking_name',         '智能停车场管理系统', '停车场名称'),
    ('max_reservation_hours', '4',                '最大预约时长（小时）'),
    ('reservation_advance_minutes', '30',         '最少提前预约时间（分钟）'),
    ('free_cancel_minutes',  '15',                '免费取消预约时限（分钟）'),
    ('max_vehicles_per_user', '5',                '每用户最多绑定车辆数');
