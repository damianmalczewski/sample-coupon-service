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

------------------------------------------------------------------------------------------------------------------------
-- 2000 bulk coupons for paging and filtering: BULK0001..BULK2000, usage limit 1-10, every fifth one in DE. Created 1 ms
-- apart after the coupons above - the list orders by creation time only, equal timestamps would make paging unstable.

--DO
--$$
--    BEGIN
--        FOR i IN 1..2000
--            LOOP
--                INSERT INTO coupons (coupon_code, coupon_usage_limit, coupon_usage_count, coupon_country_code,
--                                     coupon_created_at)
--                VALUES
--                    (
--                        'BULK' || lpad(i::text, 4, '0'),
--                        i % 10 + 1,
--                        0,
--                        CASE WHEN i % 5 = 0 THEN 'DE' ELSE 'PL' END,
--                        now() + i * INTERVAL '1 millisecond'
--                    )
--                ON CONFLICT DO NOTHING;
--            END LOOP;
--    END
--$$;
