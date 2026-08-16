# Flip Skateshop - Backend

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.20-blue.svg?logo=kotlin)](https://kotlinlang.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg?logo=springboot)](https://spring.io/projects/spring-boot)
[![WebFlux](https://img.shields.io/badge/Spring-WebFlux%20(Reactive)-green.svg)](https://docs.spring.io/spring-framework/reference/web/webflux.html)
[![MongoDB](https://img.shields.io/badge/Database-MongoDB%20Reactive-green.svg?logo=mongodb)](https://www.mongodb.com)
[![MinIO](https://img.shields.io/badge/Storage-MinIO%20S3-red.svg?logo=minio)](https://min.io/)
[![Stripe](https://img.shields.io/badge/Payment-Stripe%20API-6772E5.svg?logo=stripe)](https://stripe.com)
[![Frontend](https://img.shields.io/badge/Frontend-flip__frontend-orange.svg?logo=github)](https://github.com/thomas-btst/flip_frontend)
[![License](https://img.shields.io/badge/License-Educational-lightgrey.svg)]()

A modern, reactive, and robust backend for the **Flip Skateshop** e-commerce platform.  
Built with **Kotlin** and **Spring Boot 3 (WebFlux / Coroutines)**, this application provides an end-to-end non-blocking reactive architecture handling authentication, product catalog management, shopping carts, Stripe payments, invoicing, and MinIO S3-compatible object storage.

> **Frontend Application**: The client-facing and back-office user interfaces are developed in a separate repository: [flip_frontend](https://github.com/thomas-btst/flip_frontend).

## Table of Contents

1. [Features](#features)
2. [Architecture and Tech Stack](#architecture-and-tech-stack)
3. [Frontend and Backend Ecosystem](#frontend-and-backend-ecosystem)
4. [Environment Variables and Configuration](#environment-variables-and-configuration)
5. [Getting Started](#getting-started)
6. [Testing Strategy and Code Quality](#testing-strategy-and-code-quality)
7. [OpenAPI Documentation (Swagger)](#openapi-documentation-swagger)
8. [Contributing](#contributing)
9. [Author and License](#author-and-license)

## Features

### 1. Authentication and Security
- **Stateless JWT Architecture**: Signed short-lived access tokens paired with rotating database-persisted refresh tokens.
- **Account Lifecycle**:
  - Registration with password strength validation and email activation.
  - One-time time-limited activation keys.
  - Secure password reset flow with time-limited email verification links.
- **Brute-Force Protection (Rate Limiting)**: **Resilience4j** rate limiters applied on sensitive authentication and credential modification endpoints.
- **Role-Based Access Control (RBAC)**: Distinct user privileges (`ROLE_USER`, `ROLE_ADMIN`) protecting administrative routes.

### 2. Reactive Product Catalog
- Domain model for skateboard categories: *Complete Skateboards, Decks, Wheels, Trucks, Bearings, Grip Tapes*.
- Multi-criteria search, category filtering, and optimized reactive pagination.
- Full administrative CRUD operations with product picture uploads and inventory management.

### 3. Reactive Shopping Cart
- Dynamic, persistent cart per user account.
- Item addition, quantity updates, removal, and complete cart reset.
- Availability validation and automated total price calculations.

### 4. Order Management and Stripe Payments
- Secure checkout session generation using the **Stripe Checkout API**.
- Complete order lifecycle handling: `PENDING`, `PAID`, `SHIPPED`, `DELIVERED`, `CANCELED`.
- Dynamic, custom HTML/PDF invoice generation rendered with **Thymeleaf**.
- Automated order confirmation email dispatch with invoice reference.

### 5. Administration and Analytics
- Administrative statistics and dashboard endpoint (`/commands/stats`): revenue metrics, total orders, average cart size, and order volume.
- User management and order status updates.

### 6. MinIO S3 Object Storage
- File hosting and distribution for product images, user avatars, and invoice documents.
- Automatic bucket provisioning and access policy setup upon application startup.

### 7. Emailing and HTML Templates
- Transactional email dispatch via **JavaMailSender**.
- Responsive, styled Thymeleaf HTML email templates:
  - `activateAccountEmail.html` (Account activation)
  - `resetPasswordEmail.html` (Password reset)
  - `commandConfirmationEmail.html` (Order confirmation with invoice)

### 8. Dynamic Database Seeder
- Realistic sample data generation using **Java Faker**.
- Granular collection seeding selectable via command-line arguments (`users`, `products`, `admin`).

## Architecture and Tech Stack

The project is built on the **Reactive Streams** paradigm combined with **Kotlin Coroutines** for high concurrency, non-blocking I/O, and maintainability:

```
      +-----------------------------------------------------------+
      |                 Clients (Web / Mobile)                   |
      +-----------------------------------------------------------+
                                  |  HTTP/REST (JSON / Multipart)
                                  v
+-----------------------------------------------------------------------+
|  Spring WebFlux Controller Layer (Reactive Endpoints & Suspend Fun)   |
+-----------------------------------------------------------------------+
                                  |
                                  v
+-----------------------------------------------------------------------+
|           Service Layer (Business Logic & Coroutines Reactor)          |
+-----------------------------------------------------------------------+
         |                        |                          |
         v                        v                          v
+------------------+    +-------------------+    +----------------------+
| Mongo Repository |    |   MinIO Service   |    |    Stripe Service    |
| (Reactive Driver)|    |   (S3 Storage)    |    |   (Checkout API)     |
+------------------+    +-------------------+    +----------------------+
         |                        |                          |
         v                        v                          v
    [ MongoDB ]               [ MinIO ]                 [ Stripe ]
```

### Core Technologies
- **Language**: Kotlin 2.0 (JVM 21) & Kotlin Coroutines (`kotlinx-coroutines-reactor`)
- **Framework**: Spring Boot 3.3.5 (Spring WebFlux, Spring Security 6, Spring Data MongoDB Reactive)
- **Database**: MongoDB 7 (Reactive Driver)
- **Object Storage**: MinIO (S3 Compatible)
- **Payments**: Stripe Java SDK
- **Resilience**: Resilience4j (Rate Limiter & Retry)
- **Template Engine**: Thymeleaf
- **API Documentation**: SpringDoc OpenAPI 2 / Swagger UI
- **Testing & Quality**: JUnit 5, MockK, WebTestClient, JaCoCo, Ktlint

## Frontend and Backend Ecosystem

This backend API is designed to work in conjunction with the frontend client application:

- **Frontend Repository**: [https://github.com/thomas-btst/flip_frontend](https://github.com/thomas-btst/flip_frontend)
- **Communication**: REST API via WebFlux (default port `8080`), secured with JWT Bearer tokens.
- **Client Origin (CORS)**: Configured by default for `http://localhost:5173`.

## Environment Variables and Configuration

The application can be configured through system environment variables, command-line arguments, or the `application.yml` file.

> **Important**: For security reasons, `application.yml` files are excluded from Git version control and not tracked in the repository. You must copy the provided template files before launching the app or executing tests:
> ```bash
> cp src/main/resources/application.yml.example src/main/resources/application.yml
> cp src/test/resources/application.yml.example src/test/resources/application.yml
> ```

### Supported Configuration Properties

| Environment Variable | Spring Property (`application.yml`) | Description | Default Value (Dev) |
| :--- | :--- | :--- | :--- |
| `SERVER_PORT` | `server.port` | HTTP server listening port | `8080` |
| `SPRING_DATA_MONGODB_HOST` | `spring.data.mongodb.host` | MongoDB server host | `localhost` |
| `SPRING_DATA_MONGODB_PORT` | `spring.data.mongodb.port` | MongoDB server port | `27017` |
| `SPRING_DATA_MONGODB_DATABASE` | `spring.data.mongodb.database` | MongoDB database name | `flip` |
| `SPRING_DATA_MONGODB_USERNAME` | `spring.data.mongodb.username` | MongoDB username | `mongoadmin` |
| `SPRING_DATA_MONGODB_PASSWORD` | `spring.data.mongodb.password` | MongoDB password | `secret` |
| `SPRING_MAIL_HOST` | `spring.mail.host` | SMTP mail server host | `smtp.gmail.com` |
| `SPRING_MAIL_PORT` | `spring.mail.port` | SMTP port (TLS) | `587` |
| `SPRING_MAIL_USERNAME` | `spring.mail.username` | Sender email address | `flip.skateshop.noreply@gmail.com` |
| `SPRING_MAIL_PASSWORD` | `spring.mail.password` | SMTP application password | `dummy-smtp-password-1234` |
| `SKATESHOP_CLIENT_URL` | `skateshop.client.url` | Frontend client origin (CORS / redirects) | `http://localhost:5173` |
| `SKATESHOP_SECURITY_JWT_SECRET_KEY` | `skateshop.security.jwt.secret-key` | HMAC secret key (min. 32 chars) | *Default dev secret* |
| `SKATESHOP_STRIPE_PRIVATE_KEY` | `skateshop.stripe.privateKey` | Stripe secret API key (`sk_test_...`) | *Mock test key* |
| `SKATESHOP_MINIO_ENDPOINT` | `skateshop.minio.endpoint` | MinIO server URL | `http://localhost:9000` |
| `SKATESHOP_MINIO_ACCESS_KEY` | `skateshop.minio.access-key` | MinIO access key | `minioadmin` |
| `SKATESHOP_MINIO_SECRET_KEY` | `skateshop.minio.secret-key` | MinIO secret key | `password` |
| `SKATESHOP_MINIO_BUCKET` | `skateshop.minio.bucket` | MinIO S3 bucket name | `flip` |

### Setting Environment Variables

When running locally with Docker Compose, all local infrastructure (MongoDB, MinIO) and internal tokens (JWT) work out of the box with default development values. Only the following **3 variables are strictly required** to enable external third-party integrations (Stripe payments and email notifications):

- `SKATESHOP_STRIPE_PRIVATE_KEY`: Your Stripe secret key (e.g. `sk_test_...`) to create checkout sessions.
- `SPRING_MAIL_USERNAME`: Your Gmail address used to dispatch account activation, password reset, and invoice emails.
- `SPRING_MAIL_PASSWORD`: Your Gmail application password (16-character token generated from Google Account Security).

#### Option 1: Terminal Export (Linux / macOS)
```bash
export SKATESHOP_STRIPE_PRIVATE_KEY="sk_test_your_stripe_secret_key"
export SPRING_MAIL_USERNAME="your-address@gmail.com"
export SPRING_MAIL_PASSWORD="your-smtp-app-password"
./mvnw spring-boot:run
```

#### Option 2: Maven Command-Line Arguments
```bash
./mvnw spring-boot:run -Dspring-boot.run.arguments="--skateshop.stripe.privateKey=sk_test_xxx --spring.mail.username=your-address@gmail.com --spring.mail.password=your-app-password"
```

#### Option 3: IDE Run Configuration (IntelliJ IDEA / VS Code)
- **IntelliJ IDEA**: `Run` > `Edit Configurations...` > Select `Spring Boot` / `Application` > Add keys to **Environment variables**.

## Getting Started

### Prerequisites
- **[Java 21 JDK](https://www.oracle.com/fr/java/technologies/downloads/#java21)** (or higher)
- **[Docker and Docker Compose](https://docs.docker.com/compose/)**

### Setup Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/thomas-btst/flip_backend.git
   cd flip_backend
   ```

2. **Create configuration files from examples**
   ```bash
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   cp src/test/resources/application.yml.example src/test/resources/application.yml
   ```

3. **Start MongoDB and MinIO services with Docker Compose**
   ```bash
   docker compose up -d
   ```
   *This starts MongoDB on port `27017` and MinIO on `9000` (API) and `8900` (Web Console).*

4. **Populate database with the Seeder (Optional)**
   ```bash
   # Generates 5 users, 200 products, and the default admin (admin@flip.fr / admin)
   ./mvnw spring-boot:run -Dspring-boot.run.arguments="--seed"
   ```
   > Tip: To seed specific collections only:
   > ```bash
   > ./mvnw spring-boot:run -Dspring-boot.run.arguments="--seed=users,products"
   > ```

5. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```
   *The API will be available at `http://localhost:8080`.*

## Testing Strategy and Code Quality

This project emphasizes reliability, security, and maintainability through a comprehensive automated testing suite:

```
                   /\
                  /  \     E2E & Controller Tests (WebTestClient)
                 /----\    HTTP endpoints, JWT security, DTO validations
                /      \
               /--------\  Repository Integration Tests
              /          \ Real reactive MongoDB queries & aggregations
             /------------\
            /              \ Service Unit Tests (MockK)
           /________________\ Business logic & mock isolation
```

### Implemented Test Types

1. **Controller & E2E Tests (`src/test/kotlin/.../controller/`)**:
   - Executed using reactive `WebTestClient`.
   - Validates HTTP response codes (`200 OK`, `201 Created`, `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Not Found`).
   - Verifies `Authorization: Bearer <JWT>` headers, request payload validations (`@Valid`), and Resilience4j rate limiting.

2. **Repository Integration Tests (`src/test/kotlin/.../repository/`)**:
   - Executed against a dedicated test MongoDB instance (`flip_test`).
   - Validates custom queries, date filtering, average score aggregations, and refresh token atomic operations.

3. **Service Unit Tests (`src/test/kotlin/.../service/`)**:
   - Strict business logic isolation using **MockK**.
   - Tests cart operations, password hashing, email sending, and Stripe checkout session flows.

4. **Test Isolation and Idempotency (`ServicesCleaner`)**:
   - The `ServicesCleaner` utility automatically resets database collections and service states between test runs, ensuring fully deterministic results.

### Useful Testing and Quality Commands

- **Run all tests:**
  ```bash
  ./mvnw clean test
  ```

- **Run a specific test class:**
  ```bash
  ./mvnw test -Dtest=AuthenticationControllerTest
  ```

- **Generate code coverage report (JaCoCo):**
  ```bash
  ./mvnw jacoco:report
  ```
  *The HTML report is generated at `target/site/jacoco/index.html`.*

- **Check Kotlin code style (Ktlint):**
  ```bash
  ./mvnw ktlint:check
  ```

## OpenAPI Documentation (Swagger)

Once the application is running, interactively explore and test all API endpoints using Swagger UI:

- **Swagger UI URL**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI Specification (JSON)**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

> **Authentication in Swagger**: Click the `Authorize` button at the top right and enter your JWT token as `Bearer <token>` to test secured endpoints.

## Contributing

Before submitting contributions, verify that all quality checks pass:

1. **Compile the project:**
   ```bash
   ./mvnw clean compile
   ```
2. **Execute tests:**
   ```bash
   ./mvnw clean test
   ```
3. **Verify Kotlin code formatting:**
   ```bash
   ./mvnw ktlint:check
   ```

## Author and License

- **Author**: Thomas BATISTA
- **Institution**: IUT of Arles - BUT Informatique
- **License**: Developed for academic and educational purposes.
