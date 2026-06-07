package com.fadeway32.postapi.spi;

import com.fadeway32.postapi.model.HttpClientVendor;
import java.time.Duration;

public class PostApiClientOptions {
    private HttpClientVendor clientType = HttpClientVendor.NETTY;
    private Duration connectTimeout = Duration.ofSeconds(3);
    private Duration responseTimeout = Duration.ofSeconds(30);
    private Duration keepAlive = Duration.ofMinutes(5);
    private int maxTotal = 200;
    private int maxPerRoute = 50;
    private int maxIdleConnections = 50;
    private boolean traceEnabled = true;
    private String traceHeaderName = "X-Trace-Id";
    private int asyncCorePoolSize = 8;
    private int asyncMaxPoolSize = 64;
    private int asyncQueueCapacity = 1000;

    public HttpClientVendor getClientType() {
        return clientType;
    }

    public PostApiClientOptions setClientType(HttpClientVendor clientType) {
        this.clientType = clientType;
        return this;
    }

    public Duration getConnectTimeout() {
        return connectTimeout;
    }

    public PostApiClientOptions setConnectTimeout(Duration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public Duration getResponseTimeout() {
        return responseTimeout;
    }

    public PostApiClientOptions setResponseTimeout(Duration responseTimeout) {
        this.responseTimeout = responseTimeout;
        return this;
    }

    public Duration getKeepAlive() {
        return keepAlive;
    }

    public PostApiClientOptions setKeepAlive(Duration keepAlive) {
        this.keepAlive = keepAlive;
        return this;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public PostApiClientOptions setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
        return this;
    }

    public int getMaxPerRoute() {
        return maxPerRoute;
    }

    public PostApiClientOptions setMaxPerRoute(int maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
        return this;
    }

    public int getMaxIdleConnections() {
        return maxIdleConnections;
    }

    public PostApiClientOptions setMaxIdleConnections(int maxIdleConnections) {
        this.maxIdleConnections = maxIdleConnections;
        return this;
    }

    public boolean isTraceEnabled() {
        return traceEnabled;
    }

    public PostApiClientOptions setTraceEnabled(boolean traceEnabled) {
        this.traceEnabled = traceEnabled;
        return this;
    }

    public String getTraceHeaderName() {
        return traceHeaderName;
    }

    public PostApiClientOptions setTraceHeaderName(String traceHeaderName) {
        this.traceHeaderName = traceHeaderName;
        return this;
    }

    public int getAsyncCorePoolSize() {
        return asyncCorePoolSize;
    }

    public PostApiClientOptions setAsyncCorePoolSize(int asyncCorePoolSize) {
        this.asyncCorePoolSize = asyncCorePoolSize;
        return this;
    }

    public int getAsyncMaxPoolSize() {
        return asyncMaxPoolSize;
    }

    public PostApiClientOptions setAsyncMaxPoolSize(int asyncMaxPoolSize) {
        this.asyncMaxPoolSize = asyncMaxPoolSize;
        return this;
    }

    public int getAsyncQueueCapacity() {
        return asyncQueueCapacity;
    }

    public PostApiClientOptions setAsyncQueueCapacity(int asyncQueueCapacity) {
        this.asyncQueueCapacity = asyncQueueCapacity;
        return this;
    }
}
