package com.fadeway32.postapi.client;

import com.fadeway32.postapi.model.SseEvent;

@FunctionalInterface
public interface SseEventHandler {
    void onEvent(SseEvent event);
}
