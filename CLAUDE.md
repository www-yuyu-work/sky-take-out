# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build the entire project
mvn clean package -DskipTests

# Build a specific module
mvn clean package -pl sky-server -am -DskipTests

# Run the Spring Boot application (from sky-server)
mvn spring-boot:run -pl sky-server

# Run tests
mvn test
mvn test -pl sky-server -Dtest=EmployeeServiceTest
```

The application starts on port **8080**. The active Spring profile is `dev` (configured in `application.yml`).
API docs (Knife4j/Swagger): `http://localhost:8080/doc.html`

## Prerequisites

- **MySQL** on `localhost:3306`, database `sky_take_out`, user `root` / `root`
- **Redis** on `192.168.190.128:6379`, password `123456`
- A separate Vue3 **frontend project** served via nginx (not in this repo)

## Module Architecture

This is a **Maven multi-module** project (Spring Boot 2.7.3):

| Module | Purpose |
|--------|---------|
| `sky-common` | Shared utilities: JWT, AliOSS, WeChat Pay, constants, custom exceptions, ThreadLocal context, uniform `Result<T>` response wrapper, configuration properties |
| `sky-pojo` | Data objects: entities, DTOs, VOs. Entities map to DB tables; DTOs are request bodies; VOs are response bodies. Uses Lombok + Jackson + Knife4j annotations. |
| `sky-server` | Main application. Controllers, service layer, MyBatis mappers, Spring configuration, interceptors. Depends on both `sky-common` and `sky-pojo`. |

Dependency chain: `sky-server` → `sky-common` + `sky-pojo`

## Request Flow

```
Client → JwtTokenAdminInterceptor → Controller → Service → Mapper → DB
                                      ↓
                              GlobalExceptionHandler (catches BaseException subclasses)
```

- **Interceptor**: `JwtTokenAdminInterceptor` reads the token from the request header (name configured in `sky.jwt.admin-token-name`, default `token`), parses it with `JwtUtil`, and stores the employee ID in `BaseContext` (a `ThreadLocal<Long>`). All `/admin/**` routes are protected **except** `/admin/employee/login` and `/admin/employee/logout`.
- **Controllers**: REST controllers under `com.sky.controller.admin` (admin-side). Annotated with Knife4j `@Api` / `@ApiOperation` for API docs.
- **Uniform response**: Every controller returns `Result<T>` — `code=1` for success, `code=0` for failure with a `msg`.
- **Exception handling**: Business exceptions extend `BaseException` and are caught by `GlobalExceptionHandler` (`@RestControllerAdvice`), which returns `Result.error(msg)`.

## Key Patterns

### Database access
MyBatis with a hybrid approach:
- **Simple queries**: `@Select` / `@Insert` annotations directly on mapper interfaces
- **Complex queries** (dynamic WHERE, conditional updates): XML mapper files in `sky-server/src/main/resources/mapper/*.xml`
- **Pagination**: PageHelper (`PageHelper.startPage(page, pageSize)`) with `PageResult(total, list)` as the return wrapper
- Mapper interfaces are annotated `@Mapper`, scanned from `com.sky.mapper`
- Entity aliases configured as `com.sky.entity`, camel-case mapping enabled

### Current user context
`BaseContext.getCurrentId()` returns the employee ID of the authenticated user (set by the JWT interceptor). Services call this to populate `createUser`/`updateUser` fields. Always call `BaseContext.removeCurrentId()` after use in non-request-scoped contexts.

### Password handling
- Default password defined in `PasswordConstant.DEFAULT_PASSWORD`
- Passwords are MD5-hashed via `DigestUtils.md5DigestAsHex()` before storage or comparison

### Status constants
`StatusConstant.ENABLE = 1`, `StatusConstant.DISABLE = 0` — used for employee account state and shop business status.

### Shop status
Shop open/close state is stored in **Redis** (key `SHOP_STATUS`) rather than the database. `ShopController` reads/writes it via `StringRedisTemplate`.

### External integrations (utilities exist, may not be fully wired yet)
- **AliOSS**: `AliOssUtil` + `AliOssProperties` for file uploads
- **WeChat Pay**: `WeChatPayUtil` + `WeChatProperties` for payment processing

## Adding a New Admin Feature

Follow the existing layer pattern:

1. **Entity** in `sky-pojo/src/main/java/com/sky/entity/` — maps to a DB table, uses `@Data @Builder @NoArgsConstructor @AllArgsConstructor`
2. **DTO** in `sky-pojo/src/main/java/com/sky/dto/` — request body for create/update/query
3. **VO** in `sky-pojo/src/main/java/com/sky/vo/` — response body for queries
4. **Mapper** interface in `sky-server/.../mapper/` + optional XML in `sky-server/src/main/resources/mapper/`
5. **Service** interface in `sky-server/.../service/` + impl in `service/impl/`
6. **Controller** in `sky-server/.../controller/admin/` — use `@RestController`, `@RequestMapping("/admin/xxx")`, annotate with `@Api(tags = "...")` and `@ApiOperation("...")` on each method
7. If the endpoint is **not** login/logout, the JWT interceptor will automatically protect it — no extra config needed unless you want to exclude it in `WebMvcConfiguration`

## Key Configuration Properties

All under `sky.*` prefix, defined in `sky-common` properties classes:

- `sky.jwt.admin-secret-key` — JWT signing key (default: `itcast`)
- `sky.jwt.admin-ttl` — token expiration in milliseconds (default: `7200000` = 2 hours)
- `sky.jwt.admin-token-name` — header name for the token (default: `token`)
- DB and Redis config in `application-dev.yml` under `sky.datasource.*` and `spring.redis.*`
