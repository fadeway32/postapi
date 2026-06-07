package com.fadeway32.postapi.strategy.body;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.BodyType;
import org.apache.hc.core5.http.ClassicHttpRequest;

public interface BodyWriterStrategy {
    boolean supports(BodyType bodyType);

    void write(ClassicHttpRequest request, ApiRequest apiRequest);
}
