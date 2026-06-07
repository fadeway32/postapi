package com.fadeway32.postapi.client;

public interface WebSocketSession {
    boolean sendText(String text);

    boolean sendBinary(byte[] bytes);

    boolean close(int code, String reason);
}
