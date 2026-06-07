package com.fadeway32.postapi.strategy.body;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.BodyType;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class XmlBodyWriterStrategy implements BodyWriterStrategy {
    @Override
    public boolean supports(BodyType bodyType) {
        return BodyType.XML == bodyType;
    }

    @Override
    public void write(ClassicHttpRequest request, ApiRequest apiRequest) {
        Object body = apiRequest.body();
        String xml = body == null ? "" : String.valueOf(body);
        request.setEntity(new StringEntity(xml, ContentType.APPLICATION_XML.withCharset(StandardCharsets.UTF_8)));
    }
}
