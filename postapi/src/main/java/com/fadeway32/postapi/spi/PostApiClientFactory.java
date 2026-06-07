package com.fadeway32.postapi.spi;

import com.fadeway32.postapi.client.PostApiClient;
import com.fadeway32.postapi.model.HttpClientVendor;

public interface PostApiClientFactory {
    boolean supports(HttpClientVendor vendor);

    PostApiClient create(PostApiClientOptions options);
}
