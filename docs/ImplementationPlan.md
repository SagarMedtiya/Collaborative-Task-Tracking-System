# Implementation Plan: Task Tracking System

> Based on [ProblemStatement.md](ProblemStatement.md) and [architecture.md](architecture.md)

---

## Summary

The implementation is divided into **6 phases**, ordered by dependency — each phase builds on stable foundations from previous ones. Core user stories (US-01 to US-12) are covered by Phases 1–5; optional extensions are deferred to Phase 6.

| Phase | Focus | User Stories | Est. Duration |
|-------|-------|--------------|---------------|
| 1 | Project Foundation & Setup | — | Week 1 |
| 2 | Authentication & User Management | US-01, US-02, US-03, US-12 | Weeks 2–3 |
| 3 | Task Management | US-04, US-05, US-06, US-08, US-09 | Weeks 4–5 |
| 4 | Teams & Projects | US-11, US-07 | Week 6 |
| 5 | Collaboration (Comments & Attachments) | US-10 | Week 7 |
| 6 | Optional Extensions (Notifications, AI) | Extension stories | Week 8 (optional) |

---

## Phase 1: Project Foundation & Setup

**Goal:** Bootable application skeleton with CI-ready structure.

### Tasks
1. Initialize Spring Boot 3.x project (Java 17+, Maven) with dependencies:
   - `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-security`
   - `spring-boot-starter-validation`, `flyway-core`, `postgresql`
   - `jjwt`, `lombok`, `mapstruct`, `springdoc-openapi`
   - Test: `spring-boot-starter-test`, `testcontainers` (PostgreSQL)
2. Create package structure per architecture.md (`common/`, `security/`, `user/`, `task/`, `team/`)
3. Configure profiles (`dev`, `test`, `prod`) with env-variable secrets
4. Set up Flyway with initial migration (`V1__init.sql`)
5. Configure PostgreSQL via Docker Compose for local development
6. Add global exception handling (`@RestControllerAdvice`) and standard `ErrorResponse` / `ApiResponse` wrappers
7. Configure springdoc-openapi + Swagger UI
8. Health check endpoint; verify app boots and connects to DB

### Deliverables
- Running app at `/` with Swagger UI accessible
- Docker Compose for app + PostgreSQL
- Baseline test suite runs green

### Exit Criteria
- `mvn verify` passes; Swagger UI loads; Flyway migration applies cleanly

---

## Phase 2: Authentication & User Management

**Goal:** Secure registration/login with JWT; profile management.
**Covers:** US-01, US-02, US-03, US-12

### Tasks
1. **User entity & repository**
   - `users` table migration (id, email UQ, password hash, name, avatar, timestamps)
2. **Security configuration**
   - `SecurityFilterChain`: permit `/api/auth/**`, authenticate everything else
   - BCrypt `PasswordEncoder` (strength 12)
3. **Auth endpoints**
   - `POST /api/auth/register` — validation (email format, password strength), duplicate-email check → 409
   - `POST /api/auth/login` — verify credentials, issue JWT access token (~15 min expiry)
4. **JWT infrastructure**
   - `JwtService` (generate/validate), `JwtAuthFilter` registered before `UsernamePasswordAuthenticationFilter`
   - Stateless session management
5. **Profile endpoints**
   - `GET /api/users/me`, `PATCH /api/users/me`
6. **Logout strategy** — client-side token discard (stateless); document token expiry policy
7. **Tests**
   - Unit: JwtService, UserService
   - Integration: register → login → access protected endpoint (Testcontainers)

### Deliverables
- Full auth flow working end-to-end via Swagger
- Token-protected endpoints return 401 without valid JWT

### Exit Criteria
- All auth integration tests green; no plaintext passwords stored; invalid/expired tokens rejected with 401

---

## Phase 3: Task Management

**Goal:** Complete personal task CRUD with filtering, sorting, searching.
**Covers:** US-04, US-05, US-06, US-08, US-09

### Tasks
1. **Task entity & migrations**
   - `tasks` table: title, description TEXT, status enum (OPEN, IN_PROGRESS, COMPLETED), priority enum, due_date, assignee_id FK, reporter_id FK, timestamps
   - Indexes: `(assignee_id, status)`, `(due_date)`
2. **DTOs & validation** — TaskCreateRequest, TaskUpdateRequest, TaskResponse (MapStruct mappings)
3. **CRUD endpoints**
   - `POST /api/tasks`, `GET /api/tasks/{id}`, `PATCH /api/tasks/{id}`, `DELETE /api/tasks/{id}`
   - Ownership checks: only assignee/reporter can modify
4. **Query features** (JPA Specifications)
   - Filters: `status`, `assignee`, priority
   - Search: `q` on title/description
   - Sorting + pagination: `sort=dueDate,desc&page=&size=`
5. **Status transitions**
   - `PATCH` supports marking completed (US-06); enforce valid transitions if needed
6. **"My tasks" view** — `GET /api/tasks?assignee=me` shortcut
7. **Tests**
   - Repository tests for Specifications (filters/search/sort)
   - Service tests for ownership rules
   - Integration: full CRUD lifecycle

### Deliverables
- Task CRUD fully operational with pagination metadata
- Filter/search/sort verified via Swagger

### Exit Criteria
- All task endpoints tested; unauthorized modification returns 403; search returns relevant results

---

## Phase 4: Teams & Projects

**Goal:** Team/project creation, membership, cross-member task assignment.
**Covers:** US-11, US-07

### Tasks
1. **Entities & migrations**
   - `teams` (name, owner_id), `team_memberships` (user_id, team_id, role: OWNER/ADMIN/MEMBER, joined_at)
   - `projects` (name, team_id)
2. **Team endpoints**
   - `POST /api/teams`, `GET /api/teams` (my teams)
   - `POST /api/teams/{id}/members` (invite/add — OWNER/ADMIN only)
   - `DELETE /api/teams/{id}/members/{userId}`
   - Role-based authorization within teams
3. **Project endpoints**
   - CRUD under `/api/projects`, scoped to team membership
4. **Extend Task model**
   - Add `project_id` FK; tasks may belong to a project
   - Assignment rule: assignee must be a member of the task's project's team (US-07)
   - Visibility rule: team members can view tasks in shared projects
5. **Authorization refactor**
   - Extract membership-check service used by both team and task modules
6. **Tests**
   - Membership role enforcement (MEMBER cannot invite)
   - Cross-team assignment blocked
   - Team-scoped task visibility

### Deliverables
- Teams/projects CRUD working; tasks assignable across team members

### Exit Criteria
- Role enforcement tests pass; a member of one team cannot see or modify another team's data

---

## Phase 5: Collaboration — Comments & Attachments

**Goal:** Rich collaboration on tasks.
**Covers:** US-10

### Tasks
1. **Comments**
   - `comments` table (task_id, author_id, content, created_at); index on `task_id`
   - `GET/POST /api/tasks/{taskId}/comments`
   - Access: visible to anyone who can see the task; edit/delete restricted to author
2. **Attachments**
   - `attachments` table (task_id, file_name, storage_key, content_type, size_bytes, uploaded_by)
   - Local filesystem or MinIO-backed storage behind a `StorageService` interface
   - `POST /api/tasks/{taskId}/attachments` (multipart) — whitelist content types, size limit, randomized keys outside web root
   - `GET .../attachments/{id}` (download), `DELETE` (uploader or task owner)
3. **Validation & limits**
   - Max file size config, comment length limit
4. **Tests**
   - Upload/download round-trip; permission checks; oversized/malicious file rejection

### Deliverables
- Comments and attachments fully functional on tasks

### Exit Criteria
- File upload/download works end-to-end; unauthorized deletes rejected with 403

---

## Phase 6: Optional Extensions

> Deferred — implement only after core phases meet exit criteria.

### 6a. Real-Time Notifications (WebSockets/SSE)
1. Introduce domain events (`TaskAssignedEvent`, `TaskUpdatedEvent`) via Spring `ApplicationEventPublisher`
2. Add SSE endpoint `/api/notifications/stream` (or WebSocket STOMP) subscribing per authenticated user
3. Notify on assignment and status changes
4. Tests: event fired on assignment; stream delivers to subscribed user

### 6b. AI-Powered Descriptions/Summaries
1. Abstract behind `AiDescriptionService` interface
2. Endpoint `POST /api/tasks/ai/describe` — generate description from short user input
3. Provider-agnostic adapter (e.g., OpenAI-compatible API) with timeout/fallback
4. Never block core task creation on AI availability

---

## Dependency Map

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5
                │                       │
                │                       └──► Phase 6a (events from Phase 4 flows)
                └──────────────────────────► Phase 6b (builds on Phase 3 create flow)
```

Phases 2–5 are strictly sequential (each depends on prior entities/auth). Phase 6a/6b depend on Phases 3–4 but not on each other.

---

## Definition of Done (per phase)

- [ ] All listed tasks implemented
- [ ] Unit + integration tests pass (`mvn verify`)
- [ ] Endpoints documented in Swagger UI
- [ ] Flyway migration applied cleanly on fresh DB
- [ ] Manual smoke test via Swagger/postman collection
- [ ] No critical security findings (authz checks reviewed)

---

## Risk Register

| Risk | Impact | Mitigation |
|------|--------|------------|
| JWT token revocation needs (logout before expiry) | Medium | Short token TTL now; add refresh tokens/blacklist in Phase 6a if required |
| Attachment storage growth | Low | `StorageService` abstraction allows swap to S3/MinIO later |
| Search performance at scale | Low | Trigram/full-text index planned in Phase 3; upgrade path to Elasticsearch isolated in repository layer |
| Scope creep into Phase 6 | Medium | Strict exit criteria gates before starting optional work |
