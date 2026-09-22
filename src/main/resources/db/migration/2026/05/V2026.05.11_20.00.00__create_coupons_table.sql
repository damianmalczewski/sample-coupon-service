CREATE TABLE coupons
(
    coupon_id           UUID PRIMARY KEY      DEFAULT gen_random_uuid(),
    coupon_code         VARCHAR(255) NOT NULL,
    coupon_usage_limit  INTEGER      NOT NULL,
    coupon_usage_count  INTEGER      NOT NULL DEFAULT 0,
    coupon_country_code VARCHAR(8)   NOT NULL,
    coupon_created_at   TIMESTAMP    NOT NULL DEFAULT now()
);

ALTER TABLE coupons
    ADD CONSTRAINT uk_coupons_coupon_code UNIQUE (coupon_code);
