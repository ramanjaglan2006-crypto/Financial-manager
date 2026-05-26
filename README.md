# Personal Finance Manager API

A complete, production-grade REST API for managing personal finances. Built using Spring Boot 3, Java 17, Spring Security, Spring Data JPA, and H2 database.

## Features

- **User Authentication**: Secure session-based authentication using HTTP-only cookies and BCrypt password hashing.
- **Transaction Management**: Track income and expenses with detailed categorizations.
- **Category Management**: System defaults (Salary, Rent, etc.) and custom user-defined categories.
- **Savings Goals**: Track savings goals with progress calculations over time.
- **Reporting**: Generate monthly and yearly financial reports.

## Architecture

This project follows a strict layered architecture:
- `Controller Layer`: Exposes REST endpoints, validates input, handles session context.
- `Service Layer`: Contains pure business logic.
- `Repository Layer`: Interfaces with the database using Spring Data JPA.
- `DTO Pattern`: Strictly decouples API contracts from database entities using Java Records.
- `Global Exception Handling`: Uses `@ControllerAdvice` for unified JSON error structures.

## Requirements

- Java 17+
- Maven 3.8+

## Setup & Run

1. **Clone the repository**:
   ```bash
   git clone <repo-url>
   cd financial_sys_manager-main
   ```

2. **Run using Maven**:
   ```bash
   ./mvnw spring-boot:run
   ```

3. **Run using Docker**:
   ```bash
   docker build -t finance-manager .
   docker run -p 8080:8080 finance-manager
   ```

## API Documentation

Once the application is running, Swagger UI is available at:
`http://localhost:8080/swagger-ui.html`

OpenAPI specification JSON:
`http://localhost:8080/api-docs`

H2 Database Console:
`http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:financedb`
- **Username**: `sa`
- **Password**: `password`

## Deployment

The application is configured to run on Render or similar platforms out-of-the-box using the provided Dockerfile. 

Make sure to override environment variables for a real PostgreSQL database in a production environment:
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `SERVER_SERVLET_SESSION_COOKIE_SECURE=true`
