ALTER TABLE coupon_redemptions
    ADD COLUMN coupon_redemption_request_id UUID;

UPDATE coupon_redemptions
SET coupon_redemption_request_id = coupon_redemption_id;

ALTER TABLE coupon_redemptions
    ALTER COLUMN coupon_redemption_request_id SET NOT NULL;

ALTER TABLE coupon_redemptions
    ADD CONSTRAINT uk_coupon_redemptions_request_id UNIQUE (coupon_redemption_request_id);
