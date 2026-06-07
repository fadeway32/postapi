# postapiAll

[English](README.md) | [中文](README.zh-CN.md)

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

## Docker Deployment

The repository includes Docker support for both services:

- `postadmin`: Spring Boot backend, exposed on container port `8088`.
- `postapifront`: Vue production build served by Nginx, exposed on container port `80`.

In the frontend image, production API calls use `/proxy-default`. Nginx proxies that path to `http://postadmin:8088`, so the browser only needs to access the frontend service.

### Deploy With Docker Compose

Build and start both frontend and backend from the repository root:

```bash
docker compose up -d --build
```

Default access URLs:

```text
Frontend: http://127.0.0.1:9527
Backend:  http://127.0.0.1:8088
```

Default login:

```json
{
  "tenantCode": "demo",
  "username": "admin",
  "password": "admin123"
}
```

### Compose Environment Variables

You can override ports, image tags and JVM options:

```bash
POSTADMIN_PORT=8088 \
POSTAPIFRONT_PORT=9527 \
POSTADMIN_IMAGE=fadeway32/postadmin:latest \
POSTAPIFRONT_IMAGE=fadeway32/postapifront:latest \
JAVA_OPTS="-Xms256m -Xmx512m" \
docker compose up -d --build
```

On Windows PowerShell:

```powershell
$env:POSTADMIN_PORT="8088"
$env:POSTAPIFRONT_PORT="9527"
$env:JAVA_OPTS="-Xms256m -Xmx512m"
docker compose up -d --build
```

### Data Persistence

`postadmin` uses H2 file storage in the container:

```text
/app/data/postadmin
```

`compose.yaml` mounts it to the named Docker volume:

```text
postadmin-data:/app/data
```

List and inspect the volume:

```bash
docker volume ls
docker volume inspect postapiall_postadmin-data
```

The exact volume prefix depends on the Compose project name.

### Logs And Health Checks

View service status:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f postadmin
docker compose logs -f postapifront
```

Smoke test:

```bash
curl http://127.0.0.1:9527
curl http://127.0.0.1:8088/auth/me
```

`/auth/me` may return an unauthenticated response before login; the goal is to confirm the backend is reachable.

### Upgrade

Rebuild and restart while keeping the data volume:

```bash
git pull
docker compose up -d --build
```

### Stop Or Remove

Stop containers but keep data:

```bash
docker compose down
```

Remove containers and the H2 data volume:

```bash
docker compose down -v
```

### Backend-Only Docker Run

Build and run only the backend image:

```bash
docker build -f postadmin/Dockerfile -t fadeway32/postadmin:latest .
docker run -d \
  --name postadmin \
  -p 8088:8088 \
  -e JAVA_OPTS="-Xms256m -Xmx512m" \
  -v postadmin-data:/app/data \
  fadeway32/postadmin:latest
```

### Frontend-Only Docker Build

Build and run only the frontend image:

```bash
docker build -t fadeway32/postapifront:latest ./postapifront
docker run -d \
  --name postapifront \
  -p 9527:80 \
  fadeway32/postapifront:latest
```

When running the frontend alone, make sure its `/proxy-default` path is routed to a reachable backend. Compose handles this automatically through the shared Docker network.

## Repository

Suggested GitHub repository:

```text
https://github.com/fadeway32/postapi
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
