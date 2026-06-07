package com.fadeway32.postapi.spi;

import com.fadeway32.postapi.client.DefaultTraceIdProvider;
import com.fadeway32.postapi.client.DefaultPostApiClient;
import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.client.PostApiThreadFactory;
import com.fadeway32.postapi.client.adapter.ApacheHttpClientAdapter;
import com.fadeway32.postapi.client.adapter.HutoolHttpClientAdapter;
import com.fadeway32.postapi.client.adapter.NettyHttpClientAdapter;
import com.fadeway32.postapi.client.adapter.OkHttpClientAdapter;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fadeway32.postapi.strategy.body.BinaryBodyWriterStrategy;
import com.fadeway32.postapi.strategy.body.BodyWriterStrategy;
import com.fadeway32.postapi.strategy.body.FormUrlEncodedBodyWriterStrategy;
import com.fadeway32.postapi.strategy.body.JsonBodyWriterStrategy;
import com.fadeway32.postapi.strategy.body.MultipartBodyWriterStrategy;
import com.fadeway32.postapi.strategy.body.XmlBodyWriterStrategy;
import com.fadeway32.postapi.strategy.method.DeleteMethodStrategy;
import com.fadeway32.postapi.strategy.method.GetMethodStrategy;
import com.fadeway32.postapi.strategy.method.HttpMethodStrategy;
import com.fadeway32.postapi.strategy.method.PostMethodStrategy;
import com.fadeway32.postapi.strategy.method.PutMethodStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

public class DefaultPostApiClientFactory implements PostApiClientFactory {
    @Override
    public boolean supports(HttpClientVendor vendor) {
        return true;
    }

    @Override
    public PostApiClient create(PostApiClientOptions options) {
        ObjectMapper objectMapper = new ObjectMapper();
        HttpClientAdapter adapter;
        switch (options.getClientType()) {
            case APACHE:
                adapter = apacheAdapter(options, objectMapper);
                break;
            case OKHTTP:
                adapter = okHttpAdapter(options, objectMapper);
                break;
            case HUTOOL:
                adapter = new HutoolHttpClientAdapter(objectMapper, options.getResponseTimeout());
                break;
            case NETTY:
                adapter = nettyAdapter(options, objectMapper);
                break;
            default:
                adapter = okHttpAdapter(options, objectMapper);
                break;
        }
        return new DefaultPostApiClient(
                adapter,
                new DefaultTraceIdProvider(),
                asyncExecutor(options),
                options.isTraceEnabled(),
                options.getTraceHeaderName()
        );
    }

    private HttpClientAdapter apacheAdapter(PostApiClientOptions options, ObjectMapper objectMapper) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(Timeout.ofMilliseconds(options.getConnectTimeout().toMillis()))
                .build();
        PoolingHttpClientConnectionManager connectionManager = PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(options.getMaxTotal())
                .setMaxConnPerRoute(options.getMaxPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setResponseTimeout(Timeout.ofMilliseconds(options.getResponseTimeout().toMillis()))
                .build();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .build();
        List<HttpMethodStrategy> methodStrategies = Arrays.asList(
                new GetMethodStrategy(),
                new PostMethodStrategy(),
                new PutMethodStrategy(),
                new DeleteMethodStrategy()
        );
        List<BodyWriterStrategy> bodyStrategies = Arrays.asList(
                new JsonBodyWriterStrategy(objectMapper),
                new XmlBodyWriterStrategy(),
                new FormUrlEncodedBodyWriterStrategy(),
                new MultipartBodyWriterStrategy(),
                new BinaryBodyWriterStrategy()
        );
        return new ApacheHttpClientAdapter(httpClient, methodStrategies, bodyStrategies);
    }

    private HttpClientAdapter okHttpAdapter(PostApiClientOptions options, ObjectMapper objectMapper) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(options.getMaxTotal());
        dispatcher.setMaxRequestsPerHost(options.getMaxPerRoute());
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(new ConnectionPool(
                        options.getMaxIdleConnections(),
                        options.getKeepAlive().toMillis(),
                        TimeUnit.MILLISECONDS
                ))
                .connectTimeout(options.getConnectTimeout())
                .callTimeout(options.getResponseTimeout())
                .readTimeout(options.getResponseTimeout())
                .build();
        return new OkHttpClientAdapter(okHttpClient, objectMapper);
    }

    private HttpClientAdapter nettyAdapter(PostApiClientOptions options, ObjectMapper objectMapper) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("postapi-netty-spi")
                .maxConnections(options.getMaxTotal())
                .pendingAcquireMaxCount(options.getAsyncQueueCapacity())
                .maxIdleTime(options.getKeepAlive())
                .build();
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(options.getConnectTimeout().toMillis()))
                .responseTimeout(options.getResponseTimeout())
                .compress(true)
                .keepAlive(true);
        return new NettyHttpClientAdapter(httpClient, objectMapper, options.getResponseTimeout());
    }

    private Executor asyncExecutor(PostApiClientOptions options) {
        return new ThreadPoolExecutor(
                options.getAsyncCorePoolSize(),
                options.getAsyncMaxPoolSize(),
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(options.getAsyncQueueCapacity()),
                new PostApiThreadFactory("postapi-spi-async-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
