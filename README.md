# Studium API

A production-ready RESTful API for a Pomodoro-style study session management application. Built with Spring Boot 3.5.7 and Java 21, featuring secure JWT authentication, real-time session tracking, and comprehensive study analytics.

## Overview

Studium API is a backend service that powers a study productivity application, enabling users to manage study sessions, track subjects, and monitor their learning progress using the Pomodoro Technique. The API implements industry-standard security practices, scalable architecture, and efficient data management.

## Key Features

### Authentication & Security
- **JWT-based Authentication** with access and refresh token rotation
- **Argon2 Password Hashing** - Industry-leading password security (with BCrypt fallback)
- **HTTP-only Secure Cookies** for token storage
- **Role-based Access Control** (RBAC) with USER and ADMIN roles
- **Stateless Session Management** for scalability
- **Custom Exception Handling** with proper HTTP status codes
- **CORS Configuration** for secure cross-origin requests

### Study Session Management
- **Pomodoro Timer Sessions** - Create, pause, resume, and complete study sessions
- **Session Types** - Work sessions, short breaks, and long breaks
- **Interruption Tracking** - Monitor and log session interruptions
- **Session States** - IN_PROGRESS, PAUSED, COMPLETED, CANCELLED
- **Real-time Session Data** - Track planned vs actual duration
- **Session Notes** - Add notes and reflections to completed sessions

### Subject Management
- **Subject Organization** - Create and manage study subjects
- **Weekly Goals** - Set and track weekly study time targets
- **Progress Tracking** - Monitor total study time and completed Pomodoros
- **Subject Analytics** - View study statistics per subject

### Performance & Scalability
- **Redis Integration** - Caching layer for improved performance
- **Lazy Loading** - Optimized database queries with JPA lazy fetching
- **Connection Pooling** - Efficient database connection management
- **Stateless Architecture** - Horizontally scalable design

## Technology Stack

### Core Framework
- **Spring Boot 3.5.7** - Modern Java application framework
- **Java 21** - Latest LTS version with modern language features
- **Maven** - Dependency management and build automation

### Security
- **Spring Security** - Comprehensive security framework
- **JJWT 0.12.6** - JSON Web Token implementation
- **Argon2** - Memory-hard password hashing algorithm
- **Bouncy Castle** - Cryptographic provider

### Database & Caching
- **PostgreSQL** - Robust relational database
- **Spring Data JPA** - Data persistence layer
- **Hibernate** - ORM framework
- **Redis** - In-memory data structure store for caching

### Additional Libraries
- **Lombok** - Reduces boilerplate code
- **Bean Validation** - Input validation and constraints
- **Jackson** - JSON processing

##  Architecture

The application follows a **layered architecture** pattern with clear separation of concerns:

```
┌─────────────────────────────────────┐
│         Controllers Layer           │  ← REST API endpoints
├─────────────────────────────────────┤
│         Services Layer              │  ← Business logic
├─────────────────────────────────────┤
│         Repository Layer            │  ← Data access
├─────────────────────────────────────┤
│         Entity Layer                │  ← Domain models
└─────────────────────────────────────┘
```

### Key Components

- **Controllers** - Handle HTTP requests and responses
- **Services** - Implement business logic and orchestration
- **Repositories** - Data access abstraction
- **Entities** - JPA domain models with relationships
- **DTOs** - Data Transfer Objects for request/response
- **Security Filters** - JWT authentication and authorization
- **Exception Handlers** - Centralized error handling
- **Configuration** - Security, CORS, Redis, and cookie settings

## API Endpoints

### Authentication (`/api/v1/auth`)
- `POST /register` - Create new user account
- `POST /login` - Authenticate and receive tokens
- `POST /refresh-token` - Refresh access token
- `POST /logout` - Invalidate tokens and logout
- `GET /user/{userId}` - Get user information

### Study Sessions (`/api/v1/sessions`)
- `POST /start` - Start a new study session
- `GET /{id}` - Get session details
- `PATCH /{sessionId}/pause` - Pause active session
- `PATCH /{sessionId}/resume` - Resume paused session
- `PATCH /{sessionId}/interruptions` - Log an interruption
- `PATCH /{sessionId}/completed` - Mark session as completed
- `PATCH /{sessionId}/cancel` - Cancel active session

### Subjects (`/api/v1/subject`)
- `POST /add` - Create a new subject
- `GET /subjects` - Get all user's subjects
- `GET /{subjectId}` - Get subject details

### Admin (`/api/admin-qwerty`)
- `POST /revoke/token` - Revoke refresh token (Admin only)

## Security Features

### Token Management
- **Access Tokens** - Short-lived (15 minutes) for API access
- **Refresh Tokens** - Long-lived (7 days) stored in database
- **Token Rotation** - Refresh tokens are rotated on each use
- **Token Revocation** - Support for token invalidation

### Password Security
- **Argon2 Hashing** - Memory-hard algorithm resistant to GPU attacks
- **Delegating Password Encoder** - Support for multiple algorithms
- **Secure Storage** - Passwords never stored in plain text

### Request Security
- **JWT Filter Chain** - Validates tokens on every request
- **Authentication Principal** - Spring Security integration
- **Method-level Security** - `@PreAuthorize` for role-based access

## Getting Started

### Prerequisites
- Java 21 or higher
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+

### Environment Variables

Create a `.env` file or set the following environment variables:

```bash
# Database Configuration
DB_URL=jdbc:postgresql://localhost:5432/studium_db
DB_USER=your_db_user
DB_PASS=your_db_password

# JWT Configuration
TOKEN_SIGNATURE=your-secret-key-minimum-256-bits
```

### Installation

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd studium-api
   ```

2. **Configure database**
   - Create a PostgreSQL database
   - Update `application.properties` or set environment variables

3. **Start Redis**
   ```bash
   redis-server
   ```

4. **Build the project**
   ```bash
   mvn clean install
   ```

5. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

The API will be available at `http://localhost:8080`

## 📁 Project Structure

```
src/main/java/com/lancea/studium/studium_api/
├── config/              # Configuration classes
│   ├── CookieUtil.java
│   ├── GlobalCorsConfig.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── controller/          # REST controllers
│   ├── AdminController.java
│   ├── AuthController.java
│   ├── SessionController.java
│   └── SubjectController.java
├── dto/                 # Data Transfer Objects
│   ├── request/         # Request DTOs
│   └── response/        # Response DTOs
├── entity/              # JPA entities
│   ├── RefreshToken.java
│   ├── Role.java
│   ├── SessionStatus.java
│   ├── SessionType.java
│   ├── StudySession.java
│   ├── Subject.java
│   └── User.java
├── exception/           # Custom exceptions
│   ├── GlobalExceptionHandler.java
│   └── ...
├── repository/          # Data access layer
│   ├── RefreshTokenRepository.java
│   ├── StudySessionRepository.java
│   ├── SubjectRepository.java
│   └── UserRepository.java
├── security/            # Security components
│   ├── JwtAuthenticationFilter.java
│   ├── MyExceptionTranslationFilter.java
│   └── MyUserDetails.java
└── service/             # Business logic
    ├── AdminService.java
    ├── AuthService.java
    ├── JwtService.java
    ├── SessionService.java
    └── SubjectService.java
```

## Design Patterns & Best Practices

- **Repository Pattern** - Abstraction for data access
- **Service Layer Pattern** - Separation of business logic
- **DTO Pattern** - Data transfer objects for API contracts
- **Builder Pattern** - Lombok builders for entity creation
- **Filter Pattern** - JWT authentication filter
- **Exception Handling** - Global exception handler with proper HTTP status codes
- **Dependency Injection** - Constructor-based DI throughout
- **RESTful Design** - Proper HTTP methods and status codes
- **Stateless Architecture** - No server-side sessions

## Database Schema

### Core Entities
- **Users** - User accounts with email, password, and role
- **Subjects** - Study subjects with goals and progress tracking
- **StudySessions** - Individual Pomodoro sessions with state management
- **RefreshTokens** - Token storage for refresh token rotation

### Relationships
- User → Subjects (One-to-Many)
- User → StudySessions (One-to-Many)
- Subject → StudySessions (One-to-Many)

## Testing

Run tests with:
```bash
mvn test
```

## API Documentation

The API follows RESTful conventions:
- JSON request/response format
- Proper HTTP status codes (200, 201, 400, 401, 403, 404, 409, 500)
- Consistent error response format
- Resource-based URLs

## Future Enhancements

- [ ] OpenAPI/Swagger documentation
- [ ] Unit and integration tests
- [ ] WebSocket support for real-time session updates
- [ ] Email verification
- [ ] Password reset functionality
- [ ] Study statistics and analytics endpoints
- [ ] Export study data (CSV/JSON)
- [ ] Docker containerization
- [ ] CI/CD pipeline
- [ ] Rate limiting
- [ ] API versioning strategy

## License

This project is part of a personal portfolio.

## Developer

**Lance Buela**

Built with passion using Spring Boot and modern Java best practices.

---

This API demonstrates:
- ✅ **Production-ready security** with JWT, refresh tokens, and Argon2 hashing
- ✅ **Scalable architecture** with stateless design and Redis caching
- ✅ **Clean code** following SOLID principles and design patterns
- ✅ **Modern Java** using Java 21 features and Spring Boot 3.5.7
- ✅ **Database design** with proper relationships and JPA optimization
- ✅ **Error handling** with comprehensive exception management
- ✅ **RESTful API design** following industry standards
- ✅ **Real-world application** solving an actual problem (study productivity)



