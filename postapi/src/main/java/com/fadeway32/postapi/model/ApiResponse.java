package com.fadeway32.postapi.model;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public final class ApiResponse {
    private final int statusCode;
    private final String reasonPhrase;
    private final Map<String, List<String>> headers;
    private final String body;
    private final byte[] bodyBytes;
    private final String traceId;
    private final long elapsedMillis;

    public ApiResponse(
            int statusCode,
            String reasonPhrase,
            Map<String, List<String>> headers,
            String body,
            byte[] bodyBytes,
            String traceId,
            long elapsedMillis
    ) {
        this.statusCode = statusCode;
        this.reasonPhrase = reasonPhrase;
        this.headers = headers;
        this.body = body;
        this.bodyBytes = bodyBytes == null ? new byte[0] : bodyBytes.clone();
        this.traceId = traceId;
        this.elapsedMillis = elapsedMillis;
    }

    public ApiResponse(int statusCode, String reasonPhrase, Map<String, List<String>> headers, String body, byte[] bodyBytes) {
        this(statusCode, reasonPhrase, headers, body, bodyBytes, null, -1);
    }

    public ApiResponse(int statusCode, String reasonPhrase, Map<String, List<String>> headers, String body) {
        this(statusCode, reasonPhrase, headers, body, body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8));
    }

    public int statusCode() {
        return statusCode;
    }

    public String reasonPhrase() {
        return reasonPhrase;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public String body() {
        return body;
    }

    public String json() {
        return body;
    }

    public String toJson() {
        return body;
    }

    public byte[] bodyBytes() {
        return bodyBytes.clone();
    }

    public String traceId() {
        return traceId;
    }

    public long elapsedMillis() {
        return elapsedMillis;
    }

    public boolean is2xxSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }

    public ApiResponse withTraceId(String traceId) {
        return new ApiResponse(statusCode, reasonPhrase, headers, body, bodyBytes, traceId, elapsedMillis);
    }

    public ApiResponse withElapsedMillis(long elapsedMillis) {
        return new ApiResponse(statusCode, reasonPhrase, headers, body, bodyBytes, traceId, elapsedMillis);
    }

    @Override
    public String toString() {
        return body;
    }
}
