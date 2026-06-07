# postapiAll

`postapiAll` is a full-stack API management workspace. It combines a Spring Boot admin service, a Vue 3 management console, a Postman-like HTTP request starter, and a cryptography starter for encrypted API payloads and runtime scripts.

## What It Does

- Manage tenants, API groups, API definitions, secrets, execution logs, and statistics.
- Execute third-party HTTP APIs from database-backed definitions.
- Support runtime Groovy scripts for request simulation, dynamic request assembly, and encrypted mock calls.
- Provide reusable Spring Boot starters for HTTP calls and cryptography utilities.
- Offer a Vue 3 + Element Plus admin UI for API configuration, execution, logs, and tenant management.

## Modules

| Module | Type | Description |
| --- | --- | --- |
| `postadmin` | Spring Boot app | Admin backend with auth, tenant isolation, API definitions, runtime execution, Groovy scripting, logs, and stats. |
| `postapifront` | Vue app | Frontend console built with Vue 3, Vite, Element Plus, Pinia, and UnoCSS. |
| `postapi` | Spring Boot starter | Postman-like HTTP client starter with JSON, XML, form, multipart, binary, Protobuf, Kryo, SSE, and WebSocket support. |
| `postcryption` | Spring Boot starter | Cryptography starter for AES, SM4, DES, 3DES, RSA, ECC/ECIES, SM2, MD5, SHA-256, SHA3-256, SM3, and hybrid encryption. |

## Tech Stack

- Backend: Java 8, Spring Boot 2.7.18, MyBatis-Plus, Sa-Token, Groovy, H2
- HTTP engines: Apache HttpClient 5, OkHttp, Hutool HTTP, Reactor Netty
- Crypto: Bouncy Castle, postcryption utilities
- Frontend: Vue 3, Vite/Rolldown Vite, TypeScript, Element Plus, Pinia, UnoCSS
- Deployment: Maven, Docker, Docker Compose

## Backend Quick Start

Build all Maven modules from the repository root:

```bash
mvn -q -DskipTests clean package
```

Start the backend:

```bash
java -jar postadmin/target/postadmin-0.0.1-SNAPSHOT.jar
```

Backend URL:

```text
http://127.0.0.1:8088
```

Default login:

```json
{
  "tenantCode": "demo",
  "username": "admin",
  "password": "admin123"
}
```

## Frontend Quick Start

Start the frontend from `postapifront`:

```bash
cd postapifront
pnpm install
pnpm dev
```

Frontend URL:

```text
http://127.0.0.1:9527
```

The frontend development proxy points to:

```text
http://127.0.0.1:8088
```

## Main Backend APIs

- `POST /auth/login`
- `POST /auth/logout`
- `GET /auth/me`
- `GET|POST /admin/tenants`
- `GET|POST /api/groups`
- `GET|POST /api/definitions`
- `POST /api/runtime/{apiCode}/simulate`
- `POST /api/runtime/{apiCode}/execute`
- `POST /api/runtime/batch`
- `GET /api/stats/top`
- `GET /api/stats/groups`
- `GET /api/stats/logs`

## Docker

Build the backend image:

```bash
mvn -q -DskipTests clean package
docker build -f postadmin/Dockerfile -t fadeway32/postadmin:latest .
```

Run with Docker:

```bash
docker run -d --name postadmin -p 8088:8088 -v postadmin-data:/app/data fadeway32/postadmin:latest
```

Run with Compose:

```bash
docker compose up -d
```

## Repository

Suggested GitHub repository:

```text
https://github.com/fadeway32/postapiAll
```

Recommended topics:

```text
spring-boot, vue3, api-management, groovy, postman, http-client, encryption, element-plus, sa-token
```

Recommended GitHub description:

```text
Full-stack API management platform with Spring Boot, Vue 3, runtime Groovy scripts, encrypted secrets, Postman-like HTTP execution, and cryptography utilities.
```

## License

This project is released under the MIT License. See [LICENSE](LICENSE).
