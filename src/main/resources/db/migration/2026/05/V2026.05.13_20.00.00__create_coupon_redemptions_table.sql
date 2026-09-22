CREATE TABLE coupon_redemptions
(
    coupon_redemption_id         UUID PRIMARY KEY   DEFAULT gen_random_uuid(),
    coupon_id                    UUID      NOT NULL REFERENCES coupons (coupon_id) ON DELETE CASCADE,
    user_id                      UUID      NOT NULL,
    coupon_redemption_created_at TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE coupon_redemptions
    ADD CONSTRAINT uk_coupon_redemptions_coupon_user UNIQUE (coupon_id, user_id);
