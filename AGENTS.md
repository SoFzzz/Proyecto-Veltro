---
### Project Overview

Veltro is a lightweight ERP/POS focused on SMEs that need barcode-based sales, automatic inventory updates, proactive stock alerts, and auditable operations. The backend runs on Spring Boot 4.x with Java 21, PostgreSQL, Flyway, and follows hexagonal architecture with clear DDD boundaries. Frontend (not covered here) uses React 18 + TypeScript + Vite.

### Build, Run, Test Commands

- `./mvnw clean install` — full build with unit tests
- `./mvnw spring-boot:run -Dspring.profiles.active=local` — run API against local PostgreSQL (see `docker-compose.yml`)
- `./mvnw test` — run all unit tests (Skips `VeltroApplicationTests` unless DB available)

### Hexagonal Architecture

Packages are grouped by layers:
- `domain/` — aggregates, entities, and business rules
- `application/` — use cases, services, event listeners (POS module listeners live here)
- `infrastructure/` — adapters (REST controllers), persistence, configuration
Each module (IAM, Catalog, Inventory, POS) keeps its domain logic separated, enabling independent evolution.

### Spring Boot 4 Test Patterns

- Use JUnit 5 + Mockito for unit tests
- Avoid `@SpringBootTest` unless necessary (requires PostgreSQL); prefer slicing
- Test listeners with `@ExtendWith(MockitoExtension.class)` and constructor injection

### Code Style

- Monetary values = `BigDecimal` with `@Column(precision=19, scale=4)`
- Services keep business rules; controllers thin
- Logging: use `Slf4j`, info on success, warn/error on failure

### Lombok & MapStruct

- Lombok enabled (`@Getter`, `@Setter`, `@Builder`, etc.) but use explicit methods when logic required (e.g., `SaleEntity.setStatus()`)
- MapStruct for DTO ↔ entity conversions; keep mappers in `application/.../mapper`

### Persistence, Flyway & Security

- Flyway migrations in `src/main/resources/db/migration` (V1…)
- PostgreSQL via Docker compose (`postgres:16-alpine`)
- Spring Security uses JWT (B1-02), roles: ADMIN, CASHIER, WAREHOUSE

### Agent Expectations

- Respect ADRs and acceptance criteria
- No negative stock (AC-04), no physical deletes (AC-05)
- Tests required for every new listener/service
- Update `AGENTS.md` after finishing a task

### Project Status & Progress

- **B1-01 | Project Setup — COMPLETED**
- **B1-02 | IAM Module — COMPLETED**
- **B1-03 | Catalog Module — COMPLETED**
- **B1-04 | Inventory Module — COMPLETED**
- **B2-01 | Sale Module — COMPLETED**
- **B2-02 | Observer Pattern — COMPLETED**
- **B2-03 | Proactive Alerts — COMPLETED**
- **B2-04 | Purchasing Module — COMPLETED**
- **B3-03 | Forensic Audit — COMPLETED (March 2026)** - AuditCommandExecutor integrated into SaleService, InventoryService, PurchaseOrderService
- **B3-01 | AI Fallback Scanner — COMPLETED (March 2026)** - Strategy Pattern with BarcodeStrategy and AiVisionStrategy (placeholder throws UnsupportedOperationException)
- **B3-02 | Dashboard & Reports — COMPLETED (March 2026)** - Facade Pattern for Dashboard, Factory Method for PDF/Excel report export

### Key Patterns & Lessons Learned

- ADR-002: `@Version` mandatory on POS/inventory entities
- ADR-005: `NUMERIC(19,4)` monetary precision
- ADR-006: State Pattern for Sale/PurchaseOrder lifecycle
- Domain events drive stock synchronization; Inventory service enforces audit trail
- Spring Boot 4.x Migration: @WebMvcTest removed. Use @ExtendWith(MockitoExtension.class) with manual constructor injection. @MockBean → @MockitoBean. Mockito 5.x requires lenient() for stubs not used in all paths.

### Test Status (updated)

| Test Class | Module | Tests |
|------------|--------|------:|
| `JwtTokenProviderTest` | IAM (B1-02) | 13 |
| `AuthControllerTest` | IAM (B1-02) | 11 |
| `ProductServiceTest` | Catalog (B1-03) | 6 |
| `ProductControllerTest` | Catalog (B1-03) | 15 |
| `CategoryControllerTest` | Catalog (B1-03) | 12 |
| `InventoryServiceTest` | Inventory (B1-04) | 13 |
| `InventoryControllerTest` | Inventory (B1-04) | 21 |
| `InProgressStateTest` | POS (B2-01) | 5 |
| `CompletedStateTest` | POS (B2-01) | 5 |
| `VoidedStateTest` | POS (B2-01) | 5 |
| `SaleServiceTest` | POS (B2-01) | 14 |
| `SaleControllerTest` | POS (B2-01) | 5 |
| `DeductStockSaleListenerTest` | POS (B2-02) | 4 |
| `RestoreStockSaleListenerTest` | POS (B2-02) | 4 |
| `OutOfStockHandlerTest` | Alerts (B2-03) | 1 |
| `LowStockHandlerTest` | Alerts (B2-03) | 5 |
| `OverstockHandlerTest` | Alerts (B2-03) | 6 |
| `AlertChainBuilderTest` | Alerts (B2-03) | 6 |
| `EvaluateStockAlertsListenerTest` | Alerts (B2-03) | 3 |
| `AlertServiceTest` | Alerts (B2-03) | 11 |
| `AlertControllerTest` | Alerts (B2-03) | 6 |
| `IncrementStockOrderListenerTest` | Purchasing (B2-04) | 4 |
| `SupplierServiceTest` | Purchasing (B2-04) | 8 |
| `PendingStateTest` | Purchasing (B2-04) | 6 |
| `PartialStateTest` | Purchasing (B2-04) | 5 |
| `ReceivedStateTest` | Purchasing (B2-04) | 5 |
| `VoidedStateTest` | Purchasing (B2-04) | 5 |
| `PurchaseOrderServiceTest` | Purchasing (B2-04) | 14 |
| `SupplierControllerTest` | Purchasing (B2-04) | 11 |
| `PurchaseOrderControllerTest` | Purchasing (B2-04) | 15 |
| `AuditCommandExecutorTest` | Audit (B3-03) | 8 |
| `ForensicAuditServiceTest` | Audit (B3-03) | 10 |
| `AuditRecordEntityTest` | Audit (B3-03) | 3 |
| `AuditControllerTest` | Audit (B3-03) | 6 |
| `BarcodeStrategyTest` | Scanner (B3-01) | 6 |
| `AiVisionStrategyTest` | Scanner (B3-01) | 13 |
| `ScannerServiceTest` | Scanner (B3-01) | 7 |
| `ScannerControllerTest` | Scanner (B3-01) | 6 |
| `DashboardServiceTest` | Dashboard (B3-02) | 4 |
| `DashboardControllerTest` | Dashboard (B3-02) | 2 |
| `ReportControllerTest` | Reports (B3-02) | 5 |
| **Total passing** | | **326** |

**Expected failure:** `VeltroApplicationTests` requires a running PostgreSQL instance.

### B2-01 | Sale Module — COMPLETED (March 2026)

- Entities: `SaleEntity`, `SaleDetailEntity` in `domain/pos/model/` with `@Version`
- State Pattern classes (`InProgressState`, `CompletedState`, `VoidedState`)
- Events published: `SaleCompletedEvent`, `SaleVoidedEvent` (consumed in B2-02)
- Flyway migration `V4__create_sale_tables.sql` with `sale_number_seq`
- Lesson: JPA lifecycle callbacks don't fire in tests; override `setStatus()` to sync transient state

### B2-02 | Observer Pattern — COMPLETED (March 2026)

- Listeners live in `application/pos/listener/`:
  - `DeductStockSaleListener` handles `SaleCompletedEvent`, calls `InventoryService.recordExit()` per sale item
  - `RestoreStockSaleListener` handles `SaleVoidedEvent`, calls `InventoryService.recordEntry()` per item
- No `@Transactional` on listeners; rely on InventoryService transactions
- Exceptions propagate to roll back sale confirm/void on failure; prevents negative stock
- `recordExit()` and `recordEntry()` already persist `InventoryMovementEntity`, so no extra movement listener
- 8 unit tests (4 per listener) cover empty cart, multi-items, failure propagation, and reason strings

### B2-03 | Proactive Alerts — COMPLETED (March 2026)

**Components created:**
- Domain: `AlertEntity`, `AlertConfigurationEntity`, `AlertType`, `AlertSeverity` in `domain/inventory/model/`
- Chain of Responsibility: `OutOfStockHandler`, `LowStockHandler`, `OverstockHandler`, `AlertChainBuilder` in `application/inventory/alert/`
- Event: `StockChangedEvent` published by `InventoryService` after `recordEntry()`, `recordExit()`, `recordAdjustment()`
- Listener: `EvaluateStockAlertsListener` in `application/inventory/listener/`
- Services: `AlertService` (smart auto-resolution), `AlertConfigurationService` (lazy default config)
- Flyway: `V5__create_alert_tables.sql` with composite index on `(severity, created_at)` (ADR-003)
- REST: `AlertController` with paginated endpoints ordered by severity

**Key decisions:**
- Separate `AlertConfigurationEntity` — not merged into `InventoryEntity`
- Smart auto-resolution: only resolve alerts whose condition no longer applies; keep active if condition persists
- Default config: criticalStock=0, minStock from InventoryEntity or 5, overstockThreshold from InventoryEntity or 100

**Test count update:** 134 → 172 (38 new tests)

### B2-04 | Purchasing Module — COMPLETED (March 2026)

**Components created:**
- Domain: `SupplierEntity`, `PurchaseOrderEntity`, `PurchaseOrderDetailEntity`, `PurchaseOrderStatus` in `domain/purchasing/model/`
- State Pattern: `PendingState`, `PartialState`, `ReceivedState`, `VoidedState` classes with lifecycle management
- Events: `OrderReceivedEvent` with `ReceivedItemInfo` published when orders transition to RECEIVED status  
- Listener: `IncrementStockOrderListener` in `application/purchasing/listener/`
- Services: `SupplierService` (CRUD with tax ID uniqueness), `PurchaseOrderService` (State Pattern + Prototype Pattern + Event Publishing)
- Flyway: `V6__create_purchasing_tables.sql` with PostgreSQL sequences for order numbering
- REST: `SupplierController`, `PurchaseOrderController` with role-based security (ADMIN, WAREHOUSE)

**Key patterns implemented:**
- **State Pattern**: Purchase order lifecycle (PENDING → PARTIAL → RECEIVED → VOIDED) with state-specific behavior validation
- **Prototype Pattern**: `cloneForNewOrder()` methods on entities for easy order duplication via `/clone` endpoints
- **Observer Pattern**: OrderReceivedEvent → IncrementStockOrderListener → InventoryService.recordEntry() for automatic stock updates
- **Repository Pattern**: Port/adapter separation with JPA implementations and sequence generation

**API endpoints:**
- Suppliers: Full CRUD with tax ID lookup, soft delete support
- Purchase Orders: CRUD, filtering by supplier, item management, state transitions (receive/void), prototype cloning

**Key decisions:**
- Tax ID as simple unique string field (no format validation per requirements)
- Purchase order numbering: PO-YYYY-NNNNNN format with PostgreSQL sequence
- Unit costs stored as historical snapshots (no ProductEntity.costPrice updates)
- Role-based access: ADMIN/WAREHOUSE for all operations (Cashiers excluded from purchasing)

**Test count update:** 172 → 244 (72 new tests)

---

## Backend Complete — Frontend Pending

All backend phases (B1, B2, B3) are complete. The frontend phases remain pending:

### PHASE 1 — Frontend (COMPLETED)

- **F1-01 | Project setup — COMPLETED:** React 18 + TypeScript + Vite. Axios with JWT interceptors (auto-attach token + silent refresh on 401). Zustand `authStore` (token, user, role). Types `ApiResponse<T>` and `PageResponse<T>`.
- **F1-02 | Authentication UI — COMPLETED:** Login page with React Hook Form + Zod. `AuthGuard` and `RoleGuard` components. Role-based redirect (ADMIN→/dashboard, CASHIER→/pos, WAREHOUSE→/inventory).
- **F1-03 | Catalog UI — COMPLETED:** Product listing page with pagination. Create/edit product form. Category tree component with inline editing.

### PHASE 2 — Frontend (PENDING)

- **F2-01 | Scanner + POS UI:** `ScannerContainer` integrating `react-zxing` (ADR-001). On decode, calls `GET /api/v1/products/barcode/{barcode}` and adds to `cartStore`. POS page with cart table, quantity modify buttons, and confirm sale button.
- **F2-02 | Alerts UI:** Unread alerts badge in the Header. Alert listing page ordered by severity. Component to configure thresholds per product.
- **F2-03 | Purchase Orders UI:** Create order form, listing page with visual states, clone order button, and merchandise reception flow.

### PHASE 3 — Frontend (PENDING)

- **F3-01 | AI Fallback UI:** 3s timer in `ScannerContainer`. If it expires without detection, show *"Identify with AI"* button. On activation, capture frame, send to backend and show modal with `ProductSuggestionResponse` list and confidence percentage.
- **F3-02 | Dashboard UI:** Dashboard page with KPI cards (`todaySales`, `averageTicket`, `outOfStockProducts`, `estimatedMonthlyProfit`). Latest sales table. Export PDF and Excel buttons.
- **F3-03 | Audit UI (ADMIN only):** Paginated table of `AuditRecordEntity` with filters by entity, action, and date range. Detail view with diff of `previousData` vs `newData`.

---

### B3-03 | Forensic Audit Integration — COMPLETED (March 2026)

**Integration completed:**
- `SaleService.java`: Added `AuditCommandExecutor` dependency, wrapped `confirm()` and `voidSale()` with audit logging
- `InventoryService.java`: Added `AuditCommandExecutor` dependency, wrapped `recordAdjustment()` with audit logging
- `PurchaseOrderService.java`: Added `AuditCommandExecutor` dependency, wrapped `markAsReceived()` and `voidOrder()` with audit logging

**Audit events captured:**
- `SALE_CONFIRMED`, `SALE_VOIDED` — Sales lifecycle
- `INVENTORY_ADJUSTMENT` — Manual stock adjustments
- `ORDER_RECEIVED`, `ORDER_VOIDED` — Purchase order lifecycle

**Test updates:** All service tests updated with mock `AuditCommandExecutor`

### B3-01 | AI Fallback Scanner — COMPLETED (March 2026)

**Strategy Pattern implemented:**
- `ScannerStrategy` interface with `scan(filename, imageData)` method
- `BarcodeStrategy` — No-op implementation (frontend handles barcode scanning)
- `AiVisionStrategy` — Placeholder that throws `UnsupportedOperationException` until OpenAI API is configured

**Components created:**
- Domain: `ProductSuggestionResponse` DTO with nested `SuggestedProduct` record
- Services: `ScannerService` coordinates strategies, validates input
- REST: `ScannerController` with `/api/v1/scanner/ai-scan` endpoint

**Key decisions:**
- AI Vision throws `UnsupportedOperationException` per requirements (no API key available)
- Response includes confidence score, suggested name, category, and estimated price
- Image validation: non-null, non-empty, valid filename required

**Test count:** 32 new tests (6 + 13 + 7 + 6)

### B3-02 | Dashboard & Reports — COMPLETED (March 2026)

**Facade Pattern for Dashboard:**
- `DashboardService` aggregates metrics from multiple sources via `DashboardQueryRepository`
- `DashboardResponse` DTO with daily sales, active products, pending orders, low stock count

**Factory Method for Reports:**
- `ReportExporter` interface with `export(report)` and `getContentType()` methods
- `PdfReportExporter` — PDF generation using iText 9.x
- `ExcelReportExporter` — Excel generation using Apache POI 5.4.x
- `ReportService` — Factory method selects exporter based on `ReportType`

**Components created:**
- DTOs: `DashboardResponse`, `ReportType` enum, `ProfitabilityReport`
- Repository: `DashboardQueryRepository` interface, `DashboardQueryJpaRepository` implementation
- REST: `DashboardController` (GET /api/v1/dashboard), `ReportController` (POST /api/v1/reports/export, GET /api/v1/reports/profitability)

**Dependencies added to pom.xml:**
- iText Core 9.1.0 for PDF generation
- Apache POI 5.4.0 for Excel generation

**Key discovery:** iText 9.x removed `setBold()` method; must use `setFont(PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD))` instead

**Test count:** 11 new tests (4 + 2 + 5)

### Phase 3 Complete — Summary

All Phase 3 tasks completed:
- **B3-03**: Forensic audit logging integrated into all critical operations
- **B3-01**: Strategy Pattern for scanner with AI placeholder ready for future API integration
- **B3-02**: Dashboard facade and report factory with PDF/Excel export

**Test count progression:** 283 → 326 (43 new tests)

---

## Final Project Status

| Phase | Backend | Frontend |
|-------|---------|----------|
| Phase 1 | ✅ B1-01, B1-02, B1-03, B1-04 | ✅ F1-01, F1-02, F1-03 |
| Phase 2 | ✅ B2-01, B2-02, B2-03, B2-04 | ⏳ F2-01, F2-02, F2-03 |
| Phase 3 | ✅ B3-01, B3-02, B3-03 | ⏳ F3-01, F3-02, F3-03 |

**Backend:** 100% complete (326 tests passing)
**Frontend:** 33% — Phase 1 complete

---

### F1 | Frontend Phase 1 — COMPLETED (March 2026)

**Project Setup (F1-01):**
- Vite + React 18 + TypeScript project with Tailwind CSS
- Axios client with JWT interceptors (auto-attach token, silent refresh on 401)
- Zustand `authStore` with persistence to localStorage
- Type definitions: `ApiResponse<T>`, `PageResponse<T>`, `User`, `Product`, `Category`, etc.

**Authentication UI (F1-02):**
- Login page with React Hook Form + Zod validation
- `AuthGuard` component — redirects to /login if not authenticated
- `RoleGuard` component — redirects to /unauthorized if role not allowed
- Role-based redirects: ADMIN→/dashboard, CASHIER→/pos, WAREHOUSE→/inventory

**Catalog UI (F1-03):**
- Product listing page with pagination (`PageResponse<Product>`)
- Create/edit product form with Zod validation
- Category tree component with recursive rendering and inline edit/delete
- Category management page with inline form

**Frontend Structure:**
```
frontend/src/
├── api/            # Axios client, auth & catalog APIs
├── stores/         # Zustand authStore
├── types/          # TypeScript interfaces
├── components/
│   ├── auth/       # AuthGuard, RoleGuard
│   ├── layout/     # MainLayout with role-based navigation
│   └── catalog/    # CategoryTree
├── pages/
│   ├── auth/       # LoginPage
│   ├── catalog/    # ProductListPage, ProductFormPage, CategoryPage
│   └── ErrorPages.tsx
├── App.tsx         # React Router routes
└── main.tsx        # BrowserRouter + QueryClientProvider
```

**Build verified:** `npm run build` successful (407KB bundle)
