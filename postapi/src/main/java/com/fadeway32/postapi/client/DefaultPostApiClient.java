package com.fadeway32.postapi.client;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.WebSocketRequest;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

public class DefaultPostApiClient implements PostApiClient {
    private final HttpClientAdapter adapter;
    private final TraceIdProvider traceIdProvider;
    private final Executor asyncExecutor;
    private final boolean traceEnabled;
    private final String traceHeaderName;

    public DefaultPostApiClient(HttpClientAdapter adapter) {
        this(adapter, new DefaultTraceIdProvider(), ForkJoinPool.commonPool(), true, "X-Trace-Id");
    }

    public DefaultPostApiClient(
            HttpClientAdapter adapter,
            TraceIdProvider traceIdProvider,
            Executor asyncExecutor,
            boolean traceEnabled,
            String traceHeaderName
    ) {
        this.adapter = adapter;
        this.traceIdProvider = traceIdProvider;
        this.asyncExecutor = asyncExecutor;
        this.traceEnabled = traceEnabled;
        this.traceHeaderName = traceHeaderName;
    }

    @Override
    public ApiResponse execute(ApiRequest.Builder requestBuilder) {
        return execute(requestBuilder.build());
    }

    @Override
    public ApiResponse execute(ApiRequest request) {
        String traceId = traceEnabled ? traceIdProvider.nextTraceId(request) : null;
        ApiRequest tracedRequest = traceId == null ? request : request.withHeader(traceHeaderName, traceId);
        long startedAt = System.nanoTime();
        ApiResponse response = adapter.execute(tracedRequest);
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);
        return response.withTraceId(traceId).withElapsedMillis(elapsedMillis);
    }

    @Override
    public CompletableFuture<ApiResponse> executeAsync(ApiRequest.Builder requestBuilder) {
        return executeAsync(requestBuilder.build());
    }

    @Override
    public CompletableFuture<ApiResponse> executeAsync(ApiRequest request) {
        return CompletableFuture.supplyAsync(() -> execute(request), asyncExecutor);
    }

    @Override
    public void streamSse(ApiRequest.Builder requestBuilder, SseEventHandler handler) {
        streamSse(requestBuilder.build(), handler);
    }

    @Override
    public void streamSse(ApiRequest request, SseEventHandler handler) {
        adapter.streamSse(request, handler);
    }

    @Override
    public WebSocketSession openWebSocket(WebSocketRequest.Builder requestBuilder, WebSocketHandler handler) {
        return openWebSocket(requestBuilder.build(), handler);
    }

    @Override
    public WebSocketSession openWebSocket(WebSocketRequest request, WebSocketHandler handler) {
        return adapter.openWebSocket(request, handler);
    }
}
