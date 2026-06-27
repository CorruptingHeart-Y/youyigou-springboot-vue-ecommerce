-- =====================================================
-- 修复商品图片：从远程占位图切换为本地图片
-- =====================================================
USE ecommerce;

-- 手机 (4款)
UPDATE e_product SET cover_image = '/uploads/products/phone/iphone15.jpg', images = '["/uploads/products/phone/iphone15.jpg","/uploads/products/phone/iphone15.svg"]' WHERE id = 1;
UPDATE e_product SET cover_image = '/uploads/products/phone/mate60.jpg', images = '["/uploads/products/phone/mate60.jpg","/uploads/products/phone/mate60.svg"]' WHERE id = 2;
UPDATE e_product SET cover_image = '/uploads/products/phone/xiaomi14.jpg', images = '["/uploads/products/phone/xiaomi14.jpg","/uploads/products/phone/xiaomi14.svg"]' WHERE id = 3;
UPDATE e_product SET cover_image = '/uploads/products/phone/samsung24.jpg', images = '["/uploads/products/phone/samsung24.jpg","/uploads/products/phone/samsung24.svg"]' WHERE id = 4;

-- 电脑 (4款)
UPDATE e_product SET cover_image = '/uploads/products/laptop/macbook.jpg', images = '["/uploads/products/laptop/macbook.jpg","/uploads/products/laptop/macbook.svg"]' WHERE id = 5;
UPDATE e_product SET cover_image = '/uploads/products/laptop/thinkpad.jpg', images = '["/uploads/products/laptop/thinkpad.jpg","/uploads/products/laptop/thinkpad.svg"]' WHERE id = 6;
UPDATE e_product SET cover_image = '/uploads/products/laptop/matebook.jpg', images = '["/uploads/products/laptop/matebook.jpg","/uploads/products/laptop/matebook.svg"]' WHERE id = 7;
UPDATE e_product SET cover_image = '/uploads/products/laptop/rog.jpg', images = '["/uploads/products/laptop/rog.jpg","/uploads/products/laptop/rog.svg"]' WHERE id = 8;

-- 男装 (4款)
UPDATE e_product SET cover_image = '/uploads/products/men/tshirt.jpg', images = '["/uploads/products/men/tshirt.jpg","/uploads/products/men/tshirt.svg"]' WHERE id = 9;
UPDATE e_product SET cover_image = '/uploads/products/men/jeans.jpg', images = '["/uploads/products/men/jeans.jpg","/uploads/products/men/jeans.svg"]' WHERE id = 10;
UPDATE e_product SET cover_image = '/uploads/products/men/jacket.jpg', images = '["/uploads/products/men/jacket.jpg","/uploads/products/men/jacket.svg"]' WHERE id = 11;
UPDATE e_product SET cover_image = '/uploads/products/men/sneakers.jpg', images = '["/uploads/products/men/sneakers.jpg","/uploads/products/men/sneakers.svg"]' WHERE id = 12;

-- 女装 (4款)
UPDATE e_product SET cover_image = '/uploads/products/women/dress.jpg', images = '["/uploads/products/women/dress.jpg","/uploads/products/women/dress.svg"]' WHERE id = 13;
UPDATE e_product SET cover_image = '/uploads/products/women/blouse.jpg', images = '["/uploads/products/women/blouse.jpg","/uploads/products/women/blouse.svg"]' WHERE id = 14;
UPDATE e_product SET cover_image = '/uploads/products/women/skirt.jpg', images = '["/uploads/products/women/skirt.jpg","/uploads/products/women/skirt.svg"]' WHERE id = 15;
UPDATE e_product SET cover_image = '/uploads/products/women/handbag.jpg', images = '["/uploads/products/women/handbag.jpg","/uploads/products/women/handbag.svg"]' WHERE id = 16;

-- 食品 (4款)
UPDATE e_product SET cover_image = '/uploads/products/food/coffee.jpg', images = '["/uploads/products/food/coffee.jpg","/uploads/products/food/coffee.svg"]' WHERE id = 17;
UPDATE e_product SET cover_image = '/uploads/products/food/greentea.jpg', images = '["/uploads/products/food/greentea.jpg","/uploads/products/food/greentea.svg"]' WHERE id = 18;
UPDATE e_product SET cover_image = '/uploads/products/food/nuts.jpg', images = '["/uploads/products/food/nuts.jpg","/uploads/products/food/nuts.svg"]' WHERE id = 19;
UPDATE e_product SET cover_image = '/uploads/products/food/snackbox.jpg', images = '["/uploads/products/food/snackbox.jpg","/uploads/products/food/snackbox.svg"]' WHERE id = 20;

-- 轮播图
UPDATE e_banner SET image_url = '/uploads/banners/banner1.svg' WHERE id = 1;
UPDATE e_banner SET image_url = '/uploads/banners/banner2.svg' WHERE id = 2;
UPDATE e_banner SET image_url = '/uploads/banners/banner3.svg' WHERE id = 3;

SELECT 'Done. All product images updated to local paths.' AS result;
