package com.fadeway32.postapi.util;

import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.spi.PostApiClientFactories;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public final class PostApiUtils {
    private static final PostApiClient DEFAULT_CLIENT = PostApiClientFactories.create();

    private PostApiUtils() {
    }

    public static ApiResponse get(String url) {
        return DEFAULT_CLIENT.execute(ApiRequest.get(url));
    }

    public static ApiResponse get(PostApiClient client, String url, Map<String, ?> queryParams) {
        ApiRequest.Builder builder = ApiRequest.get(url);
        queryParams.forEach(builder::queryParam);
        return client.execute(builder);
    }

    public static ApiResponse postJson(String url, Object body) {
        return DEFAULT_CLIENT.execute(ApiRequest.post(url).json(body));
    }

    public static ApiResponse postJson(PostApiClient client, String url, Object body) {
        return client.execute(ApiRequest.post(url).json(body));
    }

    public static CompletableFuture<ApiResponse> postJsonAsync(PostApiClient client, String url, Object body) {
        return client.executeAsync(ApiRequest.post(url).json(body));
    }

    public static ApiResponse postXml(PostApiClient client, String url, String xml) {
        return client.execute(ApiRequest.post(url).xml(xml));
    }

    public static ApiResponse postForm(PostApiClient client, String url, Map<String, ?> formFields) {
        ApiRequest.Builder builder = ApiRequest.post(url);
        formFields.forEach(builder::form);
        return client.execute(builder);
    }

    public static ApiResponse upload(PostApiClient client, String url, String fieldName, Path file) {
        return client.execute(ApiRequest.post(url).multipart().file(fieldName, file));
    }

    public static ApiResponse postBinary(PostApiClient client, String url, byte[] bytes, String contentType) {
        return client.execute(ApiRequest.post(url).binary(bytes, contentType));
    }

    public static ApiResponse postProtobuf(PostApiClient client, String url, byte[] bytes) {
        return client.execute(ApiRequest.post(url).protobuf(bytes));
    }

    public static ApiResponse postKryo(PostApiClient client, String url, Object value) {
        return client.execute(ApiRequest.post(url).kryo(ProtocolCodecUtils.toKryoBytes(value)));
    }
}
