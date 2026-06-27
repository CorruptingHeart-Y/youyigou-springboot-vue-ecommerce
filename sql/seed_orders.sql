-- =====================================================
-- 订单演示数据: 为 admin (user_id=1) 创建测试订单
-- 运行方式: mysql -u root -p ecommerce < sql/seed_orders.sql
-- =====================================================

SET @uid = 1;  -- 管理员 admin 的 user_id

-- 1. 确保 admin 有一个收货地址
INSERT INTO e_address (user_id, receiver_name, receiver_phone, province, city, district, detail, is_default)
SELECT @uid, '张三', '13800138000', '浙江省', '杭州市', '西湖区', '文三路 138 号浙大科技园 A 座 1206', 1
WHERE NOT EXISTS (SELECT 1 FROM e_address WHERE user_id = @uid LIMIT 1);

SET @addr_id = (SELECT id FROM e_address WHERE user_id = @uid AND deleted = 0 LIMIT 1);

-- 2. 插入“待收货”订单 — MacBook Pro
INSERT INTO e_order (order_no, user_id, address_id, total_amount, discount_amount, pay_amount,
                     pay_method, pay_no, pay_time, status,
                     logistics_no, logistics_company, remark, create_time, deleted)
VALUES ('20260601MACBOOK001', @uid, @addr_id, 14999.00, 0.00, 14999.00,
        'ALIPAY', 'PAY20260601143000001', '2026-06-01 14:31:22', 'DELIVERED',
        'SF1234567890', '顺丰速运', '请放快递柜', '2026-06-01 14:30:00', 0);

INSERT INTO e_order_item (order_no, product_id, product_name, product_image, price, spec, quantity, subtotal, create_time)
VALUES ('20260601MACBOOK001', 5, 'Apple MacBook Pro 14英寸 M3 Pro 芯片',
        '/uploads/products/laptop/macbook.jpg', 14999.00, '深空黑色 / 18GB+512GB', 1, 14999.00, '2026-06-01 14:30:00');

-- 3. 插入“已完成”订单 — 坚果大礼包
INSERT INTO e_order (order_no, user_id, address_id, total_amount, discount_amount, pay_amount,
                     pay_method, pay_no, pay_time, status,
                     logistics_no, logistics_company, remark, create_time, deleted)
VALUES ('20260528NUTS00002', @uid, @addr_id, 89.00, 10.00, 79.00,
        'WECHAT', 'PAY20260528102105002', '2026-05-28 10:21:05', 'COMPLETED',
        'YT9876543210', '圆通速递', '', '2026-05-28 10:20:00', 0);

INSERT INTO e_order_item (order_no, product_id, product_name, product_image, price, spec, quantity, subtotal, create_time)
VALUES ('20260528NUTS00002', 19, '三只松鼠坚果大礼包 1088g',
        '/uploads/products/food/nuts.jpg', 89.00, '经典款', 1, 89.00, '2026-05-28 10:20:00');

SELECT 'Order seed data inserted successfully!' AS result;
