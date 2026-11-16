# Frontend test suite

Files under this folder test the *existing* Blossom React application using Vitest, React Testing Library and jsdom. Axios is mocked; the tests do not modify PostgreSQL and do not require Spring Boot or Docker.

From `frontend/`:

```powershell
npm install -D "vitest@^4.1.0" "@vitest/coverage-v8@^4.1.0" jsdom @testing-library/react @testing-library/jest-dom
npm pkg set "scripts.test=vitest run" "scripts.test:watch=vitest" "scripts.test:coverage=vitest run --coverage"
npm run test
npm run test:coverage
npm run lint
npm run build
```

The first installation updates `package.json` and `package-lock.json`; **commit both**. Open `frontend/coverage/index.html` for the frontend coverage report. That report is separate from `backend/target/site/jacoco/index.html`.

Coverage counts should be reported only after executing the suite. The backend's 80% JaCoCo threshold does not apply to these optional frontend tests.
