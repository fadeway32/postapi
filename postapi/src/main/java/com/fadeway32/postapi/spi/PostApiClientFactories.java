package com.fadeway32.postapi.spi;

import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.exception.PostApiException;
import java.util.ServiceLoader;

public final class PostApiClientFactories {
    private PostApiClientFactories() {
    }

    public static PostApiClient create() {
        return create(new PostApiClientOptions());
    }

    public static PostApiClient create(PostApiClientOptions options) {
        for (PostApiClientFactory factory : ServiceLoader.load(PostApiClientFactory.class)) {
            if (factory.supports(options.getClientType())) {
                return factory.create(options);
            }
        }
        throw new PostApiException("No PostApiClientFactory found by Java SPI");
    }
}
