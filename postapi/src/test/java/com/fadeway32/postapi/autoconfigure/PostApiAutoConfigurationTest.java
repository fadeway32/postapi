package com.fadeway32.postapi.autoconfigure;

import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.client.HttpClientAdapter;
import com.fadeway32.postapi.model.HttpClientVendor;
import com.fadeway32.postapi.spi.PostApiClientFactories;
import com.fadeway32.postapi.spi.PostApiClientOptions;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class PostApiAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(PostApiAutoConfiguration.class));

    @Test
    void autoConfiguresPostApiClient() {
        contextRunner.run(context -> assertThat(context)
                .hasSingleBean(PostApiClient.class)
                .hasSingleBean(CloseableHttpClient.class)
                .hasSingleBean(PostApiProperties.class));
    }

    @Test
    void selectsConfiguredClientAdapter() {
        contextRunner
                .withPropertyValues("postapi.client-type=apache")
                .run(context -> assertThat(context.getBean(HttpClientAdapter.class).vendor())
                        .isEqualTo(HttpClientVendor.APACHE));
    }

    @Test
    void selectsNettyClientAdapter() {
        contextRunner
                .withPropertyValues("postapi.client-type=netty")
                .run(context -> assertThat(context.getBean(HttpClientAdapter.class).vendor())
                        .isEqualTo(HttpClientVendor.NETTY));
    }

    @Test
    void createsClientThroughJavaSpi() {
        PostApiClient client = PostApiClientFactories.create(new PostApiClientOptions()
                .setClientType(HttpClientVendor.OKHTTP));

        assertThat(client).isNotNull();
    }

    @Test
    void backsOffWhenDisabled() {
        contextRunner
                .withPropertyValues("postapi.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(PostApiClient.class));
    }
}
