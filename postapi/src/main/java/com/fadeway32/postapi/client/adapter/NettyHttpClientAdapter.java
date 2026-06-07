package com.fadeway32.postapi.client.adapter;

import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.BodyType;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.HttpMethod;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.http.client.HttpClient.RequestSender;
import reactor.netty.http.client.HttpClient.ResponseReceiver;

public class NettyHttpClientAdapter implements HttpClientAdapter {
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration defaultTimeout;

    public NettyHttpClientAdapter(HttpClient httpClient, ObjectMapper objectMapper, Duration defaultTimeout) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.defaultTimeout = defaultTimeout;
    }

    @Override
    public HttpClientVendor vendor() {
        return HttpClientVendor.NETTY;
    }

    @Override
    public ApiResponse execute(ApiRequest request) {
        RequestBodyData bodyData = RequestBodyEncoder.encode(request, objectMapper);
        HttpClient configured = httpClient.headers(headers -> {
            request.headers().forEach((name, values) -> values.forEach(value -> headers.add(name, value)));
            if (request.bodyType() != BodyType.NONE && bodyData.contentType() != null) {
                headers.set("Content-Type", bodyData.contentType());
            }
        }).responseTimeout(request.responseTimeout() == null ? defaultTimeout : request.responseTimeout());
        try {
            RequestSender sender = configured
                    .request(HttpMethod.valueOf(request.method().name()))
                    .uri(buildUrl(request));
            ResponseReceiver<?> receiver = sender;
            if (request.bodyType() != BodyType.NONE) {
                receiver = sender.send(Mono.just(Unpooled.wrappedBuffer(bodyData.bytes())));
            }
            return receiver
                    .responseSingle((response, content) -> content.asByteArray()
                            .defaultIfEmpty(new byte[0])
                            .map(bytes -> new ApiResponse(
                                    response.status().code(),
                                    response.status().reasonPhrase(),
                                    headers(response.responseHeaders()),
                                    new String(bytes, StandardCharsets.UTF_8),
                                    bytes)))
                    .block(request.responseTimeout() == null ? defaultTimeout : request.responseTimeout());
        } catch (Exception ex) {
            throw new PostApiException("Netty request failed: " + request.method() + " " + request.uri(), ex);
        }
    }

    private String buildUrl(ApiRequest request) {
        StringBuilder url = new StringBuilder(request.uri().toString());
        if (request.queryParams().isEmpty()) {
            return url.toString();
        }
        url.append(request.uri().getQuery() == null ? '?' : '&');
        request.queryParams().forEach((name, values) -> values.forEach(value -> {
            if (url.charAt(url.length() - 1) != '?' && url.charAt(url.length() - 1) != '&') {
                url.append('&');
            }
            url.append(encode(name)).append('=').append(encode(value));
        }));
        return url.toString();
    }

    private Map<String, List<String>> headers(io.netty.handler.codec.http.HttpHeaders responseHeaders) {
        Map<String, List<String>> headers = new LinkedHashMap<>();
        responseHeaders.entries().forEach(entry ->
                headers.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>()).add(entry.getValue()));
        return headers;
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new PostApiException("UTF-8 encoding is not supported", ex);
        }
    }
}
