# Final submission checklist

**Blossom — Full Stack Java + React assessment**

Use this list before sending the final email; checking an item should correspond to an actual verification, not an assumption.

## Required functionality

- [x] Backend Java + Spring Boot with PostgreSQL and JPA relationships.
- [x] `User`, `Product`, `Order` plus `OrderItem` for order line details.
- [x] Entity inheritance via `BaseEntity`.
- [x] Product CRUD and filters by name, category, price.
- [x] Registration, login and JWT authorization.
- [x] Order creation and user-scoped order history.
- [x] React login/registration, catalog, detail, cart and order history.
- [x] Context API and Axios integration.
- [x] Error/loading handling and responsive styling.

## Testing and engineering quality

- [x] Tests using JUnit, Mockito, MockMvc and Testcontainers.
- [x] Recorded 64 green backend tests.
- [x] JaCoCo minimum 80% line threshold passed (recorded 98%).
- [x] ESLint and production build passed.
- [x] Docker Compose stack verified with PostgreSQL, Spring Boot and React/Nginx.
- [x] Swagger UI and OpenAPI JSON verified in the local application.
- [ ] Re-run `mvn clean verify` and frontend quality checks after any documentation/code changes that impact the build.
- [ ] Confirm the backend Docker image was rebuilt after adding Swagger (`docker compose up -d --build backend`).
- [ ] Confirm `git status` is clean after the final documentation push.
- [ ] Confirm the final repository can be cloned and started from `.env.example` on a clean setup.

## Required deliverables

- [x] Public Git repository with backend and frontend.
- [x] Root README prepared with Docker/local instructions, endpoints and decisions.
- [x] Separate theoretical answers prepared.
- [x] Demo screenshots prepared under `docs/evidence`.
- [ ] Copy documentation files into the actual repository and push.
- [ ] Check that evidence screenshots do not reveal JWTs, passwords or personal system information.
- [ ] Copy the actual submitted repository URL into the final email and send only when all files are published.

## Optional extras

- [x] Docker packaging.
- [x] Swagger/OpenAPI.
- [x] Responsive UI.
- [ ] Frontend unit tests (optional; not claimed).

## Suggested final verification commands

```powershell
# Root
git status --short
git check-ignore .env
docker compose config --quiet
docker compose up -d --build
docker compose ps

# Backend
cd backend
.\mvnw.cmd clean verify

# Frontend
cd ..\frontend
npm ci
npm run lint
npm run build
```

> Avoid running the destructive `docker compose down -v` command against the assessment's persistent PostgreSQL volume.
