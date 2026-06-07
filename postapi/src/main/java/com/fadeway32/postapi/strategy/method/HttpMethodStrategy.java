package com.fadeway32.postapi.strategy.method;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.HttpMethod;
import java.net.URI;
import org.apache.hc.core5.http.ClassicHttpRequest;

public interface HttpMethodStrategy {
    boolean supports(HttpMethod method);

    ClassicHttpRequest create(URI uri, ApiRequest request);
}
