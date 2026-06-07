package com.fadeway32.postapi.strategy.method;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.HttpMethod;
import java.net.URI;
import org.apache.hc.client5.http.classic.methods.HttpDelete;
import org.apache.hc.core5.http.ClassicHttpRequest;

public class DeleteMethodStrategy implements HttpMethodStrategy {
    @Override
    public boolean supports(HttpMethod method) {
        return HttpMethod.DELETE == method;
    }

    @Override
    public ClassicHttpRequest create(URI uri, ApiRequest request) {
        return new HttpDelete(uri);
    }
}
