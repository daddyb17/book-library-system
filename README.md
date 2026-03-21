# Library System API

Production-style REST API for managing borrowers, books, and borrowing operations in a simple library system.

## Stack
- Java 17
- Spring Boot 3.5
- Spring Web, Validation, Data JPA
- Flyway for schema migrations
- H2 for local/test execution
- PostgreSQL for production-style deployment

## Why PostgreSQL
PostgreSQL is the primary database target because it gives stronger production characteristics than an embedded database: better transactional guarantees, predictable locking behavior for borrow/return workflows, and a clear upgrade path for real deployments. H2 is still included for local development and test speed.

## Features
- API versioning through `/api/v1`
- Borrower registration with unique email enforcement
- Book registration with schema-backed ISBN consistency validation
- Multiple copies supported through unique book ids
- Pagination and sorting for book listing
- Borrow and return workflows with active-loan enforcement
- Centralized `ProblemDetail` error responses
- Swagger UI and OpenAPI output
- Docker packaging and GitHub Actions CI

## Running locally
1. Start the application with the default local profile:

```bash
./gradlew bootRun
```

2. Open the API docs:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)
- H2 console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)

## Running with PostgreSQL
Use Docker Compose to launch PostgreSQL and the API with the `prod` profile:

```bash
docker compose up --build
```

The API will be available at [http://localhost:8080](http://localhost:8080).

## Profiles
- `local`: in-memory H2, enabled by default
- `test`: isolated H2 configuration for automated tests
- `prod`: PostgreSQL driven by `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`

## API summary
- `POST /api/v1/borrowers`
- `POST /api/v1/books`
- `GET /api/v1/books?page=0&size=20&sort=title,asc`
- `POST /api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}`
- `DELETE /api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}`

## Sample requests

### Register borrower
```json
{
  "name": "Alice Johnson",
  "email": "alice@example.com"
}
```

### Register book
```json
{
  "isbn": "9780132350884",
  "title": "Clean Code",
  "author": "Robert C. Martin"
}
```

## Pagination defaults
- Default page: `0`
- Default size: `20`
- Maximum size: `100`
- Default sort: `title,asc`
- Allowed sort fields: `id`, `isbn`, `title`, `author`, `createdAt`

## Testing
Run the full verification suite with:

```bash
./gradlew clean test
```

The test suite includes service-level unit tests, controller tests with MockMvc, and an integration workflow test that verifies a book copy cannot be borrowed twice at the same time.

## Postman
The Postman collection is checked in at [postman/Library System API.postman_collection.json](postman/Library%20System%20API.postman_collection.json).

## Assumptions
- A borrower may borrow multiple different book copies at the same time.
- Only one active borrower may hold a specific `bookId` at a time.
- Returning a book requires an active loan for the same borrower and book copy.
- Authentication, authorization, due dates, reservations, fines, and borrowing limits are out of scope.

## Submission note
Push the repository to GitHub and share the repository URL as the final submission artifact.
