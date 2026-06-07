package com.fadeway32.postapi.client.adapter;

import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.client.SseEventHandler;
import com.fadeway32.postapi.client.WebSocketHandler;
import com.fadeway32.postapi.client.WebSocketSession;
import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fadeway32.postapi.model.UploadFile;
import com.fadeway32.postapi.model.WebSocketRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class OkHttpClientAdapter implements HttpClientAdapter {
    private final OkHttpClient okHttpClient;
    private final ObjectMapper objectMapper;

    public OkHttpClientAdapter(OkHttpClient okHttpClient, ObjectMapper objectMapper) {
        this.okHttpClient = okHttpClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public HttpClientVendor vendor() {
        return HttpClientVendor.OKHTTP;
    }

    @Override
    public ApiResponse execute(ApiRequest request) {
        Request okRequest = toOkHttpRequest(request);
        Call call = okHttpClient.newCall(okRequest);
        try (Response response = call.execute()) {
            return toApiResponse(response);
        } catch (IOException ex) {
            throw new PostApiException("OkHttp request failed: " + request.method() + " " + request.uri(), ex);
        }
    }

    @Override
    public void streamSse(ApiRequest request, SseEventHandler handler) {
        Request okRequest = toOkHttpRequest(copyWithSseAccept(request));
        try (Response response = okHttpClient.newCall(okRequest).execute()) {
            ResponseBody body = response.body();
            if (body == null) {
                return;
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(body.byteStream(), StandardCharsets.UTF_8))) {
                SseParser.read(reader, handler);
            }
        } catch (IOException ex) {
            throw new PostApiException("OkHttp SSE request failed: " + request.uri(), ex);
        }
    }

    @Override
    public WebSocketSession openWebSocket(WebSocketRequest request, WebSocketHandler handler) {
        Request.Builder builder = new Request.Builder().url(request.uri().toString());
        request.headers().forEach((name, values) -> values.forEach(value -> builder.addHeader(name, value)));
        WebSocket webSocket = okHttpClient.newWebSocket(builder.build(), new WebSocketListener() {
            @Override
            public void onOpen(WebSocket webSocket, Response response) {
                handler.onOpen(new OkHttpWebSocketSession(webSocket));
            }

            @Override
            public void onMessage(WebSocket webSocket, String text) {
                handler.onText(text);
            }

            @Override
            public void onMessage(WebSocket webSocket, ByteString bytes) {
                handler.onBinary(bytes.toByteArray());
            }

            @Override
            public void onClosed(WebSocket webSocket, int code, String reason) {
                handler.onClosed(code, reason);
            }

            @Override
            public void onFailure(WebSocket webSocket, Throwable throwable, Response response) {
                handler.onError(throwable);
            }
        });
        return new OkHttpWebSocketSession(webSocket);
    }

    private Request toOkHttpRequest(ApiRequest request) {
        Request.Builder builder = new Request.Builder().url(buildUrl(request));
        request.headers().forEach((name, values) -> values.forEach(value -> builder.addHeader(name, value)));
        RequestBody body = createRequestBody(request);
        switch (request.method()) {
            case GET:
                builder.get();
                break;
            case POST:
                builder.post(bodyOrEmpty(body));
                break;
            case PUT:
                builder.put(bodyOrEmpty(body));
                break;
            case DELETE:
                if (body == null) {
                    builder.delete();
                } else {
                    builder.delete(body);
                }
                break;
            default:
                throw new PostApiException("Unsupported HTTP method: " + request.method());
        }
        return builder.build();
    }

    private String buildUrl(ApiRequest request) {
        okhttp3.HttpUrl.Builder builder = okhttp3.HttpUrl.get(request.uri()).newBuilder();
        request.queryParams().forEach((name, values) -> values.forEach(value -> builder.addQueryParameter(name, value)));
        return builder.build().toString();
    }

    private RequestBody createRequestBody(ApiRequest request) {
        switch (request.bodyType()) {
            case NONE:
                return null;
            case JSON:
                return textBody(toJson(request.body()), "application/json; charset=utf-8");
            case XML:
                return textBody(request.body() == null ? "" : String.valueOf(request.body()), "application/xml; charset=utf-8");
            case FORM_URLENCODED:
                return formBody(request.formFields());
            case MULTIPART:
                return multipartBody(request);
            case BINARY:
            case PROTOBUF:
            case KRYO:
                return binaryBody(request);
            default:
                throw new PostApiException("Unsupported body type: " + request.bodyType());
        }
    }

    private RequestBody textBody(String body, String contentType) {
        return RequestBody.create(body, MediaType.parse(contentType));
    }

    private RequestBody formBody(Map<String, String> fields) {
        FormBody.Builder builder = new FormBody.Builder(StandardCharsets.UTF_8);
        fields.forEach(builder::add);
        return builder.build();
    }

    private RequestBody multipartBody(ApiRequest request) {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        request.multipartFields().forEach(builder::addFormDataPart);
        for (UploadFile file : request.files()) {
            MediaType mediaType = MediaType.parse(file.contentType() == null ? "application/octet-stream" : file.contentType());
            byte[] bytes = file.content();
            if (bytes == null && file.path() != null) {
                try {
                    bytes = Files.readAllBytes(file.path());
                } catch (IOException ex) {
                    throw new PostApiException("Failed to read upload file: " + file.path(), ex);
                }
            }
            builder.addFormDataPart(file.fieldName(), file.fileName(), RequestBody.create(bytes == null ? new byte[0] : bytes, mediaType));
        }
        return builder.build();
    }

    private RequestBody binaryBody(ApiRequest request) {
        Object body = request.body();
        byte[] bytes = body instanceof byte[] ? (byte[]) body : new byte[0];
        String contentType = request.contentType() == null ? "application/octet-stream" : request.contentType();
        return RequestBody.create(bytes, MediaType.parse(contentType));
    }

    private RequestBody bodyOrEmpty(RequestBody body) {
        return body == null ? RequestBody.create(new byte[0], MediaType.parse("application/octet-stream")) : body;
    }

    private String toJson(Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof String) {
            return (String) body;
        }
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new PostApiException("Failed to serialize JSON request body", ex);
        }
    }

    private ApiResponse toApiResponse(Response response) throws IOException {
        ResponseBody responseBody = response.body();
        byte[] bytes = responseBody == null ? new byte[0] : responseBody.bytes();
        String body = new String(bytes, StandardCharsets.UTF_8);
        return new ApiResponse(response.code(), response.message(), response.headers().toMultimap(), body, bytes);
    }

    private ApiRequest copyWithSseAccept(ApiRequest request) {
        ApiRequest.Builder builder = ApiRequest.builder(request.method(), request.uri().toString()).acceptSse();
        request.headers().forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
        request.queryParams().forEach((name, values) -> values.forEach(value -> builder.queryParam(name, value)));
        if (request.responseTimeout() != null) {
            builder.responseTimeout(request.responseTimeout());
        }
        return builder.build();
    }

    private static final class OkHttpWebSocketSession implements WebSocketSession {
        private final WebSocket webSocket;

        private OkHttpWebSocketSession(WebSocket webSocket) {
            this.webSocket = webSocket;
        }

        @Override
        public boolean sendText(String text) {
            return webSocket.send(text);
        }

        @Override
        public boolean sendBinary(byte[] bytes) {
            return webSocket.send(ByteString.of(bytes));
        }

        @Override
        public boolean close(int code, String reason) {
            return webSocket.close(code, reason);
        }
    }
}
