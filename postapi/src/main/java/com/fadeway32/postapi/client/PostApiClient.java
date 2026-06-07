package com.fadeway32.postapi.client;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.WebSocketRequest;
import java.util.concurrent.CompletableFuture;

public interface PostApiClient {
    ApiResponse execute(ApiRequest.Builder requestBuilder);

    ApiResponse execute(ApiRequest request);

    CompletableFuture<ApiResponse> executeAsync(ApiRequest.Builder requestBuilder);

    CompletableFuture<ApiResponse> executeAsync(ApiRequest request);

    void streamSse(ApiRequest.Builder requestBuilder, SseEventHandler handler);

    void streamSse(ApiRequest request, SseEventHandler handler);

    WebSocketSession openWebSocket(WebSocketRequest.Builder requestBuilder, WebSocketHandler handler);

    WebSocketSession openWebSocket(WebSocketRequest request, WebSocketHandler handler);
}
