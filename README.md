# Multi-channel inventory management (Spring Boot)

Modular monolith for central inventory, channel listings, order ingestion, reservations, and outbound sync to marketplaces. The database is the source of truth; channel adapters isolate external APIs.

## Stack

- Java 21, Spring Boot 3.3
- JPA (Hibernate), H2 in-memory for local dev (MySQL-compatible mode)
- **Flyway** for versioned schema (`src/main/resources/db/migration`)
- **Resilience4j** for per-channel HTTP retry and rate limiting
- `RestClient` for outbound calls (Shopify Admin REST patterns; Amazon/Flipkart configurable)

## Run locally

```bash
cd InventoryManagement
mvn spring-boot:run
```

Defaults: H2 + Flyway apply migrations on startup. JPA `ddl-auto` is `validate` so the schema is owned by Flyway.

Health: `GET http://localhost:8080/actuator/health`

## API (examples)

- `GET /api/inventory/sku/{skuId}`
- `POST /api/inventory/reserve` — body `{ "skuId": 1, "quantity": 2 }`
- `POST /api/orders` — body matches `OrderRequest` (external id, `channelId`, items with `skuId`)
- `GET /api/channels` — registered adapter names
- `POST /api/admin/poll-orders` — trigger order pull for all enabled channel configs
- `POST /api/admin/reconcile` — placeholder reconciliation hook

## Configuration

### Database

Local (default in `application.yml`):

- JDBC URL uses `MODE=MySQL` for closer parity with production MySQL.

For MySQL, point `spring.datasource.*` at your instance and keep Flyway enabled. The migration uses portable types (`VARCHAR` for former JSON columns) so you can switch to native `JSON` in a follow-up migration if desired.

### Channel HTTP integrations (`app.channels.*`)

All adapters are **opt-in** via `enabled: true`. Without credentials they return no orders and skip outbound calls (safe for local dev).

| Property prefix | Purpose |
|-----------------|--------|
| `app.channels.shopify.*` | Shopify Admin API (`X-Shopify-Access-Token`, shop host, API version, optional `location-id` for inventory levels) |
| `app.channels.amazon.*` | Configurable base URL, bearer token (e.g. LWA access token in sandbox), and paths — **production SP-API often requires AWS SigV4 signing**; extend with the AWS SDK or a signing filter |
| `app.channels.flipkart.*` | Configurable base URL, client id/secret or static token, and order/inventory paths |

Environment variables are supported in `application.yml` (e.g. `SHOPIFY_SHOP`, `SHOPIFY_ACCESS_TOKEN`).

### Resilience (`resilience4j.*`)

Retry and rate limiters are registered per channel key: `shopify`, `amazon`, `flipkart`. Tune `maxAttempts`, `waitDuration`, and `limitForPeriod` in `application.yml`.

## Schema

Flyway `V1__initial_schema.sql` matches the logical model: product, SKU, inventory, reservations, channels, listings, orders, order lines, sync log. `V2__seed_channels.sql` inserts default channel rows aligned with adapter names.

`attributes`, `config`, and `payload` are stored as `VARCHAR` for portability between H2 (dev) and MySQL (prod). You can introduce a follow-up migration to use native `JSON` on MySQL only if you want strict JSON types.

Spring Boot 3.3 manages **Flyway 10.10.x**; H2 is supported via `flyway-core` on the classpath (no extra database module required for local runs).

## Architecture notes

- **Channel orders**: adapters return `ChannelOrderSnapshot` (no internal `channelId`); `OrderSyncService` attaches the DB channel id before validation and persistence.
- **Idempotent orders**: unique `(external_order_id, channel_id)`.
- **Stock**: pessimistic lock on inventory row during reservation; sellable = `available - reserved - safety`.
- **Sync**: inventory changes publish events; async listener pushes quantities to listings via adapters; `sync_log` records outcomes.
- **Schedulers**: order polling, periodic inventory push, reconciliation stub, failed sync retry counter bump (extend with real replay).

## Build

```bash
mvn -DskipTests package
```

## License

Example project — use and modify as needed.
