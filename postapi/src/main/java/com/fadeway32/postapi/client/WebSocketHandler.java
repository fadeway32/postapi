package com.fadeway32.postapi.client;

public interface WebSocketHandler {
    default void onOpen(WebSocketSession session) {
    }

    default void onText(String text) {
    }

    default void onBinary(byte[] bytes) {
    }

    default void onClosed(int code, String reason) {
    }

    default void onError(Throwable throwable) {
    }
}
