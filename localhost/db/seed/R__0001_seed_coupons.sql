-- application stores timestamps in UTC; DEFAULT now() would otherwise follow the JVM time zone of the session
SET LOCAL TIME ZONE 'UTC';

INSERT INTO coupons (coupon_id, coupon_code, coupon_usage_limit, coupon_usage_count, coupon_country_code)
VALUES
    ('019a0000-0000-7000-8000-000000000001', 'WELCOME10', 100, 0, 'PL')
ON CONFLICT DO NOTHING;

INSERT INTO coupons (coupon_id, coupon_code, coupon_usage_limit, coupon_usage_count, coupon_country_code)
VALUES
    ('019a0000-0000-7000-8000-000000000002', 'SUMMER25', 5, 0, 'PL')
ON CONFLICT DO NOTHING;

INSERT INTO coupons (coupon_id, coupon_code, coupon_usage_limit, coupon_usage_count, coupon_country_code)
VALUES
    ('019a0000-0000-7000-8000-000000000003', 'ONESHOT', 1, 1, 'PL')
ON CONFLICT DO NOTHING;

INSERT INTO coupon_redemptions (coupon_redemption_id, coupon_id, user_id, coupon_redemption_request_id)
VALUES
    (
        '019a0000-0000-7000-8000-000000000101',
        '019a0000-0000-7000-8000-000000000003',
        '019a0000-0000-7000-8000-000000001001',
        '019a0000-0000-7000-8000-000000010001'
    )
ON CONFLICT DO NOTHING;

INSERT INTO coupons (coupon_id, coupon_code, coupon_usage_limit, coupon_usage_count, coupon_country_code)
VALUES
    ('019a0000-0000-7000-8000-000000000004', 'BERLIN15', 50, 0, 'DE')
ON CONFLICT DO NOTHING;
