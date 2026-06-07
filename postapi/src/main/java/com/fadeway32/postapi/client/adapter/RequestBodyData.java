package com.fadeway32.postapi.client.adapter;

final class RequestBodyData {
    private final byte[] bytes;
    private final String contentType;

    RequestBodyData(byte[] bytes, String contentType) {
        this.bytes = bytes == null ? new byte[0] : bytes.clone();
        this.contentType = contentType;
    }

    byte[] bytes() {
        return bytes.clone();
    }

    String contentType() {
        return contentType;
    }
}
