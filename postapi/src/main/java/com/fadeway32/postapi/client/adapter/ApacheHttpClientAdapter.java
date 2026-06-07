package com.fadeway32.postapi.client.adapter;

import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.client.SseEventHandler;
import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fadeway32.postapi.strategy.body.BodyWriterStrategy;
import com.fadeway32.postapi.strategy.method.HttpMethodStrategy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.hc.core5.util.Timeout;

public class ApacheHttpClientAdapter implements HttpClientAdapter {
    private final CloseableHttpClient httpClient;
    private final List<HttpMethodStrategy> methodStrategies;
    private final List<BodyWriterStrategy> bodyWriterStrategies;

    public ApacheHttpClientAdapter(
            CloseableHttpClient httpClient,
            List<HttpMethodStrategy> methodStrategies,
            List<BodyWriterStrategy> bodyWriterStrategies
    ) {
        this.httpClient = httpClient;
        this.methodStrategies = new ArrayList<>(methodStrategies);
        this.bodyWriterStrategies = new ArrayList<>(bodyWriterStrategies);
    }

    @Override
    public HttpClientVendor vendor() {
        return HttpClientVendor.APACHE;
    }

    @Override
    public ApiResponse execute(ApiRequest request) {
        ClassicHttpRequest httpRequest = createHttpRequest(request);
        applyHeaders(httpRequest, request);
        applyRequestConfig(httpRequest, request);
        writeBody(httpRequest, request);
        try {
            return httpClient.execute(httpRequest, response -> {
                try {
                    return toApiResponse(response);
                } finally {
                    response.close();
                }
            });
        } catch (IOException ex) {
            throw new PostApiException("HTTP request failed: " + request.method() + " " + request.uri(), ex);
        }
    }

    @Override
    public void streamSse(ApiRequest request, SseEventHandler handler) {
        ClassicHttpRequest httpRequest = createHttpRequest(request);
        applyHeaders(httpRequest, request);
        httpRequest.addHeader("Accept", "text/event-stream");
        applyRequestConfig(httpRequest, request);
        try {
            httpClient.execute(httpRequest, response -> {
                try {
                    readSse(response, handler);
                    return null;
                } finally {
                    response.close();
                }
            });
        } catch (IOException ex) {
            throw new PostApiException("SSE request failed: " + request.uri(), ex);
        }
    }

    private ClassicHttpRequest createHttpRequest(ApiRequest request) {
        URI uri = buildUri(request);
        return methodStrategies.stream()
                .filter(strategy -> strategy.supports(request.method()))
                .findFirst()
                .orElseThrow(() -> new PostApiException("Unsupported HTTP method: " + request.method()))
                .create(uri, request);
    }

    private URI buildUri(ApiRequest request) {
        try {
            URIBuilder builder = new URIBuilder(request.uri());
            request.queryParams().forEach((name, values) ->
                    values.forEach(value -> builder.addParameter(name, value)));
            return builder.build();
        } catch (Exception ex) {
            throw new PostApiException("Failed to build request URI: " + request.uri(), ex);
        }
    }

    private void applyHeaders(ClassicHttpRequest httpRequest, ApiRequest request) {
        request.headers().forEach((name, values) ->
                values.forEach(value -> httpRequest.addHeader(name, value)));
    }

    private void applyRequestConfig(ClassicHttpRequest httpRequest, ApiRequest request) {
        Duration responseTimeout = request.responseTimeout();
        if (responseTimeout == null || !(httpRequest instanceof HttpUriRequestBase)) {
            return;
        }
        HttpUriRequestBase requestBase = (HttpUriRequestBase) httpRequest;
        requestBase.setConfig(RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(responseTimeout.toMillis()))
                .build());
    }

    private void writeBody(ClassicHttpRequest httpRequest, ApiRequest request) {
        bodyWriterStrategies.stream()
                .filter(strategy -> strategy.supports(request.bodyType()))
                .findFirst()
                .ifPresent(strategy -> strategy.write(httpRequest, request));
    }

    private ApiResponse toApiResponse(ClassicHttpResponse response) throws IOException {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        for (Header header : response.getHeaders()) {
            headers.computeIfAbsent(header.getName(), ignored -> new ArrayList<>()).add(header.getValue());
        }
        HttpEntity entity = response.getEntity();
        byte[] bytes = entity == null ? new byte[0] : EntityUtils.toByteArray(entity);
        String body = new String(bytes, StandardCharsets.UTF_8);
        return new ApiResponse(response.getCode(), response.getReasonPhrase(), headers, body, bytes);
    }

    private void readSse(ClassicHttpResponse response, SseEventHandler handler) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
            SseParser.read(reader, handler);
        }
    }
}
