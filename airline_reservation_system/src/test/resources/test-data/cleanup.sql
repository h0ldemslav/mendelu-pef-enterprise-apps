-- Delete data from all tables to have a fresh test environment ---

TRUNCATE TABLE ticket CASCADE;
TRUNCATE TABLE flight CASCADE;
TRUNCATE TABLE fare_tariff CASCADE;
TRUNCATE TABLE customer CASCADE;
TRUNCATE TABLE airport CASCADE;
TRUNCATE TABLE aircraft CASCADE;