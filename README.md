# Resource Booking System - Backend API

A robust RESTful API backend for a Resource Booking System built with Spring Boot 3, Spring Security, JWT, and MySQL.

---

## 🚀 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.3.2
- **Security:** Spring Security & JWT (JSON Web Tokens)
- **Database / ORM:** Spring Data JPA (Hibernate), MySQL Driver
- **Documentation:** Springdoc OpenAPI 2.6.0 (Swagger UI)
- **Build Tool:** Maven

---

## 🔑 Key Features

1. **Authentication & Authorization:**
   - JWT-based stateless authentication.
   - Role-Based Access Control (`ROLE_ADMIN`, `ROLE_USER`).
2. **Resource Management:**
   - Manage bookable resources (e.g., Meeting Rooms, Vehicles, Equipment).
   - Filter and view availability status.
3. **Reservation Lifecycle:**
   - Create, retrieve, and cancel reservations.
   - Status tracking (`PENDING`, `CONFIRMED`, `CANCELLED`).
4. **API Documentation:**
   - Interactive Swagger UI for testing all endpoints.
5. **Data Seeder:**
   - Pre-loads sample users and resources on initial startup.

---

## 👥 Default Seed Accounts

On application startup, default accounts are seeded into the database:

| Role | Email | Password |
|---|---|---|
| **Admin** | `admin@resourcebooking.local` | `admin123` |
| **User** | `user@resourcebooking.local` | `user123` |

---

## 📡 API Endpoints Overview

### Authentication
- `POST /auth/login` - Authenticate and obtain JWT token

### Resources
- `GET /resources` - List all resources (authenticated)
- `GET /resources/{id}` - Get resource details (authenticated)
- `POST /resources` - Create a resource (`ADMIN` only)
- `PUT /resources/{id}` - Update a resource (`ADMIN` only)
- `DELETE /resources/{id}` - Delete a resource (`ADMIN` only)

### Reservations
- `GET /reservations` - List reservations
- `POST /reservations` - Create a reservation
- `GET /reservations/{id}` - Get reservation by ID
- `PUT /reservations/{id}/cancel` - Cancel a reservation

---

## 🛠️ Configuration & Setup

### 1. Database Configuration
Ensure MySQL is running and configure credentials in `src/main/resources/application.properties`:
```properties
server.port=8081
spring.datasource.url=jdbc:mysql://localhost:3306/resource_booking?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=root
```

### 2. Build and Run
```bash
# Build the project
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

### 3. Access Swagger UI
Once started, open your browser and navigate to:
```
http://localhost:8081/swagger-ui.html
```
