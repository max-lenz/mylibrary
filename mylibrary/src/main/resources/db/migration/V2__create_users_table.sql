CREATE TABLE users (
    id       BIGSERIAL    PRIMARY KEY,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role     VARCHAR(20)  NOT NULL,
    enabled  BOOLEAN      NOT NULL DEFAULT true
);

INSERT INTO users (username, password, role, enabled) VALUES
('admin',     '$2a$10$hLVHgZaPDAHANdSJP7AqH.xZumu930p9pLEaeMveyybJS7HrOpCBO', 'ROLE_ADMIN',     true),
('librarian', '$2a$10$hLVHgZaPDAHANdSJP7AqH.xZumu930p9pLEaeMveyybJS7HrOpCBO', 'ROLE_LIBRARIAN', true);
-- Password: admin123