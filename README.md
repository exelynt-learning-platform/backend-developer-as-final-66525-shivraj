# Resource Booking System - Backend API

> **Assignment:** Backend Developer Assignment Deadline (30th Sep 2026)  
> **Built With:** Java 17+, Spring Boot 3.3.2, Spring Security 6, JWT, Spring Data JPA, MySQL / PostgreSQL, Swagger UI / OpenAPI 3

A production-ready RESTful Resource Booking System backend providing secure authentication, role-based access control (RBAC), resource catalog management, conflict-free reservation scheduling with time validations, dynamic database-level filtering, pagination, and sorting.

---

## 🎯 Evaluation Criteria Fulfillment Matrix

| # | Evaluation Criteria | Implementation Details |
|---|---|---|
| **1** | **Authentication** | JWT login via `POST /auth/login`, stateless filter `JwtAuthenticationFilter`, BCrypt password hashing via `BCryptPasswordEncoder`. |
| **2** | **Authorization & RBAC** | `ADMIN` has full access to all resources and reservations. `USER` has read-only access to resources and can manage only their own reservations. |
| **3** | **Security** | Protected endpoints via `SecurityFilterChain`, method-level `@PreAuthorize`, prevention of unauthorized data access. |
| **4** | **CRUD Operations** | Full CRUD for resources (`GET`, `POST`, `PUT`, `DELETE /resources`) and reservations (`GET`, `POST`, `PUT`, `DELETE /reservations`). |
| **5** | **Reservation Ownership** | `USER` identity is strictly resolved from the authenticated JWT token; users can view, update, and cancel only their own bookings. Admins can manage all bookings. |
| **6** | **Validation** | Jakarta validation constraints (`@NotNull`, `@Positive`), valid reservation statuses, and time boundary checks (`startTime < endTime`, no overlapping reservations on active resources). |
| **7** | **Filtering** | Database-level dynamic filtering via Spring Data JPA `Specification` by `status`, `minPrice`, and `maxPrice`. |
| **8** | **Pagination & Sorting** | Handled natively in SQL via `Pageable` (`page`, `size`, `sortBy`, `direction`). |
| **9** | **Database** | JPA/Hibernate entity relationships (`@ManyToOne` between `Reservation`, `Resource`, and `User`). Configured for MySQL / PostgreSQL with in-memory H2 for tests. |
| **10** | **API Design** | Clean REST standards, HTTP status codes (`200`, `201`, `204`, `400`, `401`, `403`, `404`, `409`), and structured DTOs (`ReservationResponse`, `ResourceRequest`). |
| **11** | **Error Handling** | Global exception handler `ApiExceptionHandler` returning standard structured error payloads. |
| **12** | **Code Quality** | Clean layered architecture separating Controller, Service, Repository, DTO, Entity, Config, and Security layers. |
| **13** | **Testing** | Comprehensive unit & integration test suite (`27/27` tests passing) testing auth, security, RBAC, ownership, validation, and filtering. |

---

## 🚀 Tech Stack

- **Java:** 17+
- **Framework:** Spring Boot 3.3.2 (Spring MVC, Spring Security, Spring Data JPA)
- **Security:** JWT (`jjwt 0.11.5`) with BCrypt password encoding
- **Database:** MySQL / PostgreSQL (Production & Dev) and H2 (Automated Tests)
- **Documentation:** Springdoc OpenAPI 2.6.0 (Swagger UI)
- **Build Tool:** Maven 3.9+

---

## 👥 Seed Test Accounts

Upon application startup, default accounts and resources are automatically seeded via `DataSeeder`:

| Role | Email | Password | Permissions |
|---|---|---|---|
| **ADMIN** | `admin@resourcebooking.local` | `admin123` | Full CRUD on resources & all reservations |
| **USER** | `user@resourcebooking.local` | `user123` | Read-only resources; CRUD on own reservations |

---

## ⚙️ Configuration & Environment Variables

The application can be configured via `src/main/resources/application.properties` or environment variables:

| Environment Variable | Default Value | Description |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/resource_booking?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` | Database JDBC URL |
| `DB_USERNAME` | `root` | Database username |
| `DB_PASSWORD` | `root` | Database password |
| `DB_DRIVER` | `com.mysql.cj.jdbc.Driver` | JDBC driver class name |
| `JWT_SECRET` | `change-this-to-a-strong-secret-key` | HMAC SHA secret key for JWT signing |
| `JWT_EXPIRATION_MS` | `3600000` (1 hour) | Token validity in milliseconds |
| `SERVER_PORT` | `8081` | HTTP server port |

---

## 🛠️ Build and Run

### 1. Run Automated Test Suite
```bash
mvn clean test
```
*All 27 integration and security tests run against an in-memory H2 database with zero external dependencies.*

### 2. Start Application Locally
```bash
mvn spring-boot:run
```

### 3. Open Interactive Swagger UI
Open your browser and navigate to:
```
http://localhost:8081/swagger-ui.html
```
Click **Authorize** and input: `Bearer <your_jwt_token>` to test protected endpoints.

---

## 📡 API Reference

### 1. Authentication
- `POST /auth/login` - Authenticate with email & password, returns JWT token.

```json
// Request
{
  "email": "user@resourcebooking.local",
  "password": "user123"
}

// Response (200 OK)
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "email": "user@resourcebooking.local",
  "role": "USER"
}
```

---

### 2. Resources (`/resources`)

| Method | Endpoint | Allowed Roles | Description |
|---|---|---|---|
| `GET` | `/resources` | `ADMIN`, `USER` | Paginated list of resources (`page`, `size`, `sortBy`, `direction`) |
| `GET` | `/resources/{id}` | `ADMIN`, `USER` | Get resource details by ID |
| `POST` | `/resources` | `ADMIN` only | Create a new resource |
| `PUT` | `/resources/{id}` | `ADMIN` only | Update resource details |
| `DELETE`| `/resources/{id}` | `ADMIN` only | Remove a resource |

```json
// POST /resources (ADMIN only)
{
  "name": "Main Conference Room",
  "type": "ROOM",
  "location": "3rd Floor, Tower A",
  "pricePerHour": 50.00,
  "available": true
}
```

---

### 3. Reservations (`/reservations`)

| Method | Endpoint | Allowed Roles | Description |
|---|---|---|---|
| `GET` | `/reservations` | `ADMIN`, `USER` | Filtered & paginated reservations (`status`, `minPrice`, `maxPrice`, `page`, `size`, `sortBy`, `direction`) |
| `GET` | `/reservations/{id}` | `ADMIN`, `USER` (own) | Get reservation by ID (Users can view only their own; Admin can view all) |
| `POST` | `/reservations` | `ADMIN`, `USER` | Create a reservation (User identity automatically inferred from JWT) |
| `PUT` | `/reservations/{id}` | `ADMIN`, `USER` (own) | Update reservation details |
| `PUT` | `/reservations/{id}/cancel` | `ADMIN`, `USER` (own) | Cancel a reservation (sets status to `CANCELLED`) |
| `DELETE`| `/reservations/{id}` | `ADMIN`, `USER` (own) | Delete a reservation |

#### Creating a Reservation
```json
// POST /reservations
{
  "resourceId": 1,
  "startTime": "2026-10-05T10:00:00",
  "endTime": "2026-10-05T12:00:00",
  "price": 80.00,
  "status": "PENDING"
}
```

#### Filtering & Pagination Parameters (`GET /reservations`):
- `status`: `PENDING`, `CONFIRMED`, `CANCELLED`
- `minPrice`: Minimum reservation price (decimal)
- `maxPrice`: Maximum reservation price (decimal)
- `page`: Page index (default: `0`)
- `size`: Page size (default: `10`)
- `sortBy`: Field to sort by (`createdAt`, `price`, `startTime`, etc.)
- `direction`: `asc` or `desc` (default: `desc`)

*Example:*  
`GET /reservations?status=CONFIRMED&minPrice=50.00&maxPrice=200.00&page=0&size=10&sortBy=price&direction=asc`
