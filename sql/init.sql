-- =====================================================
-- 电商平台数据库初始化脚本
-- =====================================================

CREATE DATABASE IF NOT EXISTS ecommerce DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE ecommerce;

-- 用户表
CREATE TABLE IF NOT EXISTS e_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称',
    avatar VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
    email VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    gender INT DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色 USER/ADMIN/SUPER_ADMIN',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除 0-正常 1-删除',
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品分类表
CREATE TABLE IF NOT EXISTS e_category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '分类名称',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID 0表示一级分类',
    sort_order INT DEFAULT 0 COMMENT '排序',
    icon VARCHAR(255) DEFAULT NULL COMMENT '图标',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
CREATE TABLE IF NOT EXISTS e_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '商品名称',
    category_id BIGINT NOT NULL COMMENT '分类ID',
    cover_image VARCHAR(500) DEFAULT NULL COMMENT '封面图',
    images TEXT DEFAULT NULL COMMENT '商品图片 JSON数组',
    price DECIMAL(10,2) NOT NULL COMMENT '售价',
    original_price DECIMAL(10,2) DEFAULT NULL COMMENT '原价',
    stock INT NOT NULL DEFAULT 0 COMMENT '库存',
    sales INT NOT NULL DEFAULT 0 COMMENT '销量',
    specs TEXT DEFAULT NULL COMMENT '规格 JSON',
    description TEXT DEFAULT NULL COMMENT '描述',
    detail MEDIUMTEXT DEFAULT NULL COMMENT '详情 HTML',
    keywords VARCHAR(500) DEFAULT NULL COMMENT '关键词',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0-下架 1-上架',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_category_id (category_id),
    INDEX idx_name (name),
    INDEX idx_status (status),
    FULLTEXT INDEX ft_name_keywords (name, keywords)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 购物车表
CREATE TABLE IF NOT EXISTS e_cart_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) DEFAULT NULL COMMENT '商品名称快照',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片快照',
    price DECIMAL(10,2) NOT NULL COMMENT '加入时单价',
    spec VARCHAR(100) DEFAULT NULL COMMENT '规格',
    quantity INT NOT NULL DEFAULT 1 COMMENT '数量',
    checked TINYINT NOT NULL DEFAULT 1 COMMENT '是否勾选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 收货地址表
CREATE TABLE IF NOT EXISTS e_address (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    receiver_name VARCHAR(50) NOT NULL COMMENT '收货人',
    receiver_phone VARCHAR(20) NOT NULL COMMENT '手机号',
    province VARCHAR(50) NOT NULL COMMENT '省份',
    city VARCHAR(50) NOT NULL COMMENT '城市',
    district VARCHAR(50) NOT NULL COMMENT '区县',
    detail VARCHAR(255) NOT NULL COMMENT '详细地址',
    is_default TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 订单表
CREATE TABLE IF NOT EXISTS e_order (
    order_no VARCHAR(32) PRIMARY KEY COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    address_id BIGINT DEFAULT NULL COMMENT '地址快照ID',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '总金额',
    discount_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
    pay_amount DECIMAL(10,2) NOT NULL COMMENT '实付金额',
    pay_method VARCHAR(20) DEFAULT NULL COMMENT '支付方式 ALIPAY/WECHAT/BALANCE',
    pay_no VARCHAR(64) DEFAULT NULL COMMENT '支付流水号',
    pay_time DATETIME DEFAULT NULL COMMENT '支付时间',
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING_PAY' COMMENT '订单状态',
    logistics_no VARCHAR(50) DEFAULT NULL COMMENT '物流单号',
    logistics_company VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
CREATE TABLE IF NOT EXISTS e_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    product_name VARCHAR(200) DEFAULT NULL COMMENT '商品名称快照',
    product_image VARCHAR(500) DEFAULT NULL COMMENT '商品图片快照',
    price DECIMAL(10,2) NOT NULL COMMENT '单价',
    spec VARCHAR(100) DEFAULT NULL COMMENT '规格',
    quantity INT NOT NULL COMMENT '数量',
    subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 商品评价表
CREATE TABLE IF NOT EXISTS e_review (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    order_item_id BIGINT DEFAULT NULL COMMENT '订单明细ID',
    order_no VARCHAR(32) DEFAULT NULL COMMENT '订单号',
    content TEXT NOT NULL COMMENT '评价内容',
    images TEXT DEFAULT NULL COMMENT '评价图片 JSON',
    rating INT NOT NULL DEFAULT 5 COMMENT '评分 1-5',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0-隐藏 1-显示',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    INDEX idx_product_id (product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- 用户收藏表
CREATE TABLE IF NOT EXISTS e_user_favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    product_id BIGINT NOT NULL COMMENT '商品ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 轮播图表
CREATE TABLE IF NOT EXISTS e_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL COMMENT '标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) DEFAULT NULL COMMENT '链接URL',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0-禁用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 公告表
CREATE TABLE IF NOT EXISTS e_announcement (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    status INT NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-发布',
    priority INT DEFAULT 0 COMMENT '优先级',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公告表';

-- 用户反馈表
CREATE TABLE IF NOT EXISTS e_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '反馈内容',
    images TEXT DEFAULT NULL COMMENT '图片 JSON',
    reply TEXT DEFAULT NULL COMMENT '管理员回复',
    reply_by BIGINT DEFAULT NULL COMMENT '回复管理员ID',
    reply_time DATETIME DEFAULT NULL COMMENT '回复时间',
    status INT NOT NULL DEFAULT 0 COMMENT '状态 0-待处理 1-已处理',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户反馈表';

-- 促销活动表
CREATE TABLE IF NOT EXISTS e_promotion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL COMMENT '标题',
    type VARCHAR(20) NOT NULL COMMENT '类型 SECKILL/DISCOUNT/COUPON',
    description TEXT DEFAULT NULL COMMENT '描述',
    product_id BIGINT DEFAULT NULL COMMENT '关联商品 NULL表示全场',
    discount_value DECIMAL(10,2) NOT NULL COMMENT '优惠值',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status INT NOT NULL DEFAULT 1 COMMENT '状态 0-停用 1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    deleted INT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='促销活动表';

-- =====================================================
-- 初始数据: 管理员账号 admin / 123456
-- =====================================================
INSERT INTO e_user (username, password, nickname, email, `role`, status) VALUES
('admin', '$2a$10$6DeLzg6Crv0IhysRqr4EcemCH17eW5gqAEMZOCojxzbzLd26XluVu', '超级管理员', 'admin@ecommerce.com', 'SUPER_ADMIN', 1);

INSERT INTO e_category (name, parent_id, sort_order, status) VALUES
('电子产品', 0, 1, 1),
('服装鞋帽', 0, 2, 1),
('食品饮料', 0, 3, 1),
('手机', 1, 1, 1),
('电脑', 1, 2, 1),
('男装', 2, 1, 1),
('女装', 2, 2, 1);