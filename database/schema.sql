-- Optional: Hibernate creates all tables automatically (ddl-auto=update).
-- Run this only to create the empty database manually.
CREATE DATABASE IF NOT EXISTS bankdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bankdb;

-- Handy queries while learning:
-- SELECT * FROM users;
-- SELECT * FROM accounts;
-- SELECT * FROM transactions ORDER BY created_at DESC;
