-- =====================================================
-- 数据库编码修复 + 全新 Mock 数据 + Unsplash 高清图
-- =====================================================
USE ecommerce;

-- ============================================
-- 1. 强制刷新所有表为 utf8mb4
-- ============================================
ALTER TABLE e_product CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE e_banner CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE e_promotion CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE e_category CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
ALTER TABLE e_user CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- ============================================
-- 2. 清空旧种子数据
-- ============================================
DELETE FROM e_promotion;
DELETE FROM e_product WHERE id > 0;
DELETE FROM e_banner WHERE id > 0;
DELETE FROM e_category WHERE id > 0;

-- 重置自增
ALTER TABLE e_product AUTO_INCREMENT = 1;
ALTER TABLE e_banner AUTO_INCREMENT = 1;
ALTER TABLE e_promotion AUTO_INCREMENT = 1;
ALTER TABLE e_category AUTO_INCREMENT = 1;

-- ============================================
-- 3. 分类数据
-- ============================================
INSERT INTO e_category (name, parent_id, sort_order, status) VALUES
('电子产品', 0, 1, 1),
('服装鞋帽', 0, 2, 1),
('食品饮料', 0, 3, 1),
('手机', 1, 1, 1),
('电脑', 1, 2, 1),
('男装', 2, 1, 1),
('女装', 2, 2, 1);

-- ============================================
-- 4. 轮播图（Unsplash 宽屏）
-- ============================================
INSERT INTO e_banner (title, image_url, link_url, sort_order, status) VALUES
('618 年中大促', '/uploads/banners/banner1.svg', '/products', 1, 1),
('新品首发 限时优惠', '/uploads/banners/banner2.svg', '/products?category=1', 2, 1),
('夏日焕新季 全场满减', '/uploads/banners/banner3.svg', '/products?category=2', 3, 1);

-- ============================================
-- 5. 商品数据（Unsplash 高清图 + 正确中文）
-- ============================================

-- 手机 (category_id = 4)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('iPhone 15 Pro Max 256GB', 4, '/uploads/products/phone/iphone15.jpg', '["/uploads/products/phone/iphone15.jpg","/uploads/products/phone/iphone15.svg"]', 8999.00, 9999.00, 200, 1580, 'A17 Pro 芯片 | 钛金属设计 | 4800 万像素', 'iPhone,苹果,手机', 1),
('华为 Mate 60 Pro', 4, '/uploads/products/phone/mate60.jpg', '["/uploads/products/phone/mate60.jpg","/uploads/products/phone/mate60.svg"]', 6999.00, 7999.00, 150, 2100, '麒麟 9000S | 卫星通话 | 超可靠玄武架构', '华为,Mate,手机', 1),
('小米 14 Ultra', 4, '/uploads/products/phone/xiaomi14.jpg', '["/uploads/products/phone/xiaomi14.jpg","/uploads/products/phone/xiaomi14.svg"]', 5999.00, 6499.00, 300, 890, '骁龙 8 Gen 3 | 徕卡光学 | 120W 快充', '小米,手机', 1),
('三星 Galaxy S24 Ultra', 4, '/uploads/products/phone/samsung24.jpg', '["/uploads/products/phone/samsung24.jpg","/uploads/products/phone/samsung24.svg"]', 9699.00, 10999.00, 180, 760, 'AI 智慧赋能 | 钛金属框架 | S Pen', '三星,手机', 1);

-- 电脑 (category_id = 5)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('MacBook Pro 14 M3 Pro', 5, '/uploads/products/laptop/macbook.jpg', '["/uploads/products/laptop/macbook.jpg","/uploads/products/laptop/macbook.svg"]', 14999.00, 16999.00, 80, 620, 'M3 Pro 芯片 | 18GB 内存 | Liquid Retina XDR', 'MacBook,苹果,笔记本', 1),
('ThinkPad X1 Carbon Gen 12', 5, '/uploads/products/laptop/thinkpad.jpg', '["/uploads/products/laptop/thinkpad.jpg","/uploads/products/laptop/thinkpad.svg"]', 10999.00, 12999.00, 120, 450, 'Ultra 7 处理器 | 32GB | 商务旗舰', 'ThinkPad,联想,笔记本', 1),
('华为 MateBook X Pro', 5, '/uploads/products/laptop/matebook.jpg', '["/uploads/products/laptop/matebook.jpg","/uploads/products/laptop/matebook.svg"]', 8999.00, 9999.00, 100, 380, '3.1K OLED | 轻薄机身 | 超级终端', '华为,笔记本', 1),
('ROG 枪神 8 Plus', 5, '/uploads/products/laptop/rog.jpg', '["/uploads/products/laptop/rog.jpg","/uploads/products/laptop/rog.svg"]', 19999.00, 21999.00, 50, 290, 'i9-14900HX | RTX 4070 | 240Hz', 'ROG,游戏本', 1);

-- 男装 (category_id = 6)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('纯棉圆领短袖T恤', 6, '/uploads/products/men/tshirt.jpg', '["/uploads/products/men/tshirt.jpg","/uploads/products/men/tshirt.svg"]', 129.00, 199.00, 500, 3200, '100%纯棉 | 舒适透气 | 多色可选', 'T恤,纯棉,男装', 1),
('修身弹力牛仔裤', 6, '/uploads/products/men/jeans.jpg', '["/uploads/products/men/jeans.jpg","/uploads/products/men/jeans.svg"]', 299.00, 399.00, 400, 1800, '弹力面料 | 修身版型 | 四季可穿', '牛仔裤,男装', 1),
('轻薄防风夹克', 6, '/uploads/products/men/jacket.jpg', '["/uploads/products/men/jacket.jpg","/uploads/products/men/jacket.svg"]', 459.00, 699.00, 300, 950, '防风防水 | 轻薄设计 | 户外必备', '夹克,户外,男装', 1),
('复古潮流板鞋', 6, '/uploads/products/men/sneakers.jpg', '["/uploads/products/men/sneakers.jpg","/uploads/products/men/sneakers.svg"]', 399.00, 599.00, 350, 760, '复古设计 | 厚底增高 | 情侣款', '板鞋,潮流,男装', 1);

-- 女装 (category_id = 7)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('法式碎花连衣裙', 7, '/uploads/products/women/dress.jpg', '["/uploads/products/women/dress.jpg","/uploads/products/women/dress.svg"]', 269.00, 399.00, 280, 1350, '法式浪漫 | 碎花印花 | 显瘦收腰', '连衣裙,碎花,女装', 1),
('真丝飘带衬衫', 7, '/uploads/products/women/blouse.jpg', '["/uploads/products/women/blouse.jpg","/uploads/products/women/blouse.svg"]', 359.00, 599.00, 200, 680, '100%桑蚕丝 | 飘带设计 | 通勤百搭', '衬衫,真丝,女装', 1),
('高腰A字短裙', 7, '/uploads/products/women/skirt.jpg', '["/uploads/products/women/skirt.jpg","/uploads/products/women/skirt.svg"]', 199.00, 299.00, 320, 1120, '高腰显瘦 | A字裙摆 | 百搭时尚', '短裙,A字裙,女装', 1),
('真皮斜挎手提包', 7, '/uploads/products/women/handbag.jpg', '["/uploads/products/women/handbag.jpg","/uploads/products/women/handbag.svg"]', 599.00, 899.00, 150, 430, '头层牛皮 | 斜挎手提两用 | 轻奢风', '包,真皮,女装', 1);

-- 食品 (category_id = 3)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('精选云南小粒咖啡豆 500g', 3, '/uploads/products/food/coffee.jpg', '["/uploads/products/food/coffee.jpg","/uploads/products/food/coffee.svg"]', 89.00, 129.00, 600, 4500, '云南高山 | 中度烘焙 | 醇厚回甘', '咖啡,云南,咖啡豆', 1),
('特级龙井绿茶 200g', 3, '/uploads/products/food/greentea.jpg', '["/uploads/products/food/greentea.jpg","/uploads/products/food/greentea.svg"]', 168.00, 258.00, 400, 2800, '明前采摘 | 正宗西湖龙井 | 清香甘甜', '龙井,绿茶,茶叶', 1),
('每日坚果混合装 750g', 3, '/uploads/products/food/nuts.jpg', '["/uploads/products/food/nuts.jpg","/uploads/products/food/nuts.svg"]', 129.00, 189.00, 500, 6700, '6种坚果 | 每日一袋 | 无添加', '坚果,混合,零食', 1),
('网红零食大礼包', 3, '/uploads/products/food/snackbox.jpg', '["/uploads/products/food/snackbox.jpg","/uploads/products/food/snackbox.svg"]', 69.00, 99.00, 800, 3200, '20款网红零食 | 追剧必备 | 礼盒装', '零食,大礼包,网红', 1);

-- ============================================
-- 6. 促销活动数据
-- ============================================
INSERT INTO e_promotion (title, type, description, product_id, discount_value, min_amount, seckill_stock, start_time, end_time, status) VALUES
('618 全场满200减30', 'DISCOUNT', '年中大促，全场商品满200元立减30元，上不封顶！', NULL, 30.00, 200.00, NULL, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('夏装 8 折特惠', 'DISCOUNT', '夏季新款服装限时8折优惠，清凉一夏！', NULL, 0.20, 0.00, NULL, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);

-- 秒杀活动：绑定前 4 个商品
INSERT INTO e_promotion (title, type, description, product_id, discount_value, min_amount, seckill_stock, start_time, end_time, status) VALUES
('限时秒杀-iPhone 15 Pro Max', 'SECKILL', 'iPhone 15 Pro Max 限时直降 1000 元！', 1, 7999.00, 0.00, 50, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('限时秒杀-华为 Mate 60 Pro', 'SECKILL', '华为 Mate 60 Pro 限时秒杀价！', 2, 6499.00, 0.00, 50, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('限时秒杀-小米 14 Ultra', 'SECKILL', '小米 14 Ultra 限时秒杀价！', 3, 5499.00, 0.00, 50, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1),
('限时秒杀-三星 Galaxy S24 Ultra', 'SECKILL', '三星 Galaxy S24 Ultra 限时秒杀价！', 4, 8899.00, 0.00, 50, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1);