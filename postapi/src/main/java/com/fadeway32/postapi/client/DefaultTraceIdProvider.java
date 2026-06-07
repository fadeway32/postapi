package com.fadeway32.postapi.client;

import com.fadeway32.postapi.model.ApiRequest;
import java.util.UUID;

public class DefaultTraceIdProvider implements TraceIdProvider {
    @Override
    public String nextTraceId(ApiRequest request) {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
