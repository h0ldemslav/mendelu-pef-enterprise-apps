INSERT INTO public.aircraft VALUES (1, 'SI0O6', 'Airbus A380', 16, 30, 125);
INSERT INTO public.airport VALUES (1, 'BQK', 'Brunswick Golden Isles Airport', 'US', 'US-GA', 'Brunswick', 'KBQK');
INSERT INTO public.airport VALUES (2, 'TKD', 'Takoradi Airport', 'GH', 'GH-WP', 'Sekondi-Takoradi', 'DGTK');

-- `id` is the last column in `customer` table!
INSERT INTO public.customer
VALUES ('Marry', 'Smith', 2999.81, '+351 326 363 8324', 'marryS10@reverbnation.com',
        '$2a$04$bWi8kKs0kLQi9ao03TrWeemIniME785I3uMISQRfibYwyhl55SJMi', '35645ea7-2b38-430d-aab8-f72302cdc2c8');

INSERT INTO public.customer
VALUES ('Ben', 'Rope', 501.81, '+42 326 363 8324', 'benRO11P@reverbnation.com',
        '$2a$04$Pu.MMm4WemMDrb5ckNdMF.GzwQ3PVaMCpavhRccDzN9NANFsnKOXu', '0931192f-35a2-461b-bc85-133290c63c67');

INSERT INTO public.customer
VALUES ('John', 'Doe', 871.81, '+1 326 363 8324', 'johnnyD222@reverbnation.com',
        '$2a$04$e66c9HczLQxSiRWqyMN91uIoadNMW86hPi/xq0RGb40P6/Pi0fepu', 'da8d38a8-28a7-4740-8096-f0197ebabd5c');

INSERT INTO public.fare_tariff VALUES (1, 'AB99', 5513.0, 1326.0, 624.0);
INSERT INTO public.fare_tariff VALUES (2, 'SK91', 10513.0, 1726.0, 737.0);
INSERT INTO public.fare_tariff VALUES (3, 'JJ51', 9670.0, 2026.0, 791.0);

INSERT INTO public.flight VALUES (1, 'AA0718',
                                  TO_TIMESTAMP('2017-07-16 09:35:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  TO_TIMESTAMP('2017-07-16 10:30:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  'Scheduled', NULL, 1, 1, 2, 1);
INSERT INTO public.flight VALUES (2, 'BB0531',
                                  TO_TIMESTAMP('2017-07-16 16:35:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  TO_TIMESTAMP('2017-07-16 20:30:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  'Scheduled', NULL, 1, 2, 1, 2);

INSERT INTO public.ticket VALUES (1, 0008386672215, 'Business', 5513.0, 0.0, 5513.0, '1A', 'Marry Smith',
                                  TO_TIMESTAMP('2017-07-16 09:35:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  TO_TIMESTAMP('2017-07-16 10:30:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  1, '35645ea7-2b38-430d-aab8-f72302cdc2c8');
INSERT INTO public.ticket VALUES (2, 0003099269080, 'Premium', 1326.0, 0.0, 1326.0, '17A', 'Ben Rope',
                                  TO_TIMESTAMP('2017-07-16 09:35:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  TO_TIMESTAMP('2017-07-16 10:30:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  1, '0931192f-35a2-461b-bc85-133290c63c67');
INSERT INTO public.ticket VALUES (3, 0001901266072, 'Economy', 737.0, 0.0, 737.0, '31A', 'John Doe',
                                  TO_TIMESTAMP('2017-07-16 16:35:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  TO_TIMESTAMP('2017-07-16 20:30:00', 'YYYY-MM-DD HH24:MI:SS'),
                                  2, 'da8d38a8-28a7-4740-8096-f0197ebabd5c');