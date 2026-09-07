# Architecture Document: Task Tracking System

> Based on [ProblemStatement.md](ProblemStatement.md)

---

## 1. Overview

The Task Tracking System is built as a **layered monolith** using Spring Boot, exposing a RESTful API consumed by a separate frontend client. The architecture prioritizes simplicity, security, and clear separation of concerns, with well-defined module boundaries that allow future extraction into services if scaling demands it.

---

## 2. High-Level Architecture

```
┌─────────────┐
│   Client     │  (Frontend / API consumers)
└──────┬──────┘
       │ HTTPS (JSON)
┌──────▼──────────────────────────────────────────┐
│              Spring Boot Application             │
│                                                  │
│  ┌────────────────────────────────────────────┐ │
│  │         Security Filter Chain               │ │
│  │   (JWT Auth Filter, CORS, Authorization)    │ │
│  └────────────────────────────────────────────┘ │
│                                                  │
│  ┌─────────┐  ┌─────────┐  ┌──────────────┐     │
│  │  User   │  │  Task   │  │ Team/Project │     │
│  │ Module  │  │ Module  │  │   Module     │     │
│  │         │  │         │  │  + Comments  │     │
│  └────┬────┘  └────┬────┘  └──────┬───────┘     │
│       │            │              │              │
│  ┌────▼────────────▼──────────────▼───────┐     │
│  │        Persistence Layer (JPA)          │     │
│  └────────────────────┬───────────────────┘     │
└────────────────────────┼────────────────────────┘
                         │
                ┌────────▼────────┐
                │   PostgreSQL     │
                │   (Relational)   │
                └─────────────────┘
```

### Architectural Style
- **Monolithic, modular** — single deployable unit, organized into feature modules
- **Layered per module**: Controller → Service → Repository
- **Stateless authentication** — JWT enables horizontal scale-out later

---

## 3. Tech Stack

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Java 17+ | LTS, records, sealed classes |
| Framework | Spring Boot 3.x | Industry standard, rich ecosystem |
| Build Tool | Maven | Widely used, predictable dependency management |
| Database | PostgreSQL | ACID compliance, relational fit for task/team data |
| ORM | Spring Data JPA (Hibernate) | Reduces boilerplate, schema migrations via Flyway |
| Security | Spring Security + JWT (jjwt) | Stateless auth, BCrypt password hashing |
| Validation | Jakarta Bean Validation | Declarative input validation |
| API Docs | springdoc-openapi (Swagger UI) | Auto-generated interactive docs |
| Mapping | MapStruct | Type-safe entity ↔ DTO mapping |
| Testing | JUnit 5, Mockito, Testcontainers | Unit + integration tests against real DB |

---

## 4. Module Structure

```
com.example.tasktracker
├── common/                  # Shared utilities, exceptions, responses
│   ├── config/              # SecurityConfig, OpenApiConfig, JpaConfig
│   ├── exception/           # GlobalExceptionHandler, custom exceptions
│   └── dto/                 # ApiResponse<T>, PageResponse<T>
│
├── security/
│   ├── JwtService           # Token generation/validation
│   ├── JwtAuthFilter        # OncePerRequestFilter
│   ├── UserDetailsServiceImpl
│   └── AuthController       # /auth/register, /auth/login
│
├── user/
│   ├── UserController       # /users/me, profile CRUD
│   ├── UserService
│   ├── UserRepository
│   ├── User                 # Entity
│   └── dto/                 # UserRequest, UserResponse
│
├── task/
│   ├── TaskController       # /tasks CRUD + filter/search/sort
│   ├── TaskService
│   ├── TaskRepository       # Specification-based queries
│   ├── Task                 # Entity
│   ├── CommentController    # /tasks/{id}/comments
│   ├── AttachmentController # /tasks/{id}/attachments
│   └── dto/
│
└── team/
    ├── TeamController       # /teams CRUD, membership
    ├── TeamService
    ├── ProjectController    # /projects CRUD
    ├── TeamMembership       # Entity (user ↔ team, role)
    └── dto/
```

Each module follows the same internal layering; modules communicate only through **service interfaces**, never repositories of other modules.

---

## 5. Request Flow

```
Client Request
   │
   ▼
[JwtAuthFilter] ──► validates token, sets SecurityContext (skip for /auth/**)
   │
   ▼
[Controller] ──► @Valid DTO binding, HTTP method mapping
   │
   ▼
[Service] ──► business rules, authorization checks (@PreAuthorize),
   │          ownership/membership validation, transactions (@Transactional)
   ▼
[Repository] ──► JPA queries / Specifications
   │
   ▼
[Entity] ──MapStruct──► [DTO] ──► JSON response
```

**Error handling:** exceptions bubble up to `@RestControllerAdvice`, which maps them to consistent `ErrorResponse` payloads with proper status codes (400 validation, 401 auth, 403 forbidden, 404 not found, 409 conflict).

---

## 6. Data Model

```
┌──────────┐      ┌──────────────────┐      ┌──────────┐
│  users   │      │ team_memberships │      │  teams   │
├──────────┤      ├──────────────────┤      ├──────────┤
│ id PK    │◄────►│ id PK            │◄────►│ id PK    │
│ email UQ │      │ user_id FK       │      │ name     │
│ password │      │ team_id FK       │      │ owner_id │
│ name     │      │ role (enum)      │      └────┬─────┘
│ avatar   │      │ joined_at        │           │
└────┬─────┘      └──────────────────┘           │
     │                                           │
     │ 1                                         │ 1
     │                                           │
     │ *                                         │ *
┌────▼──────────────┐   ┌───────────────┐  ┌────▼─────┐
│      tasks        │   │   comments    │  │ projects │
├───────────────────┤   ├───────────────┤  ├──────────┤
│ id PK             │◄──┤ id PK         │  │ id PK    │
│ title             │ 1 │ task_id FK    │  │ name     │
│ description TEXT  │ * │ author_id FK  │  │ team_id  │
│ status (enum)     │   │ content       │  └──────────┘
│ priority (enum)   │   │ created_at    │
│ due_date          │   └───────────────┘
│ assignee_id FK ───┼──► users.id
│ reporter_id FK ───┼──► users.id
│ project_id FK ────┼──► projects.id
│ created_at        │   ┌───────────────┐
│ updated_at        │◄──┤ attachments   │
└───────────────────┘ * │ id PK         │
                        │ task_id FK    │
                        │ file_name     │
                        │ storage_key   │
                        │ content_type  │
                        │ size_bytes    │
                        │ uploaded_by   │
                        └───────────────┘
```

### Key Enums
- `TaskStatus`: OPEN, IN_PROGRESS, COMPLETED
- `TaskPriority`: LOW, MEDIUM, HIGH
- `TeamRole`: OWNER, ADMIN, MEMBER

### Indexing Strategy
- `tasks(assignee_id, status)` — "my tasks" filtered views
- `tasks(project_id, due_date)` — project timelines
- `comments(task_id)`, `attachments(task_id)`
- Full-text or trigram index on `tasks(title, description)` for search

---

## 7. API Surface

| Area | Endpoints |
|------|-----------|
| Auth | `POST /api/auth/register`, `POST /api/auth/login` |
| Profile | `GET/PATCH /api/users/me` |
| Tasks | `GET/POST /api/tasks`, `GET/PATCH/DELETE /api/tasks/{id}` |
| Task Query | `?status=&assignee=&project=&q=&sort=dueDate,desc&page=` |
| Comments | `GET/POST /api/tasks/{taskId}/comments` |
| Attachments | `POST/GET/DELETE /api/tasks/{taskId}/attachments` |
| Teams | `POST /api/teams`, `POST /api/teams/{id}/members`, `DELETE /api/teams/{id}/members/{userId}` |
| Projects | `CRUD /api/projects`, nested under teams |

All list endpoints return paginated results (`page`, `size`, `sort` parameters).

---

## 8. Security Architecture

- **Password storage:** BCrypt (strength 12)
- **Authentication:** JWT access token (~15 min expiry) issued at login; stateless validation on every request
- **Authorization:**
  - Method-level: `@PreAuthorize` on service methods
  - Object-level: service-layer checks (task visibility = assignee, reporter, or same-team member)
- **Transport:** HTTPS enforced in production
- **Input safety:** Bean Validation on all request DTOs; JPA parameterized queries prevent SQL injection
- **File uploads:** content-type whitelist, size limits, randomized storage keys outside web root

---

## 9. Cross-Cutting Concerns

| Concern | Approach |
|---------|----------|
| Configuration | Spring profiles (`dev`, `test`, `prod`) + environment variables for secrets |
| DB Migrations | Flyway versioned scripts |
| Logging | SLF4J, correlation via request ID filter |
| Transactions | `@Transactional` at service layer |
| API Docs | Swagger UI at `/swagger-ui.html` |
| Testing | Unit tests per service; integration tests with Testcontainers |

---

## 10. Extension Points (Out-of-Scope, Designed For)

Per the problem statement's optional extensions, the architecture reserves:

1. **Real-time notifications** — an event abstraction (`TaskEventPublisher`) lets a future WebSocket/SSE module subscribe to domain events (e.g., `TaskAssignedEvent`) without touching existing services.
2. **AI-generated descriptions** — task creation flows through `TaskService.create()`, a single seam where an AI enrichment step can be added.
3. **Horizontal scaling** — stateless JWT means multiple app instances can run behind a load balancer once needed.

---

## 11. Deployment View (MVP)

```
[Load Balancer / Nginx]
        │
        ▼
[Spring Boot App]  ◄── containerized (Docker)
        │
        ▼
[PostgreSQL]
```

Single instance for MVP; the stateless design allows adding instances without architectural changes.
