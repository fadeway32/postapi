package com.fadeway32.postapi.client.adapter;

import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.lang.reflect.Method;
import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

public class HutoolHttpClientAdapter implements HttpClientAdapter {
    private final ObjectMapper objectMapper;
    private final Duration defaultTimeout;

    public HutoolHttpClientAdapter(ObjectMapper objectMapper) {
        this(objectMapper, Duration.ofSeconds(30));
    }

    public HutoolHttpClientAdapter(ObjectMapper objectMapper, Duration defaultTimeout) {
        this.objectMapper = objectMapper;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public HttpClientVendor vendor() {
        return HttpClientVendor.HUTOOL;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ApiResponse execute(ApiRequest request) {
        try {
            Class<?> requestClass = Class.forName("cn.hutool.http.HttpRequest");
            Object hutoolRequest = requestClass.getMethod(request.method().name().toLowerCase(), String.class)
                    .invoke(null, buildUrl(request));
            applyTimeout(hutoolRequest, request);
            hutoolRequest.getClass().getMethod("keepAlive", boolean.class).invoke(hutoolRequest, true);
            applyHeaders(hutoolRequest, request.headers());
            applyBody(hutoolRequest, request);
            Object response = hutoolRequest.getClass().getMethod("execute").invoke(hutoolRequest);
            int status = (Integer) response.getClass().getMethod("getStatus").invoke(response);
            byte[] bytes = (byte[]) response.getClass().getMethod("bodyBytes").invoke(response);
            String body = new String(bytes, StandardCharsets.UTF_8);
            Object headers = response.getClass().getMethod("headers").invoke(response);
            response.getClass().getMethod("close").invoke(response);
            return new ApiResponse(status, "", (Map<String, List<String>>) headers, body, bytes);
        } catch (ReflectiveOperationException ex) {
            throw new PostApiException("Hutool HTTP adapter failed. Please verify hutool-http is on the classpath.", ex);
        }
    }

    private String buildUrl(ApiRequest request) {
        StringBuilder url = new StringBuilder(request.uri().toString());
        if (request.queryParams().isEmpty()) {
            return url.toString();
        }
        url.append(request.uri().getQuery() == null ? '?' : '&');
        request.queryParams().forEach((name, values) -> values.forEach(value -> {
            if (url.charAt(url.length() - 1) != '?' && url.charAt(url.length() - 1) != '&') {
                url.append('&');
            }
            url.append(name).append('=').append(value);
        }));
        return url.toString();
    }

    private void applyHeaders(Object hutoolRequest, Map<String, List<String>> headers) throws ReflectiveOperationException {
        Method header = hutoolRequest.getClass().getMethod("header", String.class, String.class, boolean.class);
        headers.forEach((name, values) -> values.forEach(value -> invokeHeader(header, hutoolRequest, name, value)));
    }

    private void applyTimeout(Object hutoolRequest, ApiRequest request) throws ReflectiveOperationException {
        Duration timeout = request.responseTimeout() == null ? defaultTimeout : request.responseTimeout();
        hutoolRequest.getClass().getMethod("timeout", int.class).invoke(hutoolRequest, Math.toIntExact(timeout.toMillis()));
    }

    private void invokeHeader(Method method, Object target, String name, String value) {
        try {
            method.invoke(target, name, value, true);
        } catch (ReflectiveOperationException ex) {
            throw new PostApiException("Failed to set Hutool request header: " + name, ex);
        }
    }

    private void applyBody(Object hutoolRequest, ApiRequest request) throws ReflectiveOperationException {
        switch (request.bodyType()) {
            case NONE:
                break;
            case JSON:
                applyTextBody(hutoolRequest, toJson(request.body()), "application/json");
                break;
            case XML:
                applyTextBody(hutoolRequest, request.body() == null ? "" : String.valueOf(request.body()), "application/xml");
                break;
            case FORM_URLENCODED:
                Method form = hutoolRequest.getClass().getMethod("form", String.class, Object.class);
                request.formFields().forEach((name, value) -> invokeForm(form, hutoolRequest, name, value));
                break;
            case MULTIPART:
                applyMultipart(hutoolRequest, request);
                break;
            case BINARY:
            case PROTOBUF:
            case KRYO:
                applyBinary(hutoolRequest, request);
                break;
            default:
                throw new PostApiException("Unsupported body type: " + request.bodyType());
        }
    }

    private void applyTextBody(Object hutoolRequest, String body, String contentType) throws ReflectiveOperationException {
        hutoolRequest.getClass().getMethod("body", String.class, String.class).invoke(hutoolRequest, body, contentType);
    }

    private void applyBinary(Object hutoolRequest, ApiRequest request) throws ReflectiveOperationException {
        Object body = request.body();
        byte[] bytes = body instanceof byte[] ? (byte[]) body : new byte[0];
        hutoolRequest.getClass().getMethod("body", byte[].class).invoke(hutoolRequest, (Object) bytes);
        String contentType = request.contentType() == null ? "application/octet-stream" : request.contentType();
        hutoolRequest.getClass().getMethod("contentType", String.class).invoke(hutoolRequest, contentType);
    }

    private void applyMultipart(Object hutoolRequest, ApiRequest request) throws ReflectiveOperationException {
        Method form = hutoolRequest.getClass().getMethod("form", String.class, Object.class);
        request.multipartFields().forEach((name, value) -> invokeForm(form, hutoolRequest, name, value));
        request.files().forEach(file -> {
            try {
                Object value = file.path() == null ? Files.createTempFile("postapi-upload-", file.fileName()).toFile() : file.path().toFile();
                if (value instanceof File && file.path() == null) {
                    File temp = (File) value;
                    Files.write(temp.toPath(), file.content());
                    temp.deleteOnExit();
                }
                form.invoke(hutoolRequest, file.fieldName(), value);
            } catch (Exception ex) {
                throw new PostApiException("Failed to add Hutool multipart file: " + file.fileName(), ex);
            }
        });
    }

    private void invokeForm(Method method, Object target, String name, Object value) {
        try {
            method.invoke(target, name, value);
        } catch (ReflectiveOperationException ex) {
            throw new PostApiException("Failed to set Hutool form field: " + name, ex);
        }
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
}
