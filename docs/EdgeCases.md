# Edge Cases & Error Handling Specification

> Based on [ImplementationPlan.md](ImplementationPlan.md) and [architecture.md](architecture.md)
>
> **Conventions used throughout:**
> - **Information hiding:** unauthorized access to an existing resource returns **404** (existence not revealed). 403 is reserved for cases where the user knows the resource exists (e.g., they are a member of the team but lack the role).
> - **Cascade policy:** deleting a team/project with dependent data is **blocked with 409 Conflict** — clients must delete or move children first.
>
> Format: **Scenario → Expected Behavior → Status → Phase**

---

## 1. Authentication & User Management (Phase 2)

| # | Scenario | Expected Behavior | Status | Notes |
|---|----------|-------------------|--------|-------|
| AUTH-01 | Register with already-existing email | Reject; do NOT reveal whether email exists in error copy beyond generic "registration failed" if enumeration is a concern (MVP: explicit 409 acceptable) | 409 | US-01 |
| AUTH-02 | Register with malformed email / weak password (<8 chars, no complexity) | Field-level validation errors returned as structured list | 400 | Bean Validation |
| AUTH-03 | Login with wrong password | Generic "invalid credentials" — identical response and timing whether email or password was wrong | 401 | Anti-enumeration |
| AUTH-04 | Login for non-existent account | Identical to AUTH-03 (same message, comparable latency) | 401 | |
| AUTH-05 | Request with expired JWT (>15 min) | Reject; client must re-authenticate | 401 | architecture.md §8 |
| AUTH-06 | Request with tampered/invalidly-signed JWT | Reject at filter; never reaches controller | 401 | |
| AUTH-07 | Request with no `Authorization` header on protected route | Reject before controller | 401 | |
| AUTH-08 | JWT of deleted/disabled user presented | Filter validates user still exists → reject | 401 | UserDetails check per request |
| AUTH-09 | `Authorization: Bearer` with empty/malformed token string | Reject cleanly, no stack trace leak | 401 | |
| AUTH-10 | Repeated failed logins from same account/IP (brute force) | MVP: log and monitor; rate limiting deferred to Phase 6a hardening | 200/401 | Risk register item |
| AUTH-11 | Two simultaneous profile updates (`PATCH /users/me`) | Last write wins on independent fields; document behavior | 200 | Optimistic locking optional |
| AUTH-12 | Registration payload missing required fields entirely | Per-field validation errors | 400 | |
| AUTH-13 | Logout then token reuse within TTL | Still valid (stateless); documented limitation — short TTL is mitigation | 200→valid | architecture.md §10 |

---

## 2. Task Management (Phase 3)

| # | Scenario | Expected Behavior | Status | Notes |
|---|----------|-------------------|--------|-------|
| TASK-01 | Access non-existent task ID | Not found | 404 | |
| TASK-02 | Access existing task user cannot see (not assignee/reporter/no team path) | Same 404 as TASK-01 — existence hidden | 404 | Info-hiding convention |
| TASK-03 | Modify/delete task without ownership (assignee≠user, reporter≠user) | Denied | 404 (hidden) or 403 if previously visible | Consistent with TASK-02 |
| TASK-04 | Create task with due date in the past | Allowed (backdating is legitimate); flagged in validation docs | 201 | Decision recorded |
| TASK-05 | Create task with blank title, or title >255 chars, description >10k chars | Validation error list | 400 | |
| TASK-06 | Invalid status value in filter (`?status=FOO`) | Validation error, not silent empty result | 400 | |
| TASK-07 | Invalid status transition via PATCH (if transitions enforced) | Conflict explaining allowed transitions | 400/409 | MVP: OPEN↔IN_PROGRESS↔COMPLETED free-form unless restricted |
| TASK-08 | Page number beyond last page | Empty content array with valid page metadata (Spring default) | 200 | |
| TASK-09 | Negative/huge `size=` parameter (e.g., `size=100000`) | Clamped to configured maximum | 200 | Prevent resource exhaustion |
| TASK-10 | Search `q` containing SQL metacharacters (`'; DROP TABLE --`) | Parameterized query — treated as literal text; returns 0–N results safely | 200 | JPA Specifications |
| TASK-11 | Search with only whitespace / empty `q` | Ignored as filter (returns unfiltered) or 400 — pick and document | 200 | |
| TASK-12 | Search containing unicode/emoji/CJK | Full match support; verify DB collation handles it | 200 | Test with multilingual input |
| TASK-13 | Assign task to non-existent userId | Not found | 400/404 | FK validation at service layer |
| TASK-14 | Assign to self vs others — assignee lacks visibility path | Blocked pre-Phase 4 (no teams yet): assignee must be self or exist | 400/404 | Tightened by TEAM rules in Phase 4 |
| TASK-15 | Delete task that has comments/attachments | Cascade-delete children (or block) — **decision:** cascade within task aggregate | 204 | Phase 5 dependency |
| TASK-16 | Duplicate task titles in same project | Allowed (titles are not unique keys) | 201 | |
| TASK-17 | Concurrent PATCH on same task | Last write wins; consider `@Version` optimistic locking if conflicts observed | 200 | Cross-cutting §6 |
| TASK-18 | Sort field that doesn't exist (`sort=foo`) | Validation error, not raw SQL exception | 400 | Whitelist sortable columns |

---

## 3. Teams & Projects (Phase 4)

| # | Scenario | Expected Behavior | Status | Notes |
|---|----------|-------------------|--------|-------|
| TEAM-01 | Create team with duplicate name | Allowed globally (names scoped per owner context); document choice | 201 | |
| TEAM-02 | Owner attempts to leave own team | Must transfer ownership first | 409 | Prevents orphaned team |
| TEAM-03 | Remove a member who was already removed | Idempotent success or 404 — pick idempotent (204) | 204 | Race-safe |
| TEAM-04 | Invite a user who is already a member | No-op success or 409 — recommend 409 with clear message | 409 | |
| TEAM-05 | Remove the last/only OWNER (without transfer) | Blocked | 409 | Mirrors TEAM-02 |
| TEAM-06 | MEMBER role attempts to invite/remove members | Forbidden (role enforcement) | 403 | Known-resource case → 403 not 404 |
| TEAM-07 | Non-member lists/modifies another team's projects or members | Hidden | 404 | Info-hiding convention |
| TEAM-08 | Assign task to user who is not a member of the task's project's team | Blocked | 409 | US-07 rule, architecture.md §7 |
| TEAM-09 | Delete team that still has projects/tasks | Blocked until children removed or moved | 409 | Cascade policy above |
| TEAM-10 | Delete project that has tasks | Blocked, or require `?force=true` cascade — recommend block | 409 | Consistent with TEAM-09 |
| TEAM-11 | Removed team member's assigned tasks remain | Tasks keep assignee; access re-evaluated dynamically — ex-member loses visibility | 200 | Visibility computed at request time, never denormalized into tokens |
| TEAM-12 | Membership added after task creation — new member sees historical tasks | Yes, membership grants retroactive visibility | 200 | |
| TEAM-13 | Self-invite to a team by guessing ID | Forbidden/hidden | 404 | |

---

## 4. Comments & Attachments (Phase 5)

| # | Scenario | Expected Behavior | Status | Notes |
|---|----------|-------------------|--------|-------|
| CMT-01 | Comment on a task visible to requester | Allowed | 201 | |
| CMT-02 | Comment on a task NOT visible to requester | Hidden — same as no task | 404 | Info-leak prevention |
| CMT-03 | Edit/delete another user's comment | Author-only; others denied | 403 | Task owner does NOT get comment moderation in MVP |
| CMT-04 | Empty comment / exceeds max length (e.g., 5000 chars) | Validation error | 400 | |
| CMT-05 | Comment containing emoji/unicode/RTL text/newlines | Stored and returned byte-exact | 201 | UTF-8 column encoding verified |
| ATT-01 | Upload file exceeding size limit (e.g., >10 MB) | Rejected before storage I/O | 413 | Spring multipart limit |
| ATT-02 | Upload disallowed content type (`.exe`, `.bat`) | Whitelist rejection (pdf, images, docs, txt, zip) | 415 | architecture.md §8 |
| ATT-03 | Upload with mismatched extension vs actual content (magic bytes) | Content-sniff check where feasible | 415 | Best-effort MVP |
| ATT-04 | Filename contains path traversal (`../../etc/passwd`, `..\\..\\win.ini`) | Storage key randomized server-side; original name sanitized/stored as metadata only | 201 (safe) | Critical security case |
| ATT-05 | Duplicate filenames on same task | Allowed — unique generated storage keys prevent overwrite | 201 | |
| ATT-06 | Zero-byte file upload | Reject or allow — recommend reject with clear message | 400 | |
| ATT-07 | Download attachment on invisible task | Hidden | 404 | Same rule as CMT-02 |
| ATT-08 | Delete attachment — non-uploader, non-task-owner | Denied | 403 | Uploader or task owner may delete |
| ATT-09 | Delete attachment while another user is mid-download | Storage object removal; in-flight download completes or 404s — acceptable either way, document | 204/404 | |
| ATT-10 | Attachment uploaded by user who later leaves team | File persists; access follows task visibility rules (TEAM-11) | 200 | |

---

## 5. Optional Extensions (Phase 6)

| # | Scenario | Expected Behavior | Status | Notes |
|---|----------|-------------------|--------|-------|
| AI-01 | AI provider timeout/unavailable during describe call | Fallback: return 503 with hint, core task creation unaffected | 503 | Never blocks CRUD |
| AI-02 | AI returns empty/oversized output | Sanitize/truncate before returning | 200 | |
| AI-03 | Malicious prompt content in AI input | Treated as plain text; output rendered as text downstream | 200 | Frontend concern too |
| SSE-01 | Client disconnects mid-stream and reconnects | Reconnect delivers events from reconnect point (Last-Event-ID) or accepts gap — document chosen semantics | 200 | |
| SSE-02 | User assigns task to themselves | No notification sent to actor (self-action suppression) | — | Avoid noise |
| NTF-01 | Notification triggered for user who lost team access mid-flight | Delivery skipped silently | — | Visibility checked at emit time |

---

## 6. Cross-Cutting Edge Cases

| # | Scenario | Expected Behavior | Status | Notes |
|---|----------|-------------------|--------|-------|
| XCUT-01 | Malformed JSON body | Structured parse-error response, no stack trace | 400 | Global handler |
| XCUT-02 | Wrong `Content-Type` (e.g., form-encoded on JSON endpoint) | Unsupported media type | 415 | |
| XCUT-03 | Unknown URL path | JSON 404, not container HTML error page | 404 | |
| XCUT-04 | Unhandled server exception (NPE etc.) | Generic 500 envelope; details logged server-side only, correlation ID returned | 500 | Never leak internals |
| XCUT-05 | HTTP method mismatch (DELETE on collection endpoint) | Method not allowed + Allow header | 405 | |
| XCUT-06 | DB constraint violation surfaces (race between check and insert, e.g., duplicate invite) | Mapped to friendly conflict response, not raw DataIntegrityViolation | 409 | Global exception mapping |
| XCUT-07 | Request body larger than configured max | Rejected | 413 | |
| XCUT-08 | Missing pagination params | Sensible defaults applied (`page=0&size=20`) | 200 | |
| XCUT-09 | Clock skew — token issued "in future" | JWT lib rejects; NTP assumed on hosts | 401 | Operational note |
| XCUT-10 | Simultaneous registration with same email (race past AUTH-01 check) | Unique DB constraint is final arbiter → mapped to 409 | 409 | Check + constraint defense-in-depth |

---

## Usage

Each row maps directly to a test case:

- **Unit tests** → service-layer scenarios (ownership, roles, validation logic)
- **Integration tests** → full HTTP round-trips asserting status codes (per ImplementationPlan.md Definition of Done)
- **Security review checklist** → all info-hiding rows (TASK-02, TEAM-07, CMT-02, ATT-07, TEAM-13) and traversal/injection rows (TASK-10, ATT-04)
