-- ===================================================
-- Welcome Coupons — Issue 3 new-user coupons to ALL existing users
-- Run: mysql -u root -p ecommerce < sql/seed_coupons.sql
-- ===================================================

-- 1. Insert the 3 welcome coupon promotions (skip if already exist by title)
INSERT INTO e_promotion (title, description, type, discount_value, min_amount, start_time, end_time, status, product_id, create_time)
SELECT *
FROM (
    SELECT '无门槛红包' AS title, '新人专享 全场通用 立减5元' AS description, 'COUPON' AS type, 5.00 AS discount_value, 0.00 AS min_amount, '2025-01-01 00:00:00' AS start_time, '2027-12-31 23:59:59' AS end_time, 1 AS status, NULL AS product_id, NOW() AS create_time
    UNION ALL
    SELECT '服饰百搭券', '新人专享 满199减30 裙子/T恤/服饰可用', 'COUPON', 30.00, 199.00, '2025-01-01 00:00:00', '2027-12-31 23:59:59', 1, NULL, NOW()
    UNION ALL
    SELECT '数码大额券', '新人专享 满5000减600 手机/电脑/数码可用', 'COUPON', 600.00, 5000.00, '2025-01-01 00:00:00', '2027-12-31 23:59:59', 1, NULL, NOW()
) tmp
WHERE NOT EXISTS (
    SELECT 1 FROM e_promotion p
    WHERE p.type = 'COUPON' AND p.title = tmp.title AND p.deleted = 0
);

-- 2. Issue all 3 coupons to every existing user (ON DUPLICATE KEY handles re-runs safely)
INSERT INTO e_user_coupon (user_id, promotion_id, status, claim_time)
SELECT u.id, p.id, 0, NOW()
FROM e_user u
CROSS JOIN e_promotion p
WHERE p.type = 'COUPON'
  AND p.title IN ('无门槛红包', '服饰百搭券', '数码大额券')
  AND p.deleted = 0
ON DUPLICATE KEY UPDATE status = VALUES(status);
