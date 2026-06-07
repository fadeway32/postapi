package com.fadeway32.postapi.autoconfigure;

import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.client.DefaultPostApiClient;
import com.fadeway32.postapi.client.DefaultTraceIdProvider;
import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.client.PostApiThreadFactory;
import com.fadeway32.postapi.client.TraceIdProvider;
import com.fadeway32.postapi.client.adapter.ApacheHttpClientAdapter;
import com.fadeway32.postapi.client.adapter.HutoolHttpClientAdapter;
import com.fadeway32.postapi.client.adapter.NettyHttpClientAdapter;
import com.fadeway32.postapi.client.adapter.OkHttpClientAdapter;
import com.fadeway32.postapi.exception.PostApiException;
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
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import io.netty.channel.ChannelOption;
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
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@AutoConfiguration
@ConditionalOnProperty(prefix = "postapi", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(PostApiProperties.class)
public class PostApiAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public PoolingHttpClientConnectionManager postApiConnectionManager(PostApiProperties properties) {
        ConnectionConfig connectionConfig = ConnectionConfig.custom()
                .setConnectTimeout(toTimeout(properties.getConnectTimeout()))
                .build();
        return PoolingHttpClientConnectionManagerBuilder.create()
                .setMaxConnTotal(properties.getMaxTotal())
                .setMaxConnPerRoute(properties.getMaxPerRoute())
                .setDefaultConnectionConfig(connectionConfig)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CloseableHttpClient postApiHttpClient(
            PoolingHttpClientConnectionManager connectionManager,
            PostApiProperties properties
    ) {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectionRequestTimeout(toTimeout(properties.getConnectionRequestTimeout()))
                .setResponseTimeout(toTimeout(properties.getResponseTimeout()))
                .build();
        return HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setDefaultRequestConfig(requestConfig)
                .setUserAgent(properties.getUserAgent())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public OkHttpClient postApiOkHttpClient(PostApiProperties properties) {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(properties.getMaxTotal());
        dispatcher.setMaxRequestsPerHost(properties.getMaxPerRoute());
        ConnectionPool connectionPool = new ConnectionPool(
                properties.getMaxIdleConnections(),
                properties.getKeepAlive().toMillis(),
                TimeUnit.MILLISECONDS
        );
        return new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .connectTimeout(properties.getConnectTimeout())
                .callTimeout(properties.getResponseTimeout())
                .readTimeout(properties.getResponseTimeout())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConnectionProvider postApiNettyConnectionProvider(PostApiProperties properties) {
        return ConnectionProvider.builder("postapi-netty")
                .maxConnections(properties.getMaxTotal())
                .pendingAcquireMaxCount(properties.getAsyncQueueCapacity())
                .maxIdleTime(properties.getKeepAlive())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClient postApiNettyHttpClient(ConnectionProvider postApiNettyConnectionProvider, PostApiProperties properties) {
        return HttpClient.create(postApiNettyConnectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, Math.toIntExact(properties.getConnectTimeout().toMillis()))
                .responseTimeout(properties.getResponseTimeout())
                .compress(true)
                .keepAlive(true);
    }

    @Bean
    @ConditionalOnMissingBean(name = "postApiAsyncExecutor")
    public Executor postApiAsyncExecutor(PostApiProperties properties) {
        return new ThreadPoolExecutor(
                properties.getAsyncCorePoolSize(),
                properties.getAsyncMaxPoolSize(),
                60,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getAsyncQueueCapacity()),
                new PostApiThreadFactory("postapi-async-"),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceIdProvider traceIdProvider() {
        return new DefaultTraceIdProvider();
    }

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper postApiObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    @ConditionalOnMissingBean(GetMethodStrategy.class)
    public GetMethodStrategy getMethodStrategy() {
        return new GetMethodStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(PostMethodStrategy.class)
    public PostMethodStrategy postMethodStrategy() {
        return new PostMethodStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(PutMethodStrategy.class)
    public PutMethodStrategy putMethodStrategy() {
        return new PutMethodStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(DeleteMethodStrategy.class)
    public DeleteMethodStrategy deleteMethodStrategy() {
        return new DeleteMethodStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(JsonBodyWriterStrategy.class)
    public JsonBodyWriterStrategy jsonBodyWriterStrategy(ObjectMapper postApiObjectMapper) {
        return new JsonBodyWriterStrategy(postApiObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(XmlBodyWriterStrategy.class)
    public XmlBodyWriterStrategy xmlBodyWriterStrategy() {
        return new XmlBodyWriterStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(FormUrlEncodedBodyWriterStrategy.class)
    public FormUrlEncodedBodyWriterStrategy formUrlEncodedBodyWriterStrategy() {
        return new FormUrlEncodedBodyWriterStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(MultipartBodyWriterStrategy.class)
    public MultipartBodyWriterStrategy multipartBodyWriterStrategy() {
        return new MultipartBodyWriterStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(BinaryBodyWriterStrategy.class)
    public BinaryBodyWriterStrategy binaryBodyWriterStrategy() {
        return new BinaryBodyWriterStrategy();
    }

    @Bean
    @ConditionalOnMissingBean(ApacheHttpClientAdapter.class)
    public ApacheHttpClientAdapter apacheHttpClientAdapter(
            CloseableHttpClient postApiHttpClient,
            List<HttpMethodStrategy> methodStrategies,
            List<BodyWriterStrategy> bodyWriterStrategies
    ) {
        return new ApacheHttpClientAdapter(postApiHttpClient, methodStrategies, bodyWriterStrategies);
    }

    @Bean
    @ConditionalOnMissingBean(OkHttpClientAdapter.class)
    public OkHttpClientAdapter okHttpClientAdapter(OkHttpClient postApiOkHttpClient, ObjectMapper postApiObjectMapper) {
        return new OkHttpClientAdapter(postApiOkHttpClient, postApiObjectMapper);
    }

    @Bean
    @ConditionalOnMissingBean(HutoolHttpClientAdapter.class)
    public HutoolHttpClientAdapter hutoolHttpClientAdapter(ObjectMapper postApiObjectMapper, PostApiProperties properties) {
        return new HutoolHttpClientAdapter(postApiObjectMapper, properties.getResponseTimeout());
    }

    @Bean
    @ConditionalOnMissingBean(NettyHttpClientAdapter.class)
    public NettyHttpClientAdapter nettyHttpClientAdapter(
            HttpClient postApiNettyHttpClient,
            ObjectMapper postApiObjectMapper,
            PostApiProperties properties
    ) {
        return new NettyHttpClientAdapter(postApiNettyHttpClient, postApiObjectMapper, properties.getResponseTimeout());
    }

    @Bean
    @Primary
    public HttpClientAdapter postApiHttpClientAdapter(
            PostApiProperties properties,
            List<HttpClientAdapter> adapters
    ) {
        HttpClientVendor clientType = properties.getClientType();
        return adapters.stream()
                .filter(adapter -> adapter.vendor() == clientType)
                .findFirst()
                .orElseThrow(() -> new PostApiException("No PostApi HTTP adapter found for client type: " + clientType));
    }

    @Bean
    @ConditionalOnMissingBean
    public PostApiClient postApiClient(
            HttpClientAdapter postApiHttpClientAdapter,
            TraceIdProvider traceIdProvider,
            Executor postApiAsyncExecutor,
            PostApiProperties properties
    ) {
        return new DefaultPostApiClient(
                postApiHttpClientAdapter,
                traceIdProvider,
                postApiAsyncExecutor,
                properties.isTraceEnabled(),
                properties.getTraceHeaderName()
        );
    }

    private Timeout toTimeout(java.time.Duration duration) {
        return Timeout.ofMilliseconds(duration.toMillis());
    }
}
