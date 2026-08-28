# CodeArena Backend — Phase 1 (Foundation + Auth)

## What's in this phase
- Maven project skeleton, Java 17, Spring Boot 3.3.2
- Layered architecture: controller / service / serviceImpl / repository / entity / dto / mapper / config / security / exception
- JWT authentication (access + refresh tokens) with BCrypt password hashing
- **Role as a real entity** (`roles` table) referenced from `User` via `@ManyToOne`, not a hardcoded enum column — see "Why these decisions" below
- Role-based authorization (`ROLE_USER`, `ROLE_ADMIN`) via `@PreAuthorize`
- Startup data seeding: both roles + a bootstrap admin account, so RBAC is testable immediately on a fresh DB
- Global exception handling → uniform `{success,status,error,message,path,details,timestamp}` error shape
- Swagger/OpenAPI UI wired with a Bearer JWT scheme
- Pagination + sorting + searching pattern demonstrated on `GET /api/v1/users`
- Unit tests (JUnit 5 + Mockito) for `AuthServiceImpl`, plus a Spring context smoke test on H2
- Login accepts **username or email** interchangeably

## Endpoints so far
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/v1/auth/register | public | Create an account (assigned `ROLE_USER`) |
| POST | /api/v1/auth/login | public | Login with username OR email; returns access + refresh tokens |
| POST | /api/v1/auth/refresh | public | Rotate tokens |
| GET | /api/v1/users/me | authenticated | Current user profile |
| GET | /api/v1/users?search=&page=&size=&sort= | ADMIN only | Paginated user search |

## Bootstrap admin account
On first startup, `DataInitializer` seeds the `roles` table and — if no user with the configured admin username exists — creates a default admin:
```
username: admin           (ADMIN_USERNAME)
email:    admin@codearena.local  (ADMIN_EMAIL)
password: Admin@12345     (ADMIN_PASSWORD)
```
**Change this password immediately** in any real environment by setting the `ADMIN_PASSWORD` env var before first boot, or by logging in and adding a change-password endpoint (planned for a later phase).

## Run it locally

1. Start MySQL (or use the docker-compose in a later phase) and create credentials matching the env vars below, or just rely on `createDatabaseIfNotExist=true`.
2. Set environment variables (or edit `application.yml` directly for local dev):
   ```
   DB_HOST=localhost
   DB_PORT=3306
   DB_NAME=codearena
   DB_USERNAME=root
   DB_PASSWORD=root
   JWT_SECRET=<64+ char hex string>
   ADMIN_USERNAME=admin
   ADMIN_EMAIL=admin@codearena.local
   ADMIN_PASSWORD=<strong password>
   ```
3. Build and run:
   ```
   mvn clean install
   mvn spring-boot:run
   ```
4. Swagger UI: http://localhost:8080/swagger-ui.html

## Run tests
```
mvn test
```
Tests run against an in-memory H2 database (`application-test.yml`), so no MySQL instance is required to run `mvn test`.

## Why these decisions
- **Role as an entity, not an enum column on User**: a `roles` table lets us look up/list roles, and later extend them with a permissions join table, without an ALTER TABLE on `users`. The *set* of valid role names is still enum-backed (`RoleName`) for compile-time safety — only the entity wrapping it is new.
- **`DataInitializer` seeds roles + a bootstrap admin**: registration and admin-only endpoints both depend on a `roles` row existing; seeding it on startup means a fresh database is immediately usable, and there's always a way into ADMIN-only endpoints without manual SQL.
- **Stateless JWT, no sessions**: horizontally scalable, no server-side session store to manage.
- **DaoAuthenticationProvider + AuthenticationManager**: lets us reuse Spring Security's own credential-checking (timing-safe BCrypt comparison) instead of hand-rolling it in the service layer.
- **ApiResponse<T> / ErrorResponse wrappers**: every endpoint — success or failure — returns one predictable JSON shape, so the React frontend can have one Axios interceptor instead of per-endpoint parsing logic.
- **MapStruct over manual mapping**: compile-time generated mappers (`UserMapperImpl`) — no reflection cost at runtime, and a missing field mapping is caught by the compiler, not in production. `UserResponse.role` is the flat `RoleName` enum (not the `Role` entity) — clients need the name, not our internal FK/id.
- **BaseEntity with JPA auditing**: `createdAt`/`updatedAt` are needed on almost every entity in this platform (Problem, Submission, Contest, etc.); centralizing it now avoids repeating it 10 times later.
- **`open-in-view: false`**: forces us to fetch what we need inside the service/transaction boundary rather than lazily loading in the controller/serialization layer, which is a common source of N+1 queries and `LazyInitializationException`s in sloppier Spring apps.
- **Role fetched EAGER on User**: a user's role is read on almost every request (auth, `/me`, admin listings), so LAZY would just mean an extra round trip almost every time. EAGER is reserved here for this one small, rarely-changing, always-needed reference — not a blanket policy for every association (e.g. a user's submissions will be LAZY).

## Not yet built (upcoming phases)
Problem CRUD, code submission/judging engine, contests, leaderboard, frontend, Docker Compose.

---

# Phase 2 — Problem Management

## What's new
- **Entities**: `Problem` (aggregate root), `Tag`, `ProblemTag` (explicit join entity), `TestCase`, plus `ProblemExample` (embeddable) and `DifficultyLevel` (enum).
- **Admin**: create / update (full-replace) / delete problems, each with nested tags, examples, hints, and test cases (hidden + visible) in one request.
- **Public**: search/filter/paginate/sort problems (`GET /api/v1/problems`), full detail by slug (`GET /api/v1/problems/{slug}`), tag listing (`GET /api/v1/tags`).
- **Security boundary**: the public problem-detail response never includes `editorial` or hidden test cases — only `visibleTestCases`. A separate admin-only response (`ProblemAdminResponse`) includes everything, gated by `@PreAuthorize("hasRole('ADMIN')")`.

## New endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| GET | /api/v1/problems?search=&difficulty=&tags=&page=&size=&sort= | public | Search/filter/paginate/sort problem summaries |
| GET | /api/v1/problems/{slug} | public | Full problem detail (no editorial/hidden tests) |
| GET | /api/v1/tags | public | List all tags |
| POST | /api/v1/admin/problems | ADMIN | Create a problem (tags/examples/hints/testCases nested) |
| PUT | /api/v1/admin/problems/{id} | ADMIN | Update a problem (full replace) |
| DELETE | /api/v1/admin/problems/{id} | ADMIN | Delete a problem |
| GET | /api/v1/admin/problems/{id} | ADMIN | Full admin detail (editorial + all test cases) |
| POST | /api/v1/tags | ADMIN | Create a tag |

## Why these decisions
- **Problem as an aggregate root** owning `TestCase` and `ProblemTag` via `cascade = ALL, orphanRemoval = true`: there's no independent repository/controller for either — they only make sense attached to a problem, so `Problem.replaceTestCases()`/`replaceProblemTags()` centralize the clear-then-repopulate pattern used on full-replace updates.
- **`ProblemTag` as an explicit join entity**, not an implicit `@ManyToMany` table: gives the association its own identity and room to grow (e.g. `addedBy`/`addedAt`) without a migration later.
- **Examples/hints as element collections**, not their own tables: they're simple, always-problem-owned data with no independent lifecycle.
- **JPA Specifications** (`ProblemSpecification`) instead of one `@Query` with many optional params: each filter (title search, difficulty, tags) is independently composable — adding a new filter later means one new static method, not touching existing ones.
- **`@BatchSize(25)`** on `testCases`/`problemTags`: these are LAZY collections read on every list/search row; batching keeps that from becoming an N+1 query problem without forcing EAGER everywhere.
- **Slug uniqueness**: auto-generated slugs get an auto-incrementing suffix on collision; an *explicitly* admin-provided slug instead fails loudly with a 409 on collision — silently rewriting what an admin typed felt like the wrong default.
- **Update is full-replace**: the entire `tags`/`examples`/`hints`/`testCases` collections are replaced wholesale on `PUT`, matching what a typical "edit problem" admin form submits. A partial PATCH can be added later without breaking this contract.

## Testing steps
1. `mvn clean install` (builds + runs `ProblemServiceImplTest`, `AuthServiceImplTest`, and the context smoke test against H2).
2. Start MySQL, then `mvn spring-boot:run`.
3. Log in as the bootstrap admin (`POST /api/v1/auth/login` with `admin` / your configured `ADMIN_PASSWORD`) to get an access token.
4. In Swagger UI (http://localhost:8080/swagger-ui.html), click "Authorize" and paste the access token.
5. `POST /api/v1/admin/problems` with a body like:
   ```json
   {
     "title": "Two Sum",
     "difficulty": "EASY",
     "description": "Given an array of integers, return indices of the two numbers that add up to target.",
     "timeLimitMs": 2000,
     "memoryLimitKb": 262144,
     "tags": ["array", "hash-table"],
     "examples": [{"input": "[2,7,11,15], target=9", "output": "[0,1]", "explanation": "2 + 7 = 9"}],
     "hints": ["Try a hash map."],
     "testCases": [
       {"input": "[2,7,11,15]\n9", "expectedOutput": "[0,1]", "hidden": false},
       {"input": "[3,2,4]\n6", "expectedOutput": "[1,2]", "hidden": true}
     ]
   }
   ```
6. `GET /api/v1/problems` (no auth needed) — confirm the new problem appears with only its summary fields.
7. `GET /api/v1/problems/two-sum` (no auth needed) — confirm `visibleTestCases` has exactly 1 entry and there is no `editorial` field, even though one wasn't set.
8. `GET /api/v1/admin/problems/{id}` (as admin) — confirm both test cases appear.
9. Try `POST /api/v1/admin/problems` again with a non-admin JWT (register a second user, log in as them) — expect `403 Forbidden`.
10. `PUT /api/v1/admin/problems/{id}` with an updated body — confirm old test cases/tags are replaced, not appended.

## Git commit message
```
feat(problems): add Phase 2 problem management module

- Add Problem (aggregate root), Tag, ProblemTag (explicit join entity),
  TestCase entities, plus ProblemExample embeddable and DifficultyLevel enum
- Admin CRUD for problems with nested tags/examples/hints/test cases
  (full-replace semantics on update)
- Public search/filter/paginate/sort over problems via JPA Specifications
  (title search, difficulty, tag-in-list filters, independently composable)
- Enforce visibility boundary: public responses exclude editorial and
  hidden test cases; ProblemAdminResponse (ADMIN-only) exposes both
- Auto-generate unique slugs from title with collision suffixing;
  explicit admin-provided slugs fail loudly (409) on collision instead
  of being silently rewritten
- Add @BatchSize(25) on Problem's lazy collections to avoid N+1 queries
  on list/search endpoints
- Add ProblemServiceImplTest (JUnit 5 + Mockito) covering slug
  generation/collision, tag resolution, and not-found/duplicate paths
```

## Not yet built (upcoming phases)
Code submission/judging engine, contests, leaderboard, frontend, Docker Compose.

---

# Phase 3 — Online Judge

## What's new
- **Pluggable execution engine**: `judge.CodeExecutionService` is the only contract the rest of the app depends on. Today `Judge0ExecutionServiceImpl` is the sole implementation; a Docker-sandbox implementation can be added later against the same interface with zero changes elsewhere (Open/Closed principle).
- **Run Code**: ad-hoc execution against custom input, authenticated but **never persisted**.
- **Submit Code**: grades code against a problem's *entire* test-case set (hidden + visible), persists the result (source code, verdict, runtime, memory, language, pass count).
- **Verdicts**: `ACCEPTED`, `WRONG_ANSWER`, `COMPILATION_ERROR`, `RUNTIME_ERROR`, `TIME_LIMIT_EXCEEDED`, `MEMORY_LIMIT_EXCEEDED`, plus `PENDING`/`INTERNAL_ERROR` for judge-side failures.
- **Submission history**: owner-scoped for regular users, unrestricted for admins, both paginated/filterable by problem/verdict/language.

## New endpoints
| Method | Path | Auth | Description |
|---|---|---|---|
| POST | /api/v1/run | authenticated | Run code against custom input, get raw output back |
| POST | /api/v1/submissions | authenticated | Submit code for grading against a problem |
| GET | /api/v1/submissions/{id} | owner only | Full detail of one of *your* submissions |
| GET | /api/v1/submissions?problemId=&verdict=&language=&page=&size=&sort= | authenticated | Your own submission history |
| GET | /api/v1/admin/submissions/{id} | ADMIN | Any submission, regardless of owner |
| GET | /api/v1/admin/submissions?userId=&problemId=&verdict=&language= | ADMIN | Search/filter across all users' submissions |

## Judge0 setup
Point `JUDGE0_BASE_URL` at either:
- A **self-hosted Judge0 CE** instance (e.g. via their official `docker-compose.yml` — `http://localhost:2358`, no auth needed), or
- **RapidAPI's hosted Judge0** (`https://judge0-ce.p.rapidapi.com`), in which case also set `JUDGE0_RAPIDAPI_KEY` and `JUDGE0_RAPIDAPI_HOST`.

## Why these decisions
- **`CodeExecutionService` interface + judge-agnostic DTOs** (`CodeExecutionRequest`/`Result`): no Judge0-specific type (base64 flags, `cpu_time_limit`, status IDs) leaks past `Judge0ExecutionServiceImpl`. This is what makes "Docker sandbox later" a real option rather than a rewrite.
- **Always submit-then-poll, never rely on `wait=true`**: Judge0's synchronous mode isn't guaranteed identical across every self-hosted/RapidAPI deployment; polling with a bounded attempt budget works uniformly against any Judge0 CE-compatible instance.
- **We compute Memory Limit Exceeded ourselves**: Judge0 doesn't reliably expose a dedicated MLE status (an OOM kill usually surfaces as a Runtime Error signal). We compare Judge0's reported memory against the problem's configured `memoryLimitKb` and override the verdict when exceeded — this check takes priority over whatever Judge0 concluded.
- **Worst-verdict aggregation** (`COMPILATION_ERROR > INTERNAL_ERROR > RUNTIME_ERROR > TIME_LIMIT_EXCEEDED > MEMORY_LIMIT_EXCEEDED > WRONG_ANSWER > ACCEPTED`): a single ordered priority list in `SubmissionServiceImpl`, not scattered if/else — adding a new verdict later is a one-line change to that list.
- **Run is never persisted**: only Submit creates a durable record, matching how every real online judge treats "quick test" vs. "official attempt."
- **`getOwnSubmission` returns 404, not 403, for someone else's submission**: doesn't confirm to a caller that a given submission ID belongs to another user — same information-minimization principle used elsewhere in the app.
- **Judge0StatusMapper as a standalone pure function**: the Judge0 status-ID → Verdict translation has zero dependencies, so it's unit tested directly without mocking any HTTP client.

## Testing steps
1. Start a Judge0 instance (self-hosted via Docker Compose, or configure RapidAPI credentials).
2. `mvn clean install` (runs `Judge0StatusMapperTest` and `SubmissionServiceImplTest`, neither of which needs a live judge).
3. `mvn spring-boot:run`, then via Swagger UI, authenticated as any user:
   - `POST /api/v1/run` with a small script and custom `stdin` — confirm `stdout` comes back correctly.
   - Try code with a syntax error — confirm `status: COMPILATION_ERROR` and `compileOutput` is populated.
4. As admin, create a problem (Phase 2) with a couple of test cases.
5. `POST /api/v1/submissions` with correct code — confirm `verdict: ACCEPTED`, `testCasesPassed` equals `testCasesTotal`, and `runtimeMs`/`memoryKb` are populated.
6. Submit code that fails one hidden test case — confirm the overall verdict reflects that failure even though visible test cases passed.
7. `GET /api/v1/submissions` — confirm your submission appears in history.
8. Log in as a second user and try `GET /api/v1/submissions/{id}` for the first user's submission ID — expect `404`, not `403`.
9. As admin, `GET /api/v1/admin/submissions/{id}` for that same ID — confirm it *is* visible.

## Git commit message
```
feat(judge): add Phase 3 online judge (Run/Submit via Judge0)

- Add pluggable CodeExecutionService interface with Judge0ExecutionServiceImpl
  as the sole current implementation; judge-agnostic request/result DTOs so
  a future Docker-sandbox engine can be swapped in without touching callers
- Add Submission entity/repository/specification, Language and Verdict enums
- Run Code: ad-hoc execution against custom input, never persisted
- Submit Code: grades against a problem's full (hidden+visible) test-case
  set via Judge0's batch API, persists code/verdict/runtime/memory/language
- Worst-verdict aggregation across test cases via an explicit priority list
- Compute Memory Limit Exceeded ourselves by comparing Judge0's reported
  memory against the problem's configured limit (Judge0 doesn't reliably
  expose a dedicated MLE status)
- Owner-scoped submission history + detail; admin endpoints for
  unrestricted cross-user visibility, both paginated/filterable
- Judge0StatusMapper extracted as a standalone pure function, unit tested
  directly without mocking HTTP
- Add SubmissionServiceImplTest and Judge0StatusMapperTest
```

## Not yet built (upcoming phases)
Docker-sandbox execution engine (as a second CodeExecutionService implementation), contests, leaderboard, frontend, Docker Compose for the whole stack.
