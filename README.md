# Library Management System Service

A RESTful API service for library database operations: managing books, readers and book issuance.
Built on Spring Boot 3 with a focus on security, performance profiling and observability.

*Kurzbeschreibung (Deutsch):* REST-API zur Bibliotheksverwaltung (Bücher, Leser, Ausleihen) mit Java 21 und Spring Boot 3. Schwerpunkte: Authentifizierung und Rollen mit Spring Security, Datenbankmigration mit Flyway und Monitoring mit Spring Boot Actuator und Prometheus.

## Tech Stack

| Area | Technologies |
|---|---|
| Core | Java 21, Spring Boot 3.5.x |
| Data access | Spring Data JPA, Hibernate, JDBC Template, HikariCP |
| Databases | H2 (in-memory, dev), PostgreSQL (int/prod) |
| Migrations | Flyway |
| Security | Spring Security (HTTP Basic), BCrypt |
| Resiliency and performance | Spring AOP (performance logging), Spring Retry |
| Observability and logging | Spring Boot Actuator, Micrometer, Prometheus, Logstash Logback Encoder (JSON output) |
| Build tools | Maven, Lombok, Git Commit ID Plugin |

## Features

**Book management** (`/api/books`)
- Paginated and sorted queries
- Filtering by title, author, ISBN, publication year range and availability
- CRUD operations with validation

**Reader management** (`/api/readers`)
- Reader registration and profile updates
- Validation of unique contact credentials (email, phone)

**Loan tracking** (`/api/loans`)
- Book issuance and return workflows
- Tracking of overdue (`/overdue`) and lost (`/lost`) items
- Full loan history per reader

**Analytics and health metrics**
- Aggregated library statistics (`/api/statistics`)
- Custom `DatabaseHealthIndicator` monitoring DB connectivity latency and metadata
- Prometheus metric exports

## Access Control and Roles

The API uses stateless authentication via Spring Security (HTTP Basic).

| Role | Permissions |
|---|---|
| `ROLE_ADMIN` | Full access to all endpoints, including DELETE operations and Actuator endpoints |
| `ROLE_LIBRARIAN` | Operational access (read, write, update), no deletion rights |

Default credentials (dev profile only):
- Admin: `admin` / `admin123`
- Librarian: `librarian` / `admin123`

## REST API Reference

### Books (`/api/books`)

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/api/books` | LIBRARIAN, ADMIN | List books with pagination (`page`, `size`, `sortBy`, `direction`) |
| GET | `/api/books/search` | LIBRARIAN, ADMIN | Search by `title`, `author`, `isbn`, `yearFrom`, `yearTo`, `availableOnly` |
| GET | `/api/books/{id}` | LIBRARIAN, ADMIN | Get book details by ID |
| POST | `/api/books` | LIBRARIAN, ADMIN | Create a new book |
| PUT | `/api/books/{id}` | LIBRARIAN, ADMIN | Update book details |
| DELETE | `/api/books/{id}` | ADMIN | Delete book record |

### Readers (`/api/readers`)

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| GET | `/api/readers` | LIBRARIAN, ADMIN | List readers with pagination |
| GET | `/api/readers/{id}` | LIBRARIAN, ADMIN | Get reader details by ID |
| POST | `/api/readers` | LIBRARIAN, ADMIN | Register new reader |
| PUT | `/api/readers/{id}` | LIBRARIAN, ADMIN | Update reader information |
| DELETE | `/api/readers/{id}` | ADMIN | Delete reader profile |

### Book Loans (`/api/loans`)

| Method | Endpoint | Roles | Description |
|---|---|---|---|
| POST | `/api/loans` | LIBRARIAN, ADMIN | Issue a book loan |
| GET | `/api/loans/reader/{readerId}` | LIBRARIAN, ADMIN | Loan history for a specific reader |
| GET | `/api/loans/overdue` | LIBRARIAN, ADMIN | List overdue loans |
| GET | `/api/loans/lost` | LIBRARIAN, ADMIN | List lost loans |

### Analytics and Monitoring

| Method | Endpoint | Access | Description |
|---|---|---|---|
| GET | `/api/statistics` | LIBRARIAN, ADMIN | Overall system statistics |
| GET | `/actuator/health` | Public | System and DB health status |
| GET | `/actuator/prometheus` | ADMIN | Prometheus metrics endpoint |
| GET | `/actuator/info` | ADMIN | Build and Git commit metadata |
| GET | `/h2-console` | Public | H2 web console (dev profile only) |

## Environment Profiles

**default (dev)**
- Uses embedded H2 database (`jdbc:h2:mem:devdb`)
- Automatic schema creation and seed data from `data.sql`
- H2 console enabled at `/h2-console`

**int / prod**
- Connects to PostgreSQL using HikariCP connection pooling
- Schema migrations via Flyway (`classpath:db/migration`)
- Active Prometheus metric exporter with production meter filters
- Retries transient database failures using `@RetryOnDatabaseError`

## Local Setup and Run

**Prerequisites:** JDK 21, Maven 3.8+

1. Clone the repository:

   ```bash
   git clone https://github.com/max-lenz/mylibrary.git
   cd mylibrary/mylibrary
   ```

2. Build the application:

   ```bash
   mvn clean package
   ```

3. Run in dev mode (H2 database):

   ```bash
   java -jar target/mylibrary-0.0.1-SNAPSHOT.jar
   ```

4. Run in production mode (PostgreSQL):

   ```bash
   export DB_URL="jdbc:postgresql://localhost:5432/library"
   export DB_USER="library_user"
   export DB_PASSWORD="secure_password"
   java -jar target/mylibrary-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
   ```
