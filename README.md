# Bid Backend

A Spring Boot microservice providing authentication and user management for the Bid platform, backed by MongoDB and secured with JWT.

---

## Tech Stack

| Layer        | Technology                          |
|--------------|-------------------------------------|
| Language     | Java 8                              |
| Framework    | Spring Boot 2.7.18                  |
| Security     | Spring Security + JWT (jjwt 0.11.5) |
| Database     | MongoDB (embedded for dev/test)     |
| Validation   | Spring Boot Validation              |
| API Docs     | SpringDoc OpenAPI (Swagger UI)      |
| Boilerplate  | Lombok                              |
| Build Tool   | Maven                               |

---

## Project Structure

```
src/
└── main/
    ├── java/com/bidbackend/
    │   ├── BidBackendApplication.java      # Entry point
    │   ├── config/
    │   │   ├── SecurityConfig.java         # Spring Security configuration
    │   │   └── SwaggerConfig.java          # OpenAPI / Swagger configuration
    │   ├── controller/
    │   │   └── AuthController.java         # /api/auth endpoints
    │   ├── dto/
    │   │   ├── AuthRequest.java            # Login / register request body
    │   │   └── AuthResponse.java           # JWT + user info response
    │   ├── model/
    │   │   └── User.java                   # MongoDB User document
    │   ├── repository/
    │   │   └── UserRepository.java         # MongoDB user queries
    │   ├── security/
    │   │   ├── JwtAuthFilter.java          # JWT request filter
    │   │   ├── JwtUtil.java                # Token generation & validation
    │   │   └── UserDetailsServiceImpl.java # UserDetailsService impl
    │   └── service/
    │       └── AuthService.java            # Register & login business logic
    └── resources/
        └── application.yml                 # App configuration
```

---

## Getting Started

### Prerequisites

- Java 8+
- Maven 3.6+
- MongoDB (optional — embedded MongoDB is used by default)

### Run Locally

```bash
# Clone the repository
git clone <repository-url>
cd bid-backend

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The server starts at **http://localhost:8080**.

---

## Configuration

Key settings in `src/main/resources/application.yml`:

```yaml
server:
  port: 8080

spring:
  data:
    mongodb:
      host: localhost
      port: 27017
      database: bidbackend

jwt:
  secret: <your-secret-key>
  expiration: 86400000   # 1 day in milliseconds
```

> **Note:** The embedded MongoDB (`de.flapdoodle.embed.mongo`) is included as a dependency, so no local MongoDB installation is required for development.

---

## API Endpoints

Base path: `/api/auth`

| Method | Endpoint             | Description                        | Auth Required |
|--------|----------------------|------------------------------------|---------------|
| POST   | `/api/auth/register` | Register a new user, returns JWT   | No            |
| POST   | `/api/auth/login`    | Authenticate a user, returns JWT   | No            |

### Request Body (both endpoints)

```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

### Response Body

```json
{
  "token": "<jwt-token>",
  "email": "user@example.com",
  "role": "ROLE_USER"
}
```

---

## Swagger UI

Interactive API documentation is available at:

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON spec:

```
http://localhost:8080/v3/api-docs
```

---

## Running Tests

```bash
mvn test
```

---

## Build

```bash
# Package as a JAR
mvn clean package

# Run the JAR directly
java -jar target/bid-backend-0.0.1-SNAPSHOT.jar
```
