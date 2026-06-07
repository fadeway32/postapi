package com.fadeway32.postapi.client;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class PostApiThreadFactory implements ThreadFactory {
    private final AtomicInteger sequence = new AtomicInteger();
    private final String prefix;

    public PostApiThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable runnable) {
        Thread thread = new Thread(runnable, prefix + sequence.incrementAndGet());
        thread.setDaemon(true);
        return thread;
    }
}
