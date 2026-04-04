INSERT INTO books (version, title, author, isbn, total_copies, available_copies, publication_year) VALUES
(0, 'Effective Java', 'Joshua Bloch', '9780134685991', 5, 4, 2018),
(0, 'Clean Code', 'Robert C. Martin', '9780132350884', 3, 2, 2008);

INSERT INTO readers (version, first_name, last_name, email, phone, registration_date) VALUES
(0, 'John', 'Walker', 'john@example.com', '+49123456789', CURRENT_DATE),
(0, 'Anna', 'Smith', 'anna@example.com', '+49198765432', CURRENT_DATE);

INSERT INTO bookloans (version, book_id, reader_id, loan_date, due_date, loan_status) VALUES
(0, 1, 1, CURRENT_DATE, CURRENT_DATE + 14, 'ACTIVE'),
(0, 2, 2, CURRENT_DATE, CURRENT_DATE + 14, 'ACTIVE');

INSERT INTO users (username, password, role, enabled) VALUES
('admin',     '$2a$10$hLVHgZaPDAHANdSJP7AqH.xZumu930p9pLEaeMveyybJS7HrOpCBO', 'ROLE_ADMIN',     true),
('librarian', '$2a$10$hLVHgZaPDAHANdSJP7AqH.xZumu930p9pLEaeMveyybJS7HrOpCBO', 'ROLE_LIBRARIAN', true);
-- Password: admin123
