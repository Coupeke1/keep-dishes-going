INSERT INTO restaurant_orders (order_id,
                               restaurant_id,
                               customer_name,
                               customer_email,
                               delivery_street,
                               delivery_house_number,
                               delivery_bus_number,
                               delivery_country,
                               delivery_city,
                               delivery_postal_code,
                               status,
                               decision_reason,
                               created_at)
VALUES ('11111111-1111-1111-1111-111111111111', -- voorbeeld order_id
        '22222222-2222-2222-2222-222222222222', -- voorbeeld restaurant_id
        'Test Klant', -- customer_name
        'klant@test.be', -- customer_email
        'Kerkstraat', -- delivery_street
        '12', -- delivery_house_number
        NULL, -- delivery_bus_number
        'België', -- delivery_country
        'Antwerpen', -- delivery_city
        '2000', -- delivery_postal_code
        'ACCEPTED', -- status (nodig om markReady te laten werken)
        NULL, -- decision_reason
        NOW() -- created_at
       );
