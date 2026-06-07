package com.fadeway32.postapi.model;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ApiRequest {
    private final HttpMethod method;
    private final URI uri;
    private final Map<String, List<String>> headers;
    private final Map<String, List<String>> queryParams;
    private final Map<String, String> formFields;
    private final List<UploadFile> files;
    private final Map<String, String> multipartFields;
    private final BodyType bodyType;
    private final Object body;
    private final String contentType;
    private final Duration responseTimeout;

    private ApiRequest(Builder builder) {
        this.method = builder.method;
        this.uri = builder.uri;
        this.headers = deepUnmodifiable(builder.headers);
        this.queryParams = deepUnmodifiable(builder.queryParams);
        this.formFields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.formFields));
        this.files = Collections.unmodifiableList(new ArrayList<>(builder.files));
        this.multipartFields = Collections.unmodifiableMap(new LinkedHashMap<>(builder.multipartFields));
        this.bodyType = builder.bodyType;
        this.body = builder.body;
        this.contentType = builder.contentType;
        this.responseTimeout = builder.responseTimeout;
    }

    public static Builder get(String uri) {
        return builder(HttpMethod.GET, uri);
    }

    public static Builder post(String uri) {
        return builder(HttpMethod.POST, uri);
    }

    public static Builder put(String uri) {
        return builder(HttpMethod.PUT, uri);
    }

    public static Builder delete(String uri) {
        return builder(HttpMethod.DELETE, uri);
    }

    public static Builder builder(HttpMethod method, String uri) {
        return new Builder(method, URI.create(uri));
    }

    public HttpMethod method() {
        return method;
    }

    public URI uri() {
        return uri;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public Map<String, List<String>> queryParams() {
        return queryParams;
    }

    public Map<String, String> formFields() {
        return formFields;
    }

    public List<UploadFile> files() {
        return files;
    }

    public Map<String, String> multipartFields() {
        return multipartFields;
    }

    public BodyType bodyType() {
        return bodyType;
    }

    public Object body() {
        return body;
    }

    public String contentType() {
        return contentType;
    }

    public Duration responseTimeout() {
        return responseTimeout;
    }

    public Builder toBuilder() {
        Builder builder = new Builder(method, uri);
        headers.forEach((name, values) -> values.forEach(value -> builder.header(name, value)));
        queryParams.forEach((name, values) -> values.forEach(value -> builder.queryParam(name, value)));
        formFields.forEach(builder.formFields::put);
        builder.files.addAll(files);
        builder.multipartFields.putAll(multipartFields);
        builder.bodyType = bodyType;
        builder.body = body instanceof byte[] ? ((byte[]) body).clone() : body;
        builder.contentType = contentType;
        builder.responseTimeout = responseTimeout;
        return builder;
    }

    public ApiRequest withHeader(String name, Object value) {
        return toBuilder().header(name, value).build();
    }

    private static Map<String, List<String>> deepUnmodifiable(Map<String, List<String>> source) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, Collections.unmodifiableList(new ArrayList<>(value))));
        return Collections.unmodifiableMap(copy);
    }

    public static final class Builder {
        private final HttpMethod method;
        private final URI uri;
        private final Map<String, List<String>> headers = new LinkedHashMap<>();
        private final Map<String, List<String>> queryParams = new LinkedHashMap<>();
        private final Map<String, String> formFields = new LinkedHashMap<>();
        private final List<UploadFile> files = new ArrayList<>();
        private final Map<String, String> multipartFields = new LinkedHashMap<>();
        private BodyType bodyType = BodyType.NONE;
        private Object body;
        private String contentType;
        private Duration responseTimeout;

        private Builder(HttpMethod method, URI uri) {
            this.method = Objects.requireNonNull(method, "method must not be null");
            this.uri = Objects.requireNonNull(uri, "uri must not be null");
        }

        public Builder header(String name, Object value) {
            add(headers, name, value);
            return this;
        }

        public Builder queryParam(String name, Object value) {
            add(queryParams, name, value);
            return this;
        }

        public Builder json(Object body) {
            this.bodyType = BodyType.JSON;
            this.body = body;
            this.contentType = "application/json";
            return this;
        }

        public Builder xml(String body) {
            this.bodyType = BodyType.XML;
            this.body = body;
            this.contentType = "application/xml";
            return this;
        }

        public Builder form(String name, Object value) {
            this.bodyType = BodyType.FORM_URLENCODED;
            this.contentType = "application/x-www-form-urlencoded";
            this.formFields.put(name, String.valueOf(value));
            return this;
        }

        public Builder multipart() {
            this.bodyType = BodyType.MULTIPART;
            return this;
        }

        public Builder binary(byte[] body, String contentType) {
            this.bodyType = BodyType.BINARY;
            this.body = body == null ? new byte[0] : body.clone();
            this.contentType = contentType;
            return this;
        }

        public Builder protobuf(byte[] body) {
            return binary(body, "application/x-protobuf").bodyType(BodyType.PROTOBUF);
        }

        public Builder kryo(byte[] body) {
            return binary(body, "application/x-kryo").bodyType(BodyType.KRYO);
        }

        public Builder acceptSse() {
            return header("Accept", "text/event-stream");
        }

        public Builder part(String name, Object value) {
            this.bodyType = BodyType.MULTIPART;
            this.multipartFields.put(name, String.valueOf(value));
            return this;
        }

        public Builder file(String fieldName, Path path) {
            this.bodyType = BodyType.MULTIPART;
            this.files.add(UploadFile.fromPath(fieldName, path));
            return this;
        }

        public Builder file(String fieldName, Path path, String contentType) {
            this.bodyType = BodyType.MULTIPART;
            this.files.add(UploadFile.fromPath(fieldName, path, contentType));
            return this;
        }

        public Builder file(String fieldName, String fileName, byte[] content, String contentType) {
            this.bodyType = BodyType.MULTIPART;
            this.files.add(UploadFile.fromBytes(fieldName, fileName, content, contentType));
            return this;
        }

        public Builder responseTimeout(Duration responseTimeout) {
            this.responseTimeout = responseTimeout;
            return this;
        }

        private Builder bodyType(BodyType bodyType) {
            this.bodyType = bodyType;
            return this;
        }

        public ApiRequest build() {
            return new ApiRequest(this);
        }

        private static void add(Map<String, List<String>> target, String name, Object value) {
            target.computeIfAbsent(name, ignored -> new ArrayList<>()).add(String.valueOf(value));
        }
    }
}
