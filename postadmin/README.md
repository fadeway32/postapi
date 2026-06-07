# postadmin

`postadmin` is the Spring Boot backend of `postapiAll`. It manages tenants, API groups, API definitions, encrypted secrets, runtime API execution, Groovy simulation, call logs and dashboard statistics.

## Features

- MyBatis-Plus persistence, H2 default database.
- Sa-Token login and session based tenant isolation.
- SaaS tenant table and per-tenant API definitions/groups/logs.
- API definitions stored in database, with Groovy script overrides at runtime.
- Sensitive `secret` configuration encrypted by postcryption before storage.
- Remote third-party calls executed through postapi.
- Simulate, execute, batch execute, call logs, top API stats and group stats.

## Run

Build local starters first when they changed:

```bash
cd ../postapi && mvn -q -DskipTests install
cd ../postcryption && mvn -q -DskipTests clean install
```

Build and start:

```bash
cd ../postadmin
mvn -q -DskipTests package
java -jar target/postadmin-0.0.1-SNAPSHOT.jar
```

Default URL: `http://127.0.0.1:8088`

Default login:

```json
{
  "tenantCode": "demo",
  "username": "admin",
  "password": "admin123"
}
```

## Main APIs

- `POST /auth/login`
- `POST /auth/logout`
- `GET /auth/me`
- `GET|POST /admin/tenants`
- `GET|POST /api/groups`
- `PUT|DELETE /api/groups/{id}`
- `GET|POST /api/definitions`
- `GET /api/definitions/{apiCode}`
- `PUT|DELETE /api/definitions/{id}`
- `POST /api/runtime/{apiCode}/simulate`
- `POST /api/runtime/{apiCode}/execute`
- `POST /api/runtime/batch`
- `GET /api/stats/top`
- `GET /api/stats/groups`
- `GET /api/stats/logs`

## Groovy Script

`scriptText` must return a `Map`. It can override `method`, `url`, `headers`, `query`, `bodyType`, `body`, and `timeoutMillis`.

Available variables:

- `payload`: caller request payload.
- `secret`: decrypted secret map.
- `api`: current API definition entity.
- `now`: current `LocalDateTime`.

Example:

```groovy
return [
  headers: [
    Authorization: 'Bearer ' + secret.token
  ],
  query: [
    q: payload.keyword
  ],
  bodyType: 'JSON',
  body: [
    tenant: payload.tenant,
    timestamp: now.toString()
  ]
]
```

Template fields support `${payload.xxx}`, `${secret.xxx}` and shorthand `${xxx}`.
