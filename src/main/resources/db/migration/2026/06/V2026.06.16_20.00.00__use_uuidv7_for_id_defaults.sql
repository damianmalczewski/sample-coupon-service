ALTER TABLE coupons
    ALTER COLUMN coupon_id SET DEFAULT uuidv7();

ALTER TABLE coupon_redemptions
    ALTER COLUMN coupon_redemption_id SET DEFAULT uuidv7();
