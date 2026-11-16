# Frontend automated tests (optional assessment extra)

**Stack:** React + Vite + Vitest 4.1+ + React Testing Library + jsdom + V8 coverage.

## Purpose and scope

These tests validate the existing React application against mocked Axios responses and actual React contexts. They do not need a running backend or PostgreSQL, and they do not assert the full cross-service integration (which is evidenced separately by the completed purchase from React into PostgreSQL).

| Test file | Scenarios |
|---|---|
| `src/test/auth.test.jsx` | Login, registration, invalid credentials, client-side registration validation |
| `src/test/catalog.test.jsx` | Product loading, filter query parameters, empty results, API failures |
| `src/test/cart.test.jsx` | Adding products, quantity/removal, checkout payload and navigation, checkout failure |
| `src/test/navigation.test.jsx` | Product detail, protected order-history route, visible order history, logout |

## Run

From the `frontend` directory:

```bash
npm ci
npm run test
npm run test:coverage
npm run lint
npm run build
```

In a fresh clone, `npm ci` works after the updated `package.json` and `package-lock.json` have been committed. For the first installation, follow `frontend/src/test/README.md`.

## Reports

- Terminal: Vitest reports passing and failing test cases.
- Frontend HTML: `frontend/coverage/index.html` from `npm run test:coverage`.
- Backend HTML: `backend/target/site/jacoco/index.html` from `./mvnw clean verify` (requires Docker for Testcontainers).

The frontend report is separate from the **backend mandatory 80% JaCoCo line-coverage gate**. Do not combine their percentages.

## Boundaries

- Axios calls are mocked, so frontend unit tests do not contact the real API.
- Checkout integration with Java/PostgreSQL was separately verified with a real order and inventory reduction.
- Passwords and JWT secrets are not stored in these tests: sample `unit-test-token` is a fake string.
- Frontend test count, pass status and coverage percentages must be copied from an actual local test execution before final submission.
