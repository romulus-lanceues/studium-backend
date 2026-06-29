# Studium API

Studium API is a Spring Boot backend for a Pomodoro-style study productivity app. It handles authentication, subject management, study session tracking, and analytics for understanding how a user studies over time.

The project started as the backend for my own study workflow, so the API is built around practical app behavior rather than demo-only CRUD. It supports secure cookie-based JWT authentication, refresh-token rotation, Pomodoro session state changes, subject goals, dashboard stats, productivity scoring, and personalized focus recommendations.

## Why This Project Exist?

This project exists as a product of me wanting to keep my study sessions tracked and learn from them through analytics. Am I spending my study sessions productively? Or maybe I'm being interrupted left and right. I know there's a lot of POMODORO systems out there, but as someone who's very passionate when it comes to software development, I feel like it'll be better if I create one. :)

This API powers those workflows through a relational data model, session lifecycle rules, and analytics queries built around real study behavior.

## Highlights

- Secure registration and login with Spring Security
- JWT access tokens stored in HTTP-only cookies
- Refresh-token rotation with database-backed revocation
- Argon2 password hashing with BCrypt support
- Role-based access control for user and admin behavior
- Subject creation, updates, weekly goals, and progress tracking
- Pomodoro session lifecycle: start, pause, resume, complete, cancel
- Interruption tracking for focus quality
- Dashboard data for streaks, daily sessions, and last activity
- Analytics for summary stats, peak study hours, weekly goals, breakdowns, and productivity score
- Focus recommendations based on historical duration, completion rate, and interruptions
- PostgreSQL-backed persistence with Spring Data JPA
- Dockerfile for containerized deployment

## Tech Stack

- Java 21
- Spring Boot 3.5.14
- Spring Web
- Spring Security
- Spring Data JPA
- PostgreSQL
- JJWT
- Argon2 / BCrypt password encoding
- Bean Validation
- Lombok
- Maven
- Docker

## Architecture

The API follows a layered Spring Boot structure:

```text
Controllers  -> HTTP endpoints and request/response boundaries
Services     -> business rules, authorization checks, analytics logic
Repositories -> database access through Spring Data JPA
Entities     -> persisted domain model
DTOs         -> API request and response contracts
Security     -> JWT filter, user details, auth configuration
```

Important domain areas:

- `AuthService` manages registration, login, logout, and refresh-token rotation.
- `SessionService` owns Pomodoro session behavior and state transitions.
- `SubjectService` manages subjects and weekly study goals.
- `DataService` builds dashboard and analytics responses.
- `FocusRecommendationService` calculates personalized focus-session suggestions.

## Core Features

### Authentication

- Register and log in with email/password
- Store access and refresh tokens in HTTP-only cookies
- Rotate refresh tokens when generating a new access token
- Revoke refresh tokens on logout
- Hash passwords using Argon2 by default

### Study Sessions

- Start a work, short-break, or long-break session
- Pause and resume active sessions
- Record interruptions
- Complete or cancel sessions
- Track planned duration versus actual duration
- Save notes for session context

### Subjects

- Create and manage study subjects
- Assign colors and descriptions
- Set weekly session goals
- Track completed Pomodoros and total study time
- Maintain subject-level streaks

### Analytics

- Dashboard summary
- Session history
- Weekly completion overview
- Monthly completed-session totals
- Completed sessions per subject
- Summary stats
- Peak study hours
- Productivity score and trend
- Daily, weekly, and monthly breakdowns
- Weekly goal progress

### Focus Recommendation

The recommendation system looks at completed and cancelled work sessions over a recent analysis window. It groups sessions by planned duration, scores each duration using completion rate and interruption count, and returns a suggested focus length with a confidence value and human-readable insight.

## API Overview

### Auth

```text
POST /api/v1/auth/register
POST /api/v1/auth/login
POST /api/v1/auth/refresh-token
POST /api/v1/auth/logout
GET  /api/v1/auth/user/{userId}
```

### Subjects

```text
POST   /api/v1/subject/add
GET    /api/v1/subject/subjects
GET    /api/v1/subject/{subjectId}
PATCH  /api/v1/subject/update/{subjectId}
DELETE /api/v1/subject/delete/{subjectId}
```

### Sessions

```text
POST  /api/v1/sessions/{subjectId}/start
GET   /api/v1/sessions/{id}
PATCH /api/v1/sessions/{sessionId}/pause
PATCH /api/v1/sessions/{sessionId}/resume
PATCH /api/v1/sessions/{sessionId}/interruptions
PATCH /api/v1/sessions/{sessionId}/completed
PATCH /api/v1/sessions/{sessionId}/cancel
GET   /api/v1/sessions/sessions/today
GET   /api/v1/sessions/{subjectId}/history
```

### Data and Analytics

```text
GET /api/v1/data/dashboard
GET /api/v1/data/session-history
GET /api/v1/data/subjects-data
GET /api/v1/data/week/overview
GET /api/v1/data/analytics/summary
GET /api/v1/data/analytics/peak-hours
GET /api/v1/data/analytics/productivity-score
GET /api/v1/data/analytics/recommendation
GET /api/v1/data/analytics/breakdown
GET /api/v1/data/analytics/goals
```

## Configuration

The application expects these environment variables:

```bash
DB_URL=jdbc:postgresql://localhost:5432/studium_db
DB_USER=your_db_user
DB_PASS=your_db_password

TOKEN_SIGNATURE=your-base64-encoded-secret
TOKEN_EXPIRATION=900000
REFRESH_TOKEN_EXPIRATION=604800000
```

Cookie behavior is configured in `src/main/resources/application.properties`.

## Running Locally

### Prerequisites

- Java 21+
- Maven 3.6+
- PostgreSQL 12+

### Start the app

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

The API runs on:

```text
http://localhost:8080
```

## Running With Docker

```bash
docker build -t studium-api .
docker run -p 8080:8080 --env-file .env studium-api
```

## Testing

```bash
./mvnw test
```

Test coverage is an active improvement area. The next step is to add focused service and integration tests for authentication, session ownership, session lifecycle behavior, analytics calculations, and recommendation scoring.

## Current Improvement Roadmap

- Add unit tests for core services
- Add integration tests with PostgreSQL/Test containers
- Add OpenAPI/Swagger documentationf
- Add Flyway or Liquibase database migrations
- Tighten ownership checks on all ID-based read/delete endpoints
- Improve request validation for session and subject DTOs
- Replace debug `System.out.println` calls with structured logging
- Add CI for tests and Docker builds
- Add rate limiting for authentication endpoints
- Add email verification and password reset

## What This Project Demonstrates

- Building a non-trivial REST API with Spring Boot
- Implementing cookie-based JWT authentication
- Designing refresh-token rotation and revocation
- Modeling a real productivity domain with JPA relationships
- Writing service-layer business rules for stateful workflows
- Creating analytics endpoints from relational data
- Turning user behavior into personalized recommendations
- Structuring a backend project for future production hardening

## Author

Built by Lance Buela as a personal productivity API and backend portfolio project.
