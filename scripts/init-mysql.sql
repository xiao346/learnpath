CREATE DATABASE IF NOT EXISTS learnpath
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'learnpath'@'localhost'
    IDENTIFIED BY 'learnpath123';

ALTER USER 'learnpath'@'localhost'
    IDENTIFIED BY 'learnpath123';

GRANT ALL PRIVILEGES ON learnpath.*
    TO 'learnpath'@'localhost';

FLUSH PRIVILEGES;
