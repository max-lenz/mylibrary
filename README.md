Library Management System Service
A RESTful API service for library database operations, managing books, readers, and book issuance. 
Built on Spring Boot 3 with a focus on security, performance profiling, and observability.

Tech Stack
Core: Java 21, Spring Boot 3.5.x
Data Access and Persistence: Spring Data JPA, Hibernate, JDBC Template, HikariCP
Databases: H2 (In-Memory for Dev), PostgreSQL (Int/Prod)
Database Migrations: Flyway
Security: Spring Security, JWT (jjwt 0.12.6), BCrypt
Resiliency and Performance: Spring AOP (Performance logging), Spring Retry
Observability and Logging: Spring Boot Actuator, Micrometer, Prometheus, Logstash Logback Encoder (JSON output)
Build Tools: Maven, Lombok, Git Commit ID Plugin

Features
Book Management (/api/books):
Paginated and sorted queries.
Filtering by title, author, ISBN, publication year range, and availability.
CRUD operations with validation.

Reader Management (/api/readers):
Reader registration and profile updates.
Validation of unique contact credentials (email, phone).

Loan Tracking (/api/loans):
Book issuance and return workflows.
Tracking overdue (/overdue) and lost (/lost) items.
Full loan history per reader.

Analytics and Health Metrics:
Aggregated library statistics (/api/statistics).
Custom DatabaseHealthIndicator monitoring DB connectivity latency and metadata.
Prometheus metric exports.

Access Control and Roles
The API uses stateless authentication via Spring Security (HTTP Basic / JWT).

Role Permissions:
ROLE_ADMIN: Full access to all endpoints, including DELETE operations and Actuator endpoints.
ROLE_LIBRARIAN: Operational access (read, write, update). No deletion rights.

Default Credentials (Dev):
Admin: admin / admin123
Librarian: librarian / admin123

REST API Reference
Books (/api/books):
GET /api/books (Roles: LIBRARIAN, ADMIN) - List books with pagination (page, size, sortBy, direction)
GET /api/books/search (Roles: LIBRARIAN, ADMIN) - Search by title, author, isbn, yearFrom, yearTo, availableOnly
GET /api/books/{id} (Roles: LIBRARIAN, ADMIN) - Get book details by ID
POST /api/books (Roles: LIBRARIAN, ADMIN) - Create a new book
PUT /api/books/{id} (Roles: LIBRARIAN, ADMIN) - Update book details
DELETE /api/books/{id} (Roles: ADMIN) - Delete book record

Readers (/api/readers):
GET /api/readers (Roles: LIBRARIAN, ADMIN) - List readers with pagination
GET /api/readers/{id} (Roles: LIBRARIAN, ADMIN) - Get reader details by ID
POST /api/readers (Roles: LIBRARIAN, ADMIN) - Register new reader
PUT /api/readers/{id} (Roles: LIBRARIAN, ADMIN) - Update reader information
DELETE /api/readers/{id} (Roles: ADMIN) - Delete reader profile

Book Loans (/api/loans):
POST /api/loans (Roles: LIBRARIAN, ADMIN) - Issue a book loan
GET /api/loans/reader/{readerId} (Roles: LIBRARIAN, ADMIN) - Get loan history for a specific reader
GET /api/loans/overdue (Roles: LIBRARIAN, ADMIN) - List overdue loans
GET /api/loans/lost (Roles: LIBRARIAN, ADMIN) - List lost loans

Analytics and Monitoring:
GET /api/statistics (Roles: LIBRARIAN, ADMIN) - Overall system statistics
GET /actuator/health (Access: Public) - System and DB health status
GET /actuator/prometheus (Roles: ADMIN) - Prometheus metrics endpoint
GET /actuator/info (Roles: ADMIN) - Build and Git commit metadata
GET /h2-console (Access: Public) - H2 Web Console (Dev profile only)

Environment Profiles
default (Dev):
Uses embedded H2 database (jdbc:h2:mem:devdb).
Automatic schema creation and seed data execution from data.sql.
H2 console enabled at /h2-console.

int / prod:
Connects to PostgreSQL using HikariCP connection pooling.
Schema migrations handled via Flyway (classpath:db/migration).
Active Prometheus metric exporter with production meter filters.
Retries transient database failures using @RetryOnDatabaseError.

Local Setup and Run
Prerequisites:
JDK 21
Maven 3.8+

Execution Steps:
Clone the repository:
git clone https://github.com/your-org/mylibrary.git
cd mylibrary

Build the application:
mvn clean package

Run in Dev mode (H2 Database):
java -jar target/mylibrary-0.0.1-SNAPSHOT.jar

Run in Production mode (PostgreSQL):
export DB_URL="jdbc:postgresql://localhost:5432/library"
export DB_USER="library_user"
export DB_PASSWORD="secure_password"
java -jar target/mylibrary-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
