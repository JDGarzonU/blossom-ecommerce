# Theoretical Questions — Full Stack Java + React

**Technical assessment:** Blossom Technologies  
**Candidate:** Johan David Garzón Uricoechea  
**Project:** Blossom E-commerce  
**Repository:** https://github.com/JDGarzonU/blossom-ecommerce  
**Date:** November 16, 2026

---

## 1. Explain how you would implement the Repository pattern in this project.

I implement the Repository pattern by isolating database access behind interfaces that represent domain-specific persistence operations. In Blossom E-commerce, the REST controllers do not execute SQL or depend directly on `EntityManager`; they delegate to services, and services depend on repository interfaces provided by Spring Data JPA.

For example, `ProductRepository` extends `JpaRepository<Product, Long>` and `JpaSpecificationExecutor<Product>`. The first interface provides standard persistence operations such as `save`, `findById` and `findAll`. The second allows search filters to be assembled conditionally, including optional product name, category and price boundaries. This proved preferable to a single JPQL statement with nullable parameters: while implementing the assessment, an initial approach led PostgreSQL/Hibernate to infer an incompatible type for `LOWER` (`lower(bytea)`). Dynamic Specifications resolved that problem without modifying the database schema.

The resulting interaction is:

```text
ProductController → ProductService → ProductRepository → PostgreSQL
```

The controller defines HTTP behavior and validates request bodies. The service enforces business rules, maps entities to response DTOs, controls transactions and decides which repository operations are appropriate. The repository defines the persistence contract. This separation supports unit tests: I can mock `ProductRepository` when testing `ProductService` and verify behavior without connecting to a real database. Separate Testcontainers integration tests then exercise persistence against PostgreSQL itself.

I apply the same approach to `UserRepository` for case-insensitive email lookup and to `OrderRepository` for user-scoped history retrieval. For checkout, `ProductRepository` also exposes a `PESSIMISTIC_WRITE` query to lock inventory rows. That locking is intentionally invoked within the service's transactional purchase flow, rather than added indiscriminately to every read.

The advantages are separation of concerns, testability, maintainable queries and lower coupling between business logic and a specific ORM implementation. A trade-off is that inherited Spring Data operations can encourage overuse of entities directly in API responses; this project avoids that by exposing dedicated request/response DTOs.

## 2. Differences between JPA and Hibernate and in which cases you would use each.

**JPA (Jakarta Persistence)** is a Java specification that defines how object-relational mapping and persistence should work. It standardizes APIs and annotations such as `@Entity`, `@Id`, `@ManyToOne`, `@OneToMany`, `@MappedSuperclass` and `EntityManager`. JPA itself is not a database engine or a complete ORM implementation.

**Hibernate ORM** is an implementation of the JPA specification and offers additional features outside the portable JPA API, including implementation-specific optimizations, query behavior and other extensions. In Spring Boot, I typically write domain mappings against Jakarta Persistence annotations and let Hibernate act as the persistence provider.

In Blossom E-commerce, I use JPA for the domain model: `User`, `Product`, `Order` and `OrderItem` define relations and constraints with portable annotations; `BaseEntity` provides inherited audit columns using `@MappedSuperclass`. The service layer uses Spring Data JPA repositories and `@Transactional`. Hibernate performs the SQL generation and object-to-table persistence at runtime.

I would prefer **standard JPA** for typical CRUD, entity relationships and portable persistence code, because it reduces dependence on provider-specific features. I would use **Hibernate-specific capabilities** only when a measurable requirement justifies them, such as an optimization or mapping not adequately handled by standard JPA. I would document the trade-off and test behavior against the chosen PostgreSQL version.

A key lesson from the assessment was that an ORM does not remove the need to understand generated SQL. The original optional-filter query caused a PostgreSQL `lower(bytea)` failure. Inspecting the SQL and using dynamically composed JPA Specifications solved the issue. For financial amounts I use `BigDecimal`, not floating-point primitives, and order-item historical prices remain independent of later catalog price changes.

In short, **JPA defines the contract and Hibernate executes it**. They are complementary rather than competing alternatives in this application's stack.

## 3. What advantages does React offer compared to other frontend frameworks?

React is a component-based JavaScript library for building user interfaces. For this assignment, its most useful strengths are reusable components, declarative rendering, a mature ecosystem and flexibility in organizing application state.

The Blossom UI shares a common layout and product-card components across catalog, detail, cart and order-history screens. When the shopping cart state changes, React rerenders the affected components without manually synchronizing DOM elements. Hooks and Context API let the app share authentication and cart state without introducing Redux for a relatively small application. React Router provides navigation for login, registration, product details, cart and order history; Axios provides a centralized API client.

Compared with **Angular**, React generally requires less built-in framework structure for a small single-page application. Angular offers an integrated solution with more conventions and built-in mechanisms, which can be advantageous for larger teams seeking standardization. React instead leaves more freedom in routing, state and data-layer choices, but this also requires deliberate architectural decisions to avoid inconsistent patterns.

Compared with **Vue**, both support components, reactive updates and a productive development experience. Vue provides an integrated template-based approach that many teams find approachable; React's JSX and large ecosystem suit teams already comfortable with modern JavaScript and composable UI patterns. Neither option is categorically superior: team experience, domain complexity and maintainability determine the fit.

For the assessment, React and Vite made it practical to implement and validate a complete flow quickly. ESLint identified potential Hook and Fast Refresh issues, and the production build helped confirm the SPA was bundleable. Responsive CSS was sufficient for the required interface without introducing a styling dependency merely for appearance.

React also has trade-offs: state ownership, effects, stale closures, authentication lifecycle and accessibility need attention. Component flexibility is a strength when supported by consistent conventions and tests, not by itself a guarantee of maintainability.

## 4. Explain how you would organize your time over 2 days to deliver this assignment.

I would work from the evaluation criteria backward, reserving comparable effort for backend and frontend (40% each) and explicit time for documentation/theory (20%). I would prioritize an end-to-end working path over adding unrelated infrastructure or polishing only one layer.

### Day 1 — Core domain, API and first vertical slice

| Timebox | Activity | Completion check |
|---|---|---|
| First 1–2 hours | Analyze requirements, define entities/relations, decide API contracts, initialize Git, Maven, React and PostgreSQL | Run both skeleton applications |
| Next 2–3 hours | Implement entities, repositories, product CRUD, search filters and validations | Create/search products using HTTP client |
| Next 2 hours | Implement registration/login, BCrypt, JWT roles and basic security tests | Public GET, admin writes and customer restrictions |
| Remaining time | Implement order creation and history with server-calculated totals and stock consistency | Create a multi-item order and inspect PostgreSQL |

At the end of Day 1, I would commit a stable backend, record any blockers and stop extending scope until the essential API works.

### Day 2 — React, automated verification and submission

| Timebox | Activity | Completion check |
|---|---|---|
| First 2–3 hours | Create login/register, product catalog/detail, filters and shared auth/cart state | Pages navigate and consume the API |
| Next 1–2 hours | Complete checkout, order-history screens, error/loading behavior and responsive checks | Full browser journey works |
| Next 2 hours | Add JUnit, Mockito, MockMvc and PostgreSQL integration tests; run JaCoCo | No test failures and at least 80% measured coverage |
| Final timebox | Write README, separate theory answers, capture a short demo/screenshots, verify a clean setup and push | Reviewer can clone/run and find every required deliverable |

I would keep **frequent small commits**, a visible checklist of acceptance criteria and a short contingency buffer. I would test the most important integration points early: authentication, optional PostgreSQL filters, purchase transaction and frontend-to-backend routing. If time became constrained, I would defer optional Docker/Swagger/frontend unit tests before sacrificing required screens or the 80% backend test requirement.

For this submission, the resulting project also includes Docker Compose and Swagger/OpenAPI, with the automated test suite running against PostgreSQL Testcontainers. That was added only after the core application flow was working.

---

**End of theoretical responses.**
