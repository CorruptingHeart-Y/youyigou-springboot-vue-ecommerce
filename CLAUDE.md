# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build all modules (skip tests for speed)
mvn clean package -DskipTests

# Build with tests
mvn clean package

# Run a single service (order matters: user → product → order → gateway)
java -jar ecommerce-user/target/ecommerce-user-1.0.0.jar
java -jar ecommerce-product/target/ecommerce-product-1.0.0.jar
java -jar ecommerce-order/target/ecommerce-order-1.0.0.jar
java -jar ecommerce-gateway/target/ecommerce-gateway-1.0.0.jar

# Frontend
cd frontend && npm install && npm run build    # production build → frontend/dist/
cd frontend && npm run dev                      # Vite dev server on :5200, proxies API to :8080

# Docker (full stack)
docker-compose up -d
```

**Prerequisites:** JDK 23, Maven 3.8+, MySQL 8.0+, Redis, Node.js 18+.

Initialize the database: `mysql -u root -p < sql/init.sql` (creates `ecommerce` db, default admin `admin`/`123456`).

## Architecture

This is a **Spring Boot 3.3.6 + Vue 3** microservices e-commerce platform built as a university course project.

```
Browser → Gateway (:8080) ─┬→ User Service (:8081)      /api/auth/**, /api/admin/users/**
                            ├→ Product Service (:8082)    /api/products/**, /api/admin/products/**
                            ├→ Order Service (:8083)      /api/client/**, /api/admin/order/**
                            └→ User Service (:8081)      /ws/chat/** (WebSocket proxy)
```

### Module layout

| Module | Purpose |
|--------|---------|
| `ecommerce-common` | Shared entities (11 tables), DTOs, mappers, JWT security, WebSocket, global config (SecurityConfig, Redis, CORS, Jackson, MyBatis-Plus) |
| `ecommerce-gateway` | Reverse proxy on :8080 — API routing, static resource serving (`frontend/dist/`), SPA fallback, CORS filter |
| `ecommerce-user` | Auth (login/register/JWT), profile, address CRUD, email verification codes |
| `ecommerce-product` | Product/category CRUD, reviews, favorites, banners, announcements, promotions, seckill |
| `ecommerce-order` | Cart, orders, dashboard stats, feedback, logistics, coupons, seckill execution |

### Key patterns

- **Shared module pattern**: `ecommerce-common` is a dependency of all three service modules. Entities, mappers, DTOs, and config live there once and are component-scanned by each service via `@MapperScan` and Spring Boot auto-config.
- **JWT auth flow**: `JwtAuthenticationFilter` (in common) intercepts every request, extracts user info from the token, and loads dynamic permissions from `e_permission`/`e_role_permission` tables into `GrantedAuthority`. `@PreAuthorize` on admin controllers enforces fine-grained access. Tokens are cached in Redis keyed by `user:token:{userId}`.
- **Gateway routing**: `GatewayController` uses `RestTemplate` to forward API calls by path prefix. Non-API routes return `index.html` for Vue Router history mode. Static resources are served from `frontend/dist/` and `uploads/`. `GatewaySecurityConfig` uses `anyRequest().permitAll()` — auth is enforced by downstream services. The gateway's `@SpringBootApplication` (no `scanBasePackages`) only scans `com.ecommerce.gateway`, so `com.ecommerce.config` classes from `ecommerce-common` are NOT loaded in the gateway context.
- **Seckill**: Redis `DECR` for atomic inventory deduction with rollback. Inventory is pre-warmed into Redis before a promotion starts.
- **Config duplication**: Each service module has its own `application.yml` with the same datasource, Redis, and JWT secret (they must match).

### Ports

| Service | Port |
|---------|------|
| Gateway | 8080 |
| User | 8081 |
| Product | 8082 |
| Order | 8083 |
| MySQL | 3306 |
| Redis | 6379 |
| Vite dev | 5200 |

### User roles

`USER`, `ADMIN`, `SUPER_ADMIN` — stored in `e_user.role`. Dynamic permissions are assigned via `e_role_permission` linking roles to `e_permission` entries.

## WebSocket (chat)

```
Browser ─ws─→ Gateway :8080 ─ws─→ User Service :8081
              WebSocketProxyServer    ChatWebSocket
              (relay only, no logic)  (auth + message handling)
```

- **Gateway** (`WebSocketProxyServer`): accepts client connections at `/ws/chat/{token}`, transparently relays to user service. Does NOT validate tokens — the backend handles auth.
- **User Service** (`ChatWebSocket`): performs JWT validation via `@PathParam("token")`, manages `onlineUsers` map, broadcasts messages. Token is extracted from URL path (not `Authorization` header) because browser WebSocket API cannot set custom headers.
- **Frontend** (`Chat.vue`): connects to `ws://${location.host}/ws/chat/${token}`. Dev mode goes through Vite proxy (`/ws` → `:8080`, `ws: true`). Production goes directly to gateway.
- `WebSocketConfig` in `ecommerce-common` is skipped on the gateway (`@ConditionalOnExpression("${spring.application.name} != 'api-gateway'")`). The gateway has its own `GatewayWebSocketConfig`.
- `@ServerEndpoint` classes MUST be Spring beans (`@Component`) — `ServerEndpointExporter` discovers them via `getBeansWithAnnotation(ServerEndpoint.class)`. If the endpoint is not a Spring bean, the WebSocket is never registered and connections fail with immediate close.
- Security: `/ws/**` is `permitAll()` in both `SecurityConfig` (common) and `GatewaySecurityConfig`. WebSocket upgrade requests carry the JWT in the URL path, so `JwtAuthenticationFilter` (which reads the `Authorization` header) passes them through.

### WebSocket troubleshooting

**"Connected then immediately disconnected"**: 99% of the time this means `ChatWebSocket` or `WebSocketProxyServer` is missing `@Component`. Without it, Tomcat never registers the endpoint, the upgrade request gets a normal HTTP response instead of 101, and the browser fires `onclose` instantly. Check: is `@Component` on the `@ServerEndpoint` class?

**"Backend unreachable" (proxy-specific)**: Ensure the user service is running on :8081 and `services.user` in gateway's `application.yml` points to `http://localhost:8081`. The proxy converts `http://` → `ws://` automatically.

**"Auth failed" in user-service logs**: The JWT token in the URL path is invalid or expired. Check that the frontend sends a fresh token and that all services share the same `jwt.secret` in `application.yml`.

## Email configuration

Email sending for verification codes is in `ecommerce-user`. The JavaMailSender is configured in `application.yml` under `spring.mail`. The sending code is in `UserServiceImpl.sendVerificationCode()` — it reads `spring.mail.username` via `@Value` and sets it as the `From` address to avoid sender/auth mismatch rejection.

## Static file serving (uploads)

Product images, banners, and other uploaded files are served as static resources from the `uploads/` directory at the project root.

```
Upload → AdminSiteController (product service)       saves to uploads/
                                                       returns "/uploads/uuid.ext"
Serve  → Gateway StaticResourceConfig                 maps /uploads/** → file:../uploads/
       → WebMvcConfig (in ecommerce-common)            fallback for direct service access
Dev    → Vite proxy /uploads → :8080                   so dev server can serve uploads
```

- **Upload endpoint**: `POST /api/admin/site/upload` in `AdminSiteController` (ecommerce-product, `com.ecommerce.product.controller.admin`). Saves files to `{app.upload.dir}` (default `uploads/`) with UUID filenames, returns `/uploads/{filename}`.
- **Gateway serving**: `StaticResourceConfig` in ecommerce-gateway resolves `/uploads/**` to a proper `file:` URL. It searches for the `uploads/` directory in order: (1) relative to working directory, (2) one level up `../`, (3) two levels up `../../`. Uses Java's `Path.toUri().toString()` to produce valid `file:///C:/...` URLs — never `"file:" + absolutePath` which produces broken `file:C:\...` backslash URLs on Windows. The gateway only component-scans `com.ecommerce.gateway` (default `@SpringBootApplication` behavior), so `WebMvcConfig` from `ecommerce-common` (`com.ecommerce.config` package) is NOT loaded in the gateway context.
- **Image paths in database**: Stored as relative paths like `/uploads/products/phone/iphone15.jpg`. The frontend uses them as-is in `:src` bindings — no path prefixing or transformation happens anywhere in the stack.
- **Local product images**: Product images in `uploads/products/{phone,laptop,men,women,food,electronics,clothes}/`. Banners in `uploads/banners/`. Real photos are stored as `.jpg`; placeholder SVGs share the same basename.

### Static resource troubleshooting

**"All images 404 on homepage"**: The most common cause is `StaticResourceConfig` failing to locate the `uploads/` directory. The gateway must be started from the project root (`java -jar ecommerce-gateway/target/...` from `web1/`), not from within the `ecommerce-gateway/` directory. The `StaticResourceConfig` logs the resolved path at startup — check the gateway console for `Serving /uploads/** from: ...`. Verify the path points to the actual `uploads/` directory.

**"Images broken on Windows but files exist"**: The `file:` URL format must use forward slashes and proper URI syntax (`file:///C:/path/to/uploads/`). Never construct `file:` URLs by string concatenation (`"file:" + absolutePath`) — on Windows this produces `file:C:\path\to\uploads` with backslashes, which is not a valid file URL. Always use `Path.toUri().toString()` which correctly produces `file:///C:/path/to/uploads/`.

**"Images work in dev but not production"**: Dev mode (Vite on :5200) proxies `/uploads` to `http://localhost:8080`. If the gateway isn't running, images 404 in dev too. For production (`frontend/dist/`), the browser loads images from the same origin (gateway on :8080), so Vite proxy isn't involved — the gateway must serve both the SPA and `/uploads/**`.

## Promotion / Seckill data flow

```
Home.vue                       PromotionController               Database
  │                              GET /api/promotions
  │  seckillPromotions =         ├─ Query e_promotion WHERE       e_promotion
  │    promotions.filter(         │  status=1 AND active          (no cover_image column)
  │      p => p.type==='SECKILL'  │
  │    )                          ├─ Collect productIds from      e_product
  │                              │  SECKILL promotions            (has cover_image)
  │  p.coverImage  ←───────────  ├─ Batch-select products
  │                              │  and set coverImage on each
  │                              │  promotion via @TableField(exist=false)
  │  goSeckill(p) → /product/:id └─ Return promotions with cover images
```

- **`Promotion` entity** has NO `cover_image` column in `e_promotion` table. It has a transient `coverImage` field marked `@TableField(exist = false)` that is populated at query time.
- **`PromotionController.active()`** batch-selects `Product` records for all SECKILL promotions and sets each promotion's `coverImage` from the linked product. If you add a new endpoint that returns promotions, you must replicate this pattern or the seckill section on the homepage will show broken images.
- **Frontend seckill display** (`Home.vue` line 87): `<img :src="p.coverImage || p.image || '/uploads/products/default.svg'">`. The first fallback `p.image` exists for backwards compatibility; the second fallback is a local SVG placeholder.
- **Seckill execution** is in `ecommerce-order`'s `SeckillController` (`POST /api/client/seckill/{promotionId}`) and uses Redis `DECR` for atomic inventory deduction.

## Database seed data

- `sql/init.sql` — Schema + admin user + categories only (no products/promotions).
- `sql/seed_data.sql` — 20 products across 5 categories, 3 banners, 2 discount + 3 seckill promotions. Uses local image paths (`/uploads/products/...`).
- `sql/seed_full.sql` — Same structure with charset fix (`utf8mb4`) and DELETE-before-INSERT semantics for re-seeding.
- `sql/fix_images.sql` — UPDATE script to fix existing database records: switches all `cover_image`/`images`/`image_url` fields from remote URLs to local paths.

To re-seed: `mysql -u root -p ecommerce < sql/seed_full.sql`

## MyBatis-Plus auto-fill pitfall

`MyMetaObjectHandler` in `ecommerce-common` uses `setFieldValByName` (NOT `strictInsertFill`/`strictUpdateFill`) for `createTime`/`updateTime`. The `strict*` variants throw `RuntimeException` when the field is already non-null — which happens when the frontend sends an entity with timestamps copied from a previous GET response. This caused a 500 error on banner save/update.

```java
// Correct — silently overwrites even if field is non-null
this.setFieldValByName("createTime", LocalDateTime.now(), metaObject);

// Wrong — throws if field is non-null (e.g. frontend sent it)
this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
```

## Admin form pattern (frontend)

When editing an entity in admin views (Banners, Announcements, etc.), the edit form receives the full entity from the table row. Always clear the form object before assigning new values — otherwise stale `createTime`/`updateTime`/`deleted` fields leak into the PUT request and can cause `HttpMessageNotReadableException` (Jackson can't deserialize `LocalDateTime` in the returned format).

```js
const clearForm = () => { Object.keys(form).forEach(k => delete form[k]); }
const editRow = (row) => {
  editing.value = true
  clearForm()
  Object.assign(form, { id: row.id, title: row.title, ... })
}
```
