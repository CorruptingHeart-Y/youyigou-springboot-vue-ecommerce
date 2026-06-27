-- ============================================
-- Ecommerce Platform v2 Migration
-- 新增: 优惠券系统、秒杀功能、动态权限管理
-- ============================================

-- 1. e_promotion 表新增字段
ALTER TABLE e_promotion ADD COLUMN min_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT '最低消费金额（COUPON类型）' AFTER discount_value;
ALTER TABLE e_promotion ADD COLUMN seckill_stock INT DEFAULT NULL COMMENT '秒杀库存（SECKILL类型）' AFTER min_amount;

-- 2. 用户优惠券表
CREATE TABLE IF NOT EXISTS e_user_coupon (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    promotion_id BIGINT NOT NULL COMMENT '促销活动ID（COUPON类型）',
    status INT NOT NULL DEFAULT 0 COMMENT '状态 0-未使用 1-已使用 2-已过期',
    claim_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
    use_time DATETIME DEFAULT NULL COMMENT '使用时间',
    order_no VARCHAR(32) DEFAULT NULL COMMENT '使用的订单号',
    UNIQUE KEY uk_user_promotion (user_id, promotion_id),
    INDEX idx_user_id (user_id),
    INDEX idx_promotion_id (promotion_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 3. 权限表
CREATE TABLE IF NOT EXISTS e_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL COMMENT '权限名称',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '权限编码',
    description VARCHAR(200) DEFAULT NULL COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 角色权限关联表
CREATE TABLE IF NOT EXISTS e_role_permission (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role VARCHAR(20) NOT NULL COMMENT '角色 USER/ADMIN/SUPER_ADMIN',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_role_permission (role, permission_id),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 5. 初始化权限数据
INSERT INTO e_permission (name, code, description) VALUES
('商品管理', 'product:manage', '商品的增删改查和上下架'),
('订单管理', 'order:manage', '订单的查看、发货、退款处理'),
('用户管理', 'user:manage', '用户的查看、禁用/启用、角色修改'),
('内容管理', 'content:manage', '评价/反馈/公告/轮播图管理'),
('促销管理', 'promotion:manage', '促销活动（秒杀/折扣/优惠券）管理'),
('权限管理', 'permission:manage', '角色和权限的分配管理');

-- 6. 初始化角色-权限映射
-- SUPER_ADMIN 拥有全部权限
INSERT INTO e_role_permission (role, permission_id)
SELECT 'SUPER_ADMIN', id FROM e_permission;

-- ADMIN 拥有除权限管理外的所有权限
INSERT INTO e_role_permission (role, permission_id)
SELECT 'ADMIN', id FROM e_permission WHERE code != 'permission:manage';

-- 7. 示例优惠券数据
INSERT INTO e_promotion (title, description, type, discount_value, min_amount, start_time, end_time, status, product_id, create_time)
VALUES
('新人专享券', '新用户满100减20优惠券', 'COUPON', 20.00, 100.00, '2025-01-01 00:00:00', '2026-12-31 23:59:59', 1, NULL, NOW()),
('数码满减券', '数码产品满500减50', 'COUPON', 50.00, 500.00, '2025-06-01 00:00:00', '2026-12-31 23:59:59', 1, NULL, NOW()),
('夏日畅享券', '全场满200减30', 'COUPON', 30.00, 200.00, '2025-06-01 00:00:00', '2026-08-31 23:59:59', 1, NULL, NOW());

-- 8. 示例秒杀活动数据
INSERT INTO e_promotion (title, description, type, discount_value, seckill_stock, min_amount, start_time, end_time, status, product_id, create_time)
VALUES
('限时秒杀-手机专场', '精选手机限量秒杀', 'SECKILL', 0.00, 100, 0.00, '2025-06-01 00:00:00', '2026-12-31 23:59:59', 1, NULL, NOW());