package com.fadeway32.postapi.client;

import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fadeway32.postapi.model.WebSocketRequest;

public interface HttpClientAdapter {
    HttpClientVendor vendor();

    ApiResponse execute(ApiRequest request);

    default void streamSse(ApiRequest request, SseEventHandler handler) {
        throw new PostApiException(vendor() + " adapter does not support SSE streaming");
    }

    default WebSocketSession openWebSocket(WebSocketRequest request, WebSocketHandler handler) {
        throw new PostApiException(vendor() + " adapter does not support WebSocket");
    }
}
