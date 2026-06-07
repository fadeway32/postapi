# postapi-spring-boot-starter

`postapi-spring-boot-starter` is the reusable HTTP execution engine used by `postapiAll`. It provides a Postman-like Java API for building, sending and tracing HTTP requests from Spring Boot services or plain Java code.

它基于 `Java + Spring Boot + Apache HttpClient 5 / OkHttp / Hutool HTTP + Reactor Netty + 策略模式 + SPI`，适合做后端 API 编排、三方接口调用、运行时请求执行和内部工具平台。

## 能力范围

- HTTP 方法：`GET`、`POST`、`PUT`、`DELETE`
- HTTP 引擎：`OKHTTP`、`APACHE`、`HUTOOL`、`NETTY`
- 请求体：`JSON`、`XML`、`FORM_URLENCODED`、`MULTIPART`、`BINARY`、`PROTOBUF`、`KRYO`
- 文件上传：支持单文件、多文件、文件字节数组上传
- 流式协议：支持 SSE 读取，OkHttp 适配器支持 WebSocket
- 链路观测：自动注入 traceId，响应记录 `traceId` 和 `elapsedMillis`
- 异步执行：`CompletableFuture` API，支持自定义线程池参数
- 自动配置：引入 starter 后自动注册 `PostApiClient`
- SPI：非 Spring 环境可通过 `ServiceLoader` 创建 `PostApiClient`
- 扩展点：通过实现 `HttpClientAdapter`、`HttpMethodStrategy`、`BodyWriterStrategy` 自定义引擎、方法和请求体写入逻辑

## 快速使用

```xml
<dependency>
    <groupId>com.example.postapi</groupId>
    <artifactId>postapi-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

```yaml
postapi:
  enabled: true
  client-type: okhttp
  connect-timeout: 3s
  response-timeout: 30s
  max-total: 200
  max-per-route: 50
  max-idle-connections: 50
  keep-alive: 5m
  trace-enabled: true
  trace-header-name: X-Trace-Id
  async-core-pool-size: 8
  async-max-pool-size: 64
  async-queue-capacity: 1000
  user-agent: postapi-starter/0.0.1
```

```java
@Service
public class DemoService {
    private final PostApiClient postApiClient;

    public DemoService(PostApiClient postApiClient) {
        this.postApiClient = postApiClient;
    }

    public String createUser() {
        Map<String, Object> body = new HashMap<>();
        body.put("name", "Alice");
        ApiResponse response = postApiClient.execute(ApiRequest.post("https://example.com/users")
                .json(body)
                .header("X-Trace-Id", "trace-001"));
        return response.body();
    }
}
```

## 示例

### GET

```java
ApiResponse response = postApiClient.execute(ApiRequest.get("https://example.com/users")
        .queryParam("page", 1)
        .queryParam("size", 20));
```

### JSON

```java
Map<String, Object> body = new HashMap<>();
body.put("name", "Alice");
ApiResponse response = postApiClient.execute(ApiRequest.post("https://example.com/users")
        .json(body));
System.out.println(response.traceId());
System.out.println(response.elapsedMillis());
```

### 异步调用

```java
Map<String, Object> body = new HashMap<>();
body.put("name", "Alice");
CompletableFuture<ApiResponse> future = postApiClient.executeAsync(ApiRequest.post("https://example.com/users")
        .json(body));
```

### Netty 引擎

```yaml
postapi:
  client-type: netty
```

### XML

```java
ApiResponse response = postApiClient.execute(ApiRequest.post("https://example.com/orders")
        .xml("<order><id>1001</id></order>"));
```

### Form

```java
ApiResponse response = postApiClient.execute(ApiRequest.post("https://example.com/login")
        .form("username", "admin")
        .form("password", "123456"));
```

### 文件上传

```java
ApiResponse response = postApiClient.execute(ApiRequest.post("https://example.com/upload")
        .multipart()
        .part("bizType", "avatar")
        .file("file", Paths.get("avatar.png")));
```

### 二进制 / Protobuf / Kryo

```java
ApiResponse protobufResponse = postApiClient.execute(ApiRequest.post("https://example.com/events")
        .protobuf(ProtocolCodecUtils.toProtobufBytes(message)));

ApiResponse kryoResponse = postApiClient.execute(ApiRequest.post("https://example.com/cache")
        .kryo(ProtocolCodecUtils.toKryoBytes(payload)));
```

### SSE

```java
postApiClient.streamSse(ApiRequest.get("https://example.com/sse").acceptSse(), event -> {
    System.out.println(event.data());
});
```

### WebSocket

WebSocket 目前由 OkHttp 适配器提供。

```java
WebSocketSession session = postApiClient.openWebSocket(WebSocketRequest.ws("wss://example.com/ws"), new WebSocketHandler() {
    @Override
    public void onText(String text) {
        System.out.println(text);
    }
});
session.sendText("ping");
```

### 非 Spring SPI 使用

```java
PostApiClient client = PostApiClientFactories.create(new PostApiClientOptions()
        .setClientType(HttpClientVendor.OKHTTP));
ApiResponse response = client.execute(ApiRequest.get("https://example.com"));
```

### Util 调用

```java
Map<String, Object> body = new HashMap<>();
body.put("name", "Alice");
ApiResponse response = PostApiUtils.postJson("https://example.com/users", body);
```

## 代码规划

- `client`：对外暴露的 `PostApiClient`、默认实现、SSE/WebSocket 回调和底层适配器接口
- `client.adapter`：Apache、OkHttp、Hutool 三类 HTTP 客户端适配
- `model`：请求、响应、枚举、上传文件模型
- `strategy.method`：GET/POST/PUT/DELETE 方法策略，负责创建 Apache HttpClient 请求对象
- `strategy.body`：JSON/XML/Form/Multipart 请求体策略，负责把业务请求体写入 HTTP 请求
- `autoconfigure`：Spring Boot 自动配置、配置属性和 bean 注册
- `spi`：Java SPI 工厂，支持普通 jar 场景脱离 Spring 使用
- `util`：高频调用和协议编解码工具封装
- `exception`：统一异常类型，隔离底层 HttpClient 异常

## 扩展自定义请求体策略

```java
@Bean
BodyWriterStrategy customBodyWriterStrategy() {
    return new BodyWriterStrategy() {
        @Override
        public boolean supports(BodyType bodyType) {
            return false;
        }

        @Override
        public void write(ClassicHttpRequest request, ApiRequest apiRequest) {
            // custom writer
        }
    };
}
```
