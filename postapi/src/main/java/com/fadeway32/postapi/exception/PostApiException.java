package com.fadeway32.postapi.exception;

public class PostApiException extends RuntimeException {
    public PostApiException(String message) {
        super(message);
    }

    public PostApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
