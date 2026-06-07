# postapiAll

[English](README.md) | [中文](README.zh-CN.md)

`postapiAll` 是一个全栈 API 管理项目工作区，包含 Spring Boot 管理后台、Vue 3 前端控制台、Postman 风格的 HTTP 请求 starter，以及用于敏感数据和接口模拟的加密 starter。

## 项目能力

- 管理租户、API 分组、API 定义、密钥配置、调用日志和统计数据。
- 基于数据库中的 API 定义执行第三方 HTTP 接口。
- 支持运行时 Groovy 脚本，用于接口模拟、动态组装请求、加密参数生成和 mock 调用。
- 提供可复用的 Spring Boot starter：`postapi` 负责 HTTP 调用，`postcryption` 负责加密能力。
- 提供 Vue 3 + Element Plus 管理界面，用于 API 配置、运行调试、日志查看和租户管理。

## 模块说明

| 模块 | 类型 | 说明 |
| --- | --- | --- |
| `postadmin` | Spring Boot 应用 | 主后台服务，提供认证、租户隔离、API 定义、运行时执行、Groovy 脚本、调用日志和统计能力。 |
| `postapifront` | Vue 前端 | 基于 Vue 3、Vite、Element Plus、Pinia、UnoCSS 的管理控制台。 |
| `postapi` | Spring Boot starter | Postman 风格 HTTP 请求引擎，支持 JSON、XML、表单、multipart、二进制、Protobuf、Kryo、SSE 和 WebSocket。 |
| `postcryption` | Spring Boot starter | 加密工具 starter，支持 AES、SM4、DES、3DES、RSA、ECC/ECIES、SM2、MD5、SHA-256、SHA3-256、SM3 和混合加密。 |

## 技术栈

- 后端：Java 8、Spring Boot 2.7.18、MyBatis-Plus、Sa-Token、Groovy、H2
- HTTP 引擎：Apache HttpClient 5、OkHttp、Hutool HTTP、Reactor Netty
- 加密：Bouncy Castle、postcryption 工具类
- 前端：Vue 3、Vite/Rolldown Vite、TypeScript、Element Plus、Pinia、UnoCSS
- 部署：Maven、Docker、Docker Compose

## 后端快速启动

在仓库根目录构建所有 Maven 模块：

```bash
mvn -q -DskipTests clean package
```

启动后端：

```bash
java -jar postadmin/target/postadmin-0.0.1-SNAPSHOT.jar
```

后端默认地址：

```text
http://127.0.0.1:8088
```

默认登录账号：

```json
{
  "tenantCode": "demo",
  "username": "admin",
  "password": "admin123"
}
```

## 前端快速启动

进入 `postapifront` 目录启动前端：

```bash
cd postapifront
pnpm install
pnpm dev
```

前端默认地址：

```text
http://127.0.0.1:9527
```

前端开发代理指向：

```text
http://127.0.0.1:8088
```

## 主要后端接口

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

## Groovy 运行脚本

`postadmin` 支持在 API 定义中配置 `scriptText`，脚本返回值可以覆盖请求方法、URL、请求头、查询参数、请求体类型、请求体和超时时间。

脚本可用变量：

- `payload`：调用方传入的请求参数。
- `secret`：解密后的密钥配置。
- `api` / `apiDefinition`：当前 API 定义。
- `headers`：当前请求头配置。
- `query`：当前查询参数配置。
- `now`：当前 `LocalDateTime`。
- `bindings`：脚本上下文变量快照。

示例：

```groovy
return [
  success: true,
  statusCode: 200,
  responseBody: "hello ${payload.name}",
  request: [
    url: url,
    payload: payload
  ]
]
```

## Docker 部署

仓库已经包含前后端 Docker 配置：

- `postadmin`：Spring Boot 后端服务，容器端口为 `8088`。
- `postapifront`：Vue 前端生产构建，由 Nginx 提供静态资源服务，容器端口为 `80`。

前端生产镜像中，接口请求默认使用 `/proxy-default`。Nginx 会把这个路径代理到 `http://postadmin:8088`，所以浏览器只需要访问前端服务即可。

### 使用 Docker Compose 部署前后端

在仓库根目录构建并启动：

```bash
docker compose up -d --build
```

默认访问地址：

```text
前端：http://127.0.0.1:9527
后端：http://127.0.0.1:8088
```

默认登录账号：

```json
{
  "tenantCode": "demo",
  "username": "admin",
  "password": "admin123"
}
```

### Compose 环境变量

可以通过环境变量覆盖端口、镜像名称和 JVM 参数：

```bash
POSTADMIN_PORT=8088 \
POSTAPIFRONT_PORT=9527 \
POSTADMIN_IMAGE=fadeway32/postadmin:latest \
POSTAPIFRONT_IMAGE=fadeway32/postapifront:latest \
JAVA_OPTS="-Xms256m -Xmx512m" \
docker compose up -d --build
```

Windows PowerShell 示例：

```powershell
$env:POSTADMIN_PORT="8088"
$env:POSTAPIFRONT_PORT="9527"
$env:JAVA_OPTS="-Xms256m -Xmx512m"
docker compose up -d --build
```

### 数据持久化

`postadmin` 使用 H2 文件数据库，容器内数据路径为：

```text
/app/data/postadmin
```

`compose.yaml` 会把它挂载到 Docker 命名卷：

```text
postadmin-data:/app/data
```

查看数据卷：

```bash
docker volume ls
docker volume inspect postapiall_postadmin-data
```

实际的数据卷前缀取决于 Compose 项目名。

### 查看状态和日志

查看服务状态：

```bash
docker compose ps
```

查看日志：

```bash
docker compose logs -f postadmin
docker compose logs -f postapifront
```

简单连通性检查：

```bash
curl http://127.0.0.1:9527
curl http://127.0.0.1:8088/auth/me
```

未登录时 `/auth/me` 可能返回未认证信息，这里主要用于确认后端服务可访问。

### 升级部署

保留数据卷并重新构建启动：

```bash
git pull
docker compose up -d --build
```

### 停止或清理

停止容器但保留数据：

```bash
docker compose down
```

停止容器并删除 H2 数据卷：

```bash
docker compose down -v
```

### 只运行后端容器

构建并运行后端镜像：

```bash
docker build -f postadmin/Dockerfile -t fadeway32/postadmin:latest .
docker run -d \
  --name postadmin \
  -p 8088:8088 \
  -e JAVA_OPTS="-Xms256m -Xmx512m" \
  -v postadmin-data:/app/data \
  fadeway32/postadmin:latest
```

### 只构建前端容器

构建并运行前端镜像：

```bash
docker build -t fadeway32/postapifront:latest ./postapifront
docker run -d \
  --name postapifront \
  -p 9527:80 \
  fadeway32/postapifront:latest
```

如果单独运行前端容器，需要确保 `/proxy-default` 能被代理到可访问的后端服务。使用 Compose 时会自动通过容器网络代理到 `postadmin`。

## GitHub 仓库信息

仓库地址：

```text
https://github.com/fadeway32/postapi
```

推荐仓库主题：

```text
spring-boot, vue3, api-management, groovy, postman, http-client, encryption, element-plus, sa-token
```

推荐仓库描述：

```text
Full-stack API management platform with Spring Boot, Vue 3, runtime Groovy scripts, encrypted secrets, Postman-like HTTP execution, and cryptography utilities.
```

## License

本项目采用 MIT License，详见 [LICENSE](LICENSE)。
