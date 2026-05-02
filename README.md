# Multi-channel inventory management (Spring Boot + Expo)

Modular monolith for central inventory, channel listings, order ingestion, reservations, and outbound sync to marketplaces. The database is the source of truth; channel adapters isolate external APIs.

## Stack

- Java 21, Spring Boot 3.3
- JPA (Hibernate), H2 in-memory for local dev (MySQL-compatible mode)
- **Flyway** for versioned schema (`src/main/resources/db/migration`)
- **Resilience4j** for per-channel HTTP retry and rate limiting
- `RestClient` for outbound calls (Shopify Admin REST patterns; Amazon/Flipkart configurable)
- React Native + Expo (TypeScript, expo-router) in `mobile/`

## Backend functionality

- Central inventory model with products, SKUs, inventory rows, reservations, listings, and orders
- Order ingestion and idempotent persistence from channels or manual order creation
- Inventory reservation with pessimistic locking and sellable quantity calculation
- Channel adapter registry for Shopify, Amazon, and Flipkart integrations
- Per-channel order polling and adapter status discovery
- Async inventory sync events to push stock updates to enabled channel listings
- Administrative sync orchestration and reconciliation hooks
- Health check and actuator support via `GET /actuator/health`
- CORS enabled for `/api/**` and `/actuator/**` to support the mobile/web client

## Run locally

```bash
cd Inventory-Manager
mvn spring-boot:run
```

Defaults: H2 + Flyway apply migrations on startup. JPA `ddl-auto` is `validate` so the schema is owned by Flyway.

Health: `GET http://localhost:8080/actuator/health`

## Mobile app (Expo)

The repository now includes a React Native frontend under `mobile/` that talks to the same backend APIs.

### Mobile features

- Dashboard: backend health, per-channel status, per-channel/all-channel order polling, and reconcile action
- Inventory lookup: fetch stock by SKU
- Reserve stock: submit `POST /api/inventory/reserve`
- Order creation: submit `POST /api/orders` with line items
- Configurable API base URL saved in local storage

### Run mobile app

```bash
cd mobile
npm install
npm start
```

Then press:

- `w` for web
- `a` for Android emulator
- or scan the QR code in Expo Go on your phone

### API base URL tips

- Desktop browser / iOS simulator: `http://localhost:8080`
- Android emulator: `http://10.0.2.2:8080`
- Physical device: `http://<your-lan-ip>:8080` (same Wi-Fi network)

You can set the base URL in the Home tab. `EXPO_PUBLIC_API_URL` is also supported for defaults.

## API (examples)

- `GET /api/inventory/sku/{skuId}`
- `POST /api/inventory/reserve` — body `{ "skuId": 1, "quantity": 2 }`
- `POST /api/orders` — body matches `OrderRequest` (external id, `channelId`, items with `skuId`)
- `GET /api/channels` — registered adapter names
- `GET /api/channels/status` — channel rows + adapter/config status
- `POST /api/channels/{channelName}/poll-orders` — poll one channel and return fetched count/result
- `POST /api/channels/poll-orders` — poll all channels and return per-channel results
- `POST /api/admin/poll-orders` — trigger order pull for all enabled channel configs
- `POST /api/admin/reconcile` — placeholder reconciliation hook

## Demo flow

Use this flow to verify end-to-end behavior:

1. Start backend:

```bash
mvn spring-boot:run
```

2. Start mobile app:

```bash
cd mobile
npm start
```

3. In Home tab:
   - Set API URL (`localhost`, `10.0.2.2`, or LAN IP depending on device)
   - Confirm health = `UP`
   - Reload channels and verify each channel's `enabled/configured` status

4. Pull channel orders:
   - Use **Poll this channel** for targeted sync, or
   - Use **Poll all channels** for full sync
   - Inspect per-channel results (`SUCCESS/FAILED`, fetched order count)

5. Verify inventory + reservation flow:
   - Create an order in the Orders tab (uses `POST /api/orders`)
   - Check stock for a SKU in the Stock tab
   - Reserve additional stock in Reserve tab, then re-check stock

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

### CORS

`WebConfig` registers CORS for `/api/**` and `/actuator/**` so Expo web and browser-based clients can call the backend during development.

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
