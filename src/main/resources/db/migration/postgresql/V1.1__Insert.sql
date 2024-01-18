--  docker compose exec postgres bash
--  psql --dbname=zulieferer --username=zulieferer [--file=/sql/V1.1__Insert.sql]

-- COPY mit CSV-Dateien erfordert den Pfad src/main/resources/...
-- Dieser Pfad existiert aber nicht im Docker-Image
-- https://www.postgresql.org/docs/current/sql-copy.html
INSERT INTO zulieferer (id, version, name, email, baecker_id, erzeugt, aktualisiert)
VALUES
    -- admin
    ('185e1718-99ff-4c20-a373-d853e7baaf86',0,'Admin','arip@acme.com','00000000-0000-0000-0000-000000000020', '2022-01-31 00:00:00','2022-01-31 00:00:00'),
    -- HTTP GET
    ('a3c91e0e-6a53-4905-bbe4-6eecc3960fed',0,'Alpha','arep@acme.de','00000000-0000-0000-0000-000000000001','2022-01-01 00:00:00','2022-01-01 00:00:00');

INSERT INTO filiale (id, name, standort, zulieferer_id)
VALUES
  ('496230d9-2c9f-40a8-8fd7-2f08fe91c982','Willy','Aachen','185e1718-99ff-4c20-a373-d853e7baaf86'),
  ('20532be3-bea2-42a5-a924-d70bc35f3822','Wonka','Augsburg','a3c91e0e-6a53-4905-bbe4-6eecc3960fed');
