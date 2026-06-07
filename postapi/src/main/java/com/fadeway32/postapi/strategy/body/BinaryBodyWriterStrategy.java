package com.fadeway32.postapi.strategy.body;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.BodyType;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

public class BinaryBodyWriterStrategy implements BodyWriterStrategy {
    @Override
    public boolean supports(BodyType bodyType) {
        return BodyType.BINARY == bodyType || BodyType.PROTOBUF == bodyType || BodyType.KRYO == bodyType;
    }

    @Override
    public void write(ClassicHttpRequest request, ApiRequest apiRequest) {
        Object body = apiRequest.body();
        byte[] bytes = body instanceof byte[] ? (byte[]) body : new byte[0];
        String contentType = apiRequest.contentType() == null ? "application/octet-stream" : apiRequest.contentType();
        request.setEntity(new ByteArrayEntity(bytes, ContentType.parse(contentType)));
    }
}
