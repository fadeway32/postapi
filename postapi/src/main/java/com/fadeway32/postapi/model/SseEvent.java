package com.fadeway32.postapi.model;

public final class SseEvent {
    private final String id;
    private final String event;
    private final String data;
    private final Long retry;

    public SseEvent(String id, String event, String data, Long retry) {
        this.id = id;
        this.event = event;
        this.data = data;
        this.retry = retry;
    }

    public String id() {
        return id;
    }

    public String event() {
        return event;
    }

    public String data() {
        return data;
    }

    public Long retry() {
        return retry;
    }
}
