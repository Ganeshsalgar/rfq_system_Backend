# RFQ System — Backend

Production-ready Spring Boot backend for an RFQ (Request for Quotation) system with a British Auction engine.

## Tech Stack
- Java 17
- Spring Boot 3.2
- Spring Security + JWT
- Spring Data JPA
- MySQL 8+
- WebSocket (STOMP)
- Lombok
- Maven

## Setup

### 1. Configure Database
Update `src/main/resources/application.properties`:
```properties
spring.datasource.username=your_mysql_user
spring.datasource.password=your_mysql_password
```

### 2. Build & Run
```bash
mvn clean install -DskipTests
java -jar target/rfq-system-1.0.0.jar
```
App starts on `http://localhost:8080`

### Default Admin Account
On first startup, an admin account is auto-created:
```
Username: admin
Password: admin123
```

## API Endpoints

| Method | Endpoint | Role | Description |
|--------|----------|------|-------------|
| POST | `/auth/login` | Public | Login |
| POST | `/users` | Public | Register |
| GET | `/users` | ADMIN | Get all users |
| GET | `/users/{id}` | Authenticated | Get user |
| POST | `/rfq` | BUYER, ADMIN | Create RFQ |
| GET | `/rfq` | All | List RFQs |
| GET | `/rfq/{id}` | All | RFQ details |
| GET | `/rfq/{id}/bids` | All | Get bids |
| GET | `/rfq/{id}/rankings` | All | Get rankings |
| GET | `/rfq/{id}/logs` | All | Activity logs |
| POST | `/auction/bid` | SUPPLIER, ADMIN | Place bid |

## Roles
| Role | Permissions |
|------|-------------|
| BUYER | Create RFQs, view bids/rankings/logs |
| SUPPLIER | Place bids, view RFQs/rankings/logs |
| ADMIN | Full access + manage users |

## Frontend Repository
👉 [rfq-system-frontend](https://github.com/your-username/rfq-system-frontend)
