package com.fadeway32.postapi.strategy.method;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.HttpMethod;
import java.net.URI;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.core5.http.ClassicHttpRequest;

public class GetMethodStrategy implements HttpMethodStrategy {
    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.GET == method;
    }

    @Override
    public ClassicHttpRequest create(URI uri, ApiRequest request) {
        return new HttpGet(uri);
    }
}
