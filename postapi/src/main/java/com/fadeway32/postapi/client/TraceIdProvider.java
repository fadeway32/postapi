package com.fadeway32.postapi.client;

import com.fadeway32.postapi.model.ApiRequest;

@FunctionalInterface
public interface TraceIdProvider {
    String nextTraceId(ApiRequest request);
}
