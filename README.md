# VENDORA – multivendor marketplace (Spring Boot + MySQL + JWT)
Needs Java 17+, Maven 3.9+, MySQL 8+.

## Run
1. Copy `.env.example` values into your environment (or IDE run configuration). `JWT_SECRET` (32+ characters) is required; the app will not start without it.
2. Set `SEED_PASSWORD` to create usable development accounts: `admin@vendora.local`, `customer@vendora.local`,
   and one approved seller per store such as `urbansole@vendora.local`, all with that password. Without it the accounts are created disabled.
3. `mvn spring-boot:run`, then open http://localhost:8080. API docs: http://localhost:8080/swagger-ui.html (use Authorize with the login token).
4. `mvn test` runs the tests on an in-memory H2 database (no MySQL needed).

## What works (implemented in code; see tests)
- Registration, BCrypt hashing, JWT login (2h expiry), roles ADMIN / SELLER / CUSTOMER enforced on the server; role and enabled flag are read from the database on every request.
- Seller registration creates a PENDING store; sellers cannot use `/api/seller/**` until an admin approves (`POST /api/admin/sellers/{id}/approve|reject|suspend`, audit-logged).
- Sellers: create, edit and soft-delete only their own products; see only their own orders and summary (revenue, commission, payout, low stock); advance order status PLACED, PACKED, SHIPPED, DELIVERED.
- Customers: place orders (identity from the token, prices from the database), list own orders, cancel before packing (stock restored).
- Checkout is transactional, locks product rows, splits one order per seller and computes the 10% commission on the server.
- Public catalogue: `GET /api/products?q=&category=&page=&size=&sort=` (total in `X-Total-Count`), `/api/products/{id}`, `/api/categories`.
- Validation errors and failures return one JSON shape: `{status, message, details}`.

## Not built yet
Server-side cart and wishlist, addresses, taxes/shipping/coupons, reviews, payments (Razorpay), refunds, payouts, shipments, notifications and email, analytics endpoints,
seller and admin dashboard screens (use Swagger for now), product image upload, Flyway migrations (schema comes from Hibernate `ddl-auto=update`), foreign-key constraints between tables.
Log-out is client-side (token discarded); there is no server-side token revocation.
The token is kept in `sessionStorage` by the page, which is readable by any script on the page; keep the page free of third-party scripts.

## Status
This code has NOT been compiled or run by its author (no Maven or MySQL was available). Run `mvn test` first and send back any error.
