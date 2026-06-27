-- =====================================================
-- 种子数据：商品 + 轮播图 + 促销
-- 使用在线占位图，无需本地文件
-- =====================================================
USE ecommerce;

-- 1. 轮播图（3 张）
INSERT INTO e_banner (title, image_url, link_url, sort_order, status) VALUES
('618 年中大促', '/uploads/banners/banner1.svg', '/products', 1, 1),
('新品首发 限时优惠', '/uploads/banners/banner2.svg', '/products?category=1', 2, 1),
('夏日焕新季 全场满减', '/uploads/banners/banner3.svg', '/products?category=2', 3, 1);

-- 2. 商品数据

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
('纯棉圆领短袖 T 恤', 6, '/uploads/products/men/tshirt.jpg', '["/uploads/products/men/tshirt.jpg","/uploads/products/men/tshirt.svg"]', 89.00, 129.00, 500, 3200, '100% 新疆棉 | 透气亲肤 | 圆领设计', 'T恤,男装,休闲', 1),
('商务休闲修身西裤', 6, '/uploads/products/men/jeans.jpg', '["/uploads/products/men/jeans.jpg","/uploads/products/men/jeans.svg"]', 229.00, 359.00, 300, 1800, '弹力抗皱面料 | 商务通勤 | 修身版型', '西裤,男装,商务', 1),
('春季薄款运动夹克', 6, '/uploads/products/men/jacket.jpg', '["/uploads/products/men/jacket.jpg","/uploads/products/men/jacket.svg"]', 399.00, 599.00, 200, 950, '防风防水 | 透气内衬 | 多口袋设计', '夹克,男装,运动', 1),
('复古慢跑鞋', 6, '/uploads/products/men/sneakers.jpg', '["/uploads/products/men/sneakers.jpg","/uploads/products/men/sneakers.svg"]', 459.00, 699.00, 350, 1200, 'EVA 缓震中底 | 复古拼接 | 百搭潮流', '跑鞋,男装,运动', 1);

-- 女装 (category_id = 7)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('法式碎花连衣裙', 7, '/uploads/products/women/dress.jpg', '["/uploads/products/women/dress.jpg","/uploads/products/women/dress.svg"]', 259.00, 399.00, 400, 2100, '雪纺面料 | V领设计 | 优雅碎花图案', '连衣裙,女装,法式', 1),
('真丝缎面衬衫', 7, '/uploads/products/women/blouse.jpg', '["/uploads/products/women/blouse.jpg","/uploads/products/women/blouse.svg"]', 329.00, 499.00, 250, 1400, '100% 桑蚕丝 | 缎面光泽 | 通勤百搭', '衬衫,女装,真丝', 1),
('高腰 A 字半身裙', 7, '/uploads/products/women/skirt.jpg', '["/uploads/products/women/skirt.jpg","/uploads/products/women/skirt.svg"]', 189.00, 299.00, 350, 1800, '高腰显瘦 | A 字版型 | 优雅通勤', '半身裙,女装,高腰', 1),
('轻奢通勤手提包', 7, '/uploads/products/women/handbag.jpg', '["/uploads/products/women/handbag.jpg","/uploads/products/women/handbag.svg"]', 599.00, 899.00, 150, 860, '头层牛皮 | 大容量 | 磁吸搭扣设计', '手提包,女装,轻奢', 1);

-- 食品饮料 (使用 parent_id=3，对应子分类可能还没建，直接挂父分类)
INSERT INTO e_product (name, category_id, cover_image, images, price, original_price, stock, sales, description, keywords, status) VALUES
('云南小粒咖啡豆 500g', 3, '/uploads/products/food/coffee.jpg', '["/uploads/products/food/coffee.jpg","/uploads/products/food/coffee.svg"]', 79.00, 99.00, 1000, 4500, '云南普洱产区 | 中深烘焙 | 坚果焦糖风味', '咖啡,云南,饮品', 1),
('龙井绿茶礼盒装 200g', 3, '/uploads/products/food/greentea.jpg', '["/uploads/products/food/greentea.jpg","/uploads/products/food/greentea.svg"]', 268.00, 358.00, 600, 2100, '明前特级龙井 | 产地直供 | 礼品装', '龙井,茶叶,绿茶', 1),
('每日坚果混合装 750g', 3, '/uploads/products/food/nuts.jpg', '["/uploads/products/food/nuts.jpg","/uploads/products/food/nuts.svg"]', 89.00, 129.00, 800, 6700, '6 种坚果果干 | 科学配比 | 每日一包', '坚果,零食,健康', 1),
('进口零食大礼包', 3, '/uploads/products/food/snackbox.jpg', '["/uploads/products/food/snackbox.jpg","/uploads/products/food/snackbox.svg"]', 159.00, 229.00, 400, 890, '日韩欧美 20+ 种零食 | 超值礼包', '零食,进口,礼包', 1);

-- 3. 创建促销：全场折扣 + 商品秒杀
INSERT INTO e_promotion (title, description, type, discount_value, min_amount, start_time, end_time, status, product_id, create_time) VALUES
('618 全场满 200 减 30', '年中大促全场通用券', 'DISCOUNT', 30.00, 200.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, NULL, NOW()),
('夏装 8 折特惠', '男女装夏季新品限时折扣', 'DISCOUNT', 0.00, 0.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, NULL, NOW());

INSERT INTO e_promotion (title, description, type, discount_value, seckill_stock, min_amount, start_time, end_time, status, product_id, create_time) VALUES
('限时秒杀 iPhone 15 Pro Max', '惊爆价限量抢购', 'SECKILL', 7999.00, 50, 0.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 1, NOW()),
('限时秒杀 纯棉 T 恤', '夏日必备单品秒杀', 'SECKILL', 39.00, 200, 0.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 9, NOW()),
('限时秒杀 每日坚果', '健康零食限量秒杀', 'SECKILL', 59.00, 300, 0.00, '2026-01-01 00:00:00', '2026-12-31 23:59:59', 1, 19, NOW());