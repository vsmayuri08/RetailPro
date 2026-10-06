# RetailPro — Multi-Branch Retail Management System

Spring Boot + React app for running several retail branches: catalog, FIFO inventory, POS billing, returns, loyalty, payroll, and manager analytics.

Stack: **Java 21**, **Spring Boot 3.4**, **MySQL**, **JWT**, **React 18**, **Vite**.

---

## Run locally

MySQL must be running. Database name and credentials are in `backend/src/main/resources/application.properties` (`retail_management` is created automatically if missing).

```bash
# Backend — http://localhost:8080
cd backend
mvn spring-boot:run
```

```bash
# Frontend — http://localhost:5173 (proxies /api to 8080)
cd frontend
npm install
npm run dev
```

Demo data seeds when `app.seed.enabled=true` (default). Each seed record is inserted only if it is missing, so restarts are safe.

---

## Demo accounts

| Role | Email | Password |
|------|--------|----------|
| Super Admin | `admin@retailsystem.com` | `Admin@123` |
| Branch Manager (Downtown) | `manager.downtown@retailsystem.com` | `Manager@123` |
| Branch Manager (Riverside) | `manager.riverside@retailsystem.com` | `Manager@123` |
| Cashier (Downtown) | `cashier.downtown@retailsystem.com` | `Cashier@123` |

Seeded extras: two branches (DT01 / RS02), categories, suppliers, customers at different loyalty tiers, staff/payroll rows, and products with opening FIFO batches. Seeded barcodes equal the SKU (e.g. `GRO-RICE-5KG`) so POS search and webcam scan both work.

No sales are seeded. Run a checkout as the downtown cashier at `/cashier/sale` before reports, bills, ABC, RFM, or reorder suggestions have real numbers.

---

## Roles

### Super Admin
Branches, users (managers), categories, suppliers, staff & payroll (any branch), billing settings (tax + loyalty earn rate), expiry-discount ladder, org reports, **cashier leaderboard**, **activity log**, **RetailPro AI** (org facts from live branch sales).

### Branch Manager
Own-branch products and inventory (receive stock, batch history, transfers), customers, cashier login accounts, staff & payroll (cashiers / inventory staff only), sales snapshot, and analytics:

| Page | What it uses |
|------|----------------|
| ABC Analysis | Real sale revenue, last 90 days by default |
| Market Basket | Products bought together on the same invoice |
| RFM Segmentation | Recency / frequency / monetary from customer purchases |
| Demand Forecast | Average daily units vs current stock |
| Sales Trend Prediction | Dashboard chart: last 6 months of invoice revenue + next-month forecast (linear trend or daily run rate) |
| Reorder Suggestions | Sales velocity + purchase-batch history (not the catalog reorder level) |
| Purchase Orders | Create drafts from reorder suggestions; manager approves; receive adds inventory. Never sent to suppliers |
| Cashier Leaderboard | This month’s revenue by `Sale.processedBy` |
| Activity Log | Branch-scoped audit of logins, sales, returns, stock receipts |
| AI Inventory Insights | Dashboard table: stock, avg daily sales, predicted demand, days remaining — numbers from Java, Gemini only narrates |
| RetailPro AI | Floating chat (managers + super admin). `POST /api/ai/ask` sends a live FACTS snapshot; the model must not invent figures |

### Cashier
POS (`/cashier/sale`), bills, returns, leaderboard.

---

## POS, receipts, and inventory

**Checkout** (`SaleService.checkout`) is one `@Transactional` method: validate lines (branch, active, not expired, enough stock), price with live expiry discounts, tax from billing settings, invoice number, FIFO batch deduction, payment, loyalty credit. Inventory quantity is only written through `InventoryBatchService`. Loyalty only through `CustomerService.adjustLoyaltyPoints()`.

**Webcam barcode scan** — New Sale → Scan barcode. Looks up exact `Product.barcode`, then SKU.

**QR digital receipt** — After sale (and on Bills → View) a QR encodes `/receipt/{invoice}?t={hmac}`. Customers open it without logging in. Guessing an invoice number is not enough; the token is HMAC-SHA256 of the invoice using the JWT secret.

**PDF invoice** — Download from the on-screen receipt or the public digital-receipt page (`GET /api/cashier/sales/{id}/invoice.pdf` or `/api/public/receipts/{invoice}/invoice.pdf?t=`).

**Returns** — One transactional method: refund share of original total, restock via `RETURN-` batches, reverse loyalty (capped at current balance). Fully returned bills stay on the invoice; they are not rewritten.

**FIFO** — `InventoryBatch` is the source of truth. `Product.quantity` / `expiryDate` are a synced cache. Receive Stock, transfers, sales, and returns all go through `InventoryBatchService`.

---

## Analytics (read-only)

All manager reports live under `/api/manager/reports/**` (existing `/api/manager/**` security). They do not change `Sale` checkout.

- **ABC** — Rank by revenue; A ≈ first 80%, B ≈ to 95%, C = long tail including zero-sales items.
- **Reorder suggestions** — Daily demand = units sold ÷ lookback days. Lead time = average gap between supplier receipts (14 days if fewer than two). Order qty = ceil(daily × (lead + safety)) − stock. Suggested supplier = highest volume on batches that have a supplier (opening/transfer/return batches are ignored).
- **Demand forecast / market basket / RFM** — Same lookback style; empty data returns a message, not an error.
- **Sales trend** — `GET /api/manager/reports/sales-trend`. Six calendar months of `Sale.totalAmount`, then next-month revenue from linear regression (or current-month run rate if history is short).
- **Purchase orders** — `POST /api/manager/purchase-orders/from-reorder` builds one draft PO per supplier. Statuses: Draft → Pending → Approved → Received (or Cancelled). Approve is required; nothing is sent to a supplier. Receive writes `InventoryBatch` rows.

---

## Security

- JWT in `Authorization: Bearer …`
- `/api/auth/login` and `/api/public/receipts/**` are public
- `/api/admin/**` — Super Admin
- `/api/manager/**` — Super Admin or Branch Manager (branch always from the JWT)
- `/api/cashier/**` — Super Admin, Branch Manager, or Cashier

**Activity log** (`activity_logs`) records LOGIN, SALE_COMPLETED, SALE_RETURNED, PRODUCT_CREATED, STOCK_RECEIVED. Super Admin sees the last 200 org-wide; a manager sees their branch.

---

## Useful API routes

| Method | Path | Notes |
|--------|------|--------|
| POST | `/api/auth/login` | JWT |
| POST | `/api/cashier/sales` | Checkout |
| GET | `/api/cashier/products/lookup?code=` | Exact barcode/SKU |
| GET | `/api/cashier/sales/{id}/invoice.pdf` | Auth PDF |
| GET | `/api/public/receipts/{invoice}?t=` | Digital receipt |
| GET | `/api/manager/reports/abc-analysis` | Optional `startDate` / `endDate` |
| GET | `/api/manager/reports/reorder-suggestions` | Velocity + supplier |
| GET | `/api/manager/reports/sales-trend` | History + next-month revenue |
| POST | `/api/manager/purchase-orders/from-reorder` | Draft POs from reorder suggestions |
| GET | `/api/admin/activity-logs` | Audit |

---

## Project layout

```
retail-project/
  backend/     Spring Boot API (port 8080)
  frontend/    Vite + React (port 5173)
```

Theme tokens live in `frontend/src/styles/global.css` (green / white). Login is a three-column Welcome Back layout.

---

## Known limits

- `User` (login) and `Employee` (HR) are separate; `Employee.linkedUser` is unused in the UI
- Employee type cannot be changed after create (JPA inheritance)
- Payments are one method per sale (no split tender)
- Invoice sequence has a narrow race under concurrent checkouts on the same branch/day
- Stock transfer is one product per request
- No pagination / OpenAPI docs
- Milk/oil/pizza demo SKUs may be expired or missing FIFO batches after heavy testing; receive stock on Inventory if checkout says batches do not have enough

---

## Typical demo path

1. Cashier: New Sale → add Basmati Rice (or scan `GRO-RICE-5KG`) → complete sale.
2. Bills → View → scan the QR (or Download PDF).
3. Manager: ABC Analysis, Reorder Suggestions, Cashier Leaderboard.
4. Super Admin: Activity Log (logins and the sale).
