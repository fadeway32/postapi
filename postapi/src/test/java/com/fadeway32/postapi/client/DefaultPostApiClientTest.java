package com.fadeway32.postapi.client;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.ApiResponse;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fadeway32.postapi.spi.PostApiClientFactories;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultPostApiClientTest {
    private static final PostApiClient DEFAULT_CLIENT = PostApiClientFactories.create();

    @Test
    void addsTraceIdAndElapsedMillis() {
        CapturingAdapter adapter = new CapturingAdapter();
        PostApiClient client = new DefaultPostApiClient(adapter, ignored -> "trace-001", Runnable::run, true, "X-Trace-Id");

        ApiResponse response = client.execute(ApiRequest.get("https://example.com"));

        assertThat(adapter.traceHeader()).isEqualTo("trace-001");
        assertThat(response.traceId()).isEqualTo("trace-001");
        assertThat(response.elapsedMillis()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void executesAsyncWithConfiguredExecutor() throws ExecutionException, InterruptedException {
        CapturingAdapter adapter = new CapturingAdapter();
        Executor directExecutor = Runnable::run;
        PostApiClient client = new DefaultPostApiClient(adapter, ignored -> "trace-async", directExecutor, true, "X-Trace-Id");

        CompletableFuture<ApiResponse> future = client.executeAsync(ApiRequest.get("https://api.931109.xyz/setup/status"));

        assertThat(future).isCompletedWithValueMatching(response -> "trace-async".equals(response.traceId()));
    }

    @Test
    void testDefaultApi() {
//        ApiResponse execute = DEFAULT_CLIENT.execute(ApiRequest.get("https://api.931109.xyz/setup/status"));
        ApiResponse execute1 = DEFAULT_CLIENT.execute(ApiRequest.post("https://operate.lifeapp.pingan.com.cn/open-platform/external/microApp/document/getContentByDirectory").json("{\"tabId\":\"8611479a30654fc7b4de5a2d9d4b144e\",\"menuId\":\"088073c21e9c491f803aa0e0062dac09\",\"directoryId\":\"ce995830547c4cd8b38151d1d9b5b5f1\"}"));
        System.out.println(execute1.elapsedMillis());
//        System.out.println(execute.elapsedMillis());

//        assertThat(execute.toJson()).startsWith("{");
//        assertThat(execute.toString()).isEqualTo(execute.toJson());
    }

    private static final class CapturingAdapter implements HttpClientAdapter {
        private String traceHeader;

        @Override
        public HttpClientVendor vendor() {
            return HttpClientVendor.OKHTTP;
        }

        @Override
        public ApiResponse execute(ApiRequest request) {
            traceHeader = request.headers().get("X-Trace-Id").get(0);
            return new ApiResponse(200, "OK", Collections.emptyMap(), "{\"code\":0}");
        }

        private String traceHeader() {
            return traceHeader;
        }
    }
}
