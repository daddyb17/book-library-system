# Library System REST API Plan

## Summary
- Build a Java 17 Spring Boot 3 REST API on the existing Gradle skeleton to manage borrowers, books, and active borrow/return operations.
- Use URL-based versioning with `/api/v1` and page-based pagination for book listing.
- Target PostgreSQL as the production database and keep H2 for local development convenience; justify PostgreSQL in the docs as the better production-style relational choice for integrity, locking, and predictable transaction behavior.
- Deliver core API behavior, validation, error handling, unit/integration tests, API docs, a Postman collection, Docker packaging, and a declarative GitHub Actions CI workflow.

## Implementation Changes
- Add domain layers for `Borrower`, `Book`, and `Loan`.
  - `Borrower`: `id`, `name`, `email` with unique email constraint.
  - `Book`: `id`, `isbn`, `title`, `author`; allow multiple rows with the same ISBN, but reject creation if an existing ISBN is paired with a different title or author.
  - `Loan`: `id`, `borrowerId`, `bookId`, `borrowedAt`, `returnedAt`; enforce at most one active loan per `bookId`.
- Expose these REST endpoints:
  - `POST /api/v1/borrowers` to register a borrower.
  - `POST /api/v1/books` to register a book copy.
  - `GET /api/v1/books?page=0&size=20&sort=title,asc` to list books with pagination and availability in the response.
  - `POST /api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}` to borrow a book copy.
  - `DELETE /api/v1/borrowers/{borrowerId}/borrowed-books/{bookId}` to return a borrowed book copy.
- Use request/response DTOs with Bean Validation and a centralized `@RestControllerAdvice` that returns consistent `ProblemDetail`-style errors for validation failures, missing resources, duplicate email, ISBN mismatch, and already-borrowed book conflicts.
- Configure multiple environments with Spring profiles:
  - `local`: H2 for quick startup.
  - `test`: isolated test configuration.
  - `prod`: PostgreSQL driven by environment variables.
- Add OpenAPI/Swagger documentation, a checked-in Postman collection, Docker support, and a GitHub Actions workflow that runs tests and the Gradle build on Java 17.

## Public Interfaces And Defaults
- API versioning is path-based only in this iteration: `/api/v1/...`.
- Book list pagination defaults: `page=0`, `size=20`, maximum `size=100`, default sort `title,asc`.
- Borrow and return responses should include borrower id, book id, operation timestamp, and current status.
- Availability is derived from active loans, not stored as a separate mutable flag.
- Configuration should be externalized through environment variables in production to align with 12-factor expectations where practical.

## Test Plan
- Unit tests for service-layer rules:
  - unique borrower email, ISBN/title/author consistency, borrow blocked when book is already on active loan, return blocked when no matching active loan exists.
- Controller tests with MockMvc for happy paths and error payloads on invalid input, 404s, and 409 conflicts.
- Persistence/integration tests covering active-loan enforcement and repository behavior against PostgreSQL-compatible behavior.
- Build verification in CI: Gradle test suite, application build, and Docker image build.

## Assumptions
- A borrower may borrow multiple different book copies at the same time; only the same `bookId` is restricted to one active borrower.
- No authentication, authorization, due dates, reservations, fines, or borrowing limits are in scope.
- Returning a book requires an active loan for the same borrower and book copy.
- Docker and CI are in scope now; Kubernetes manifests are deferred.
- Documentation deliverables include `README.md` usage instructions, environment setup, API examples, database choice rationale, assumptions, and the GitHub submission note.
