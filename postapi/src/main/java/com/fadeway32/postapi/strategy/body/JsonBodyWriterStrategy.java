package com.fadeway32.postapi.strategy.body;

import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.BodyType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;

public class JsonBodyWriterStrategy implements BodyWriterStrategy {
    private final ObjectMapper objectMapper;

    public JsonBodyWriterStrategy(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(BodyType bodyType) {
        return BodyType.JSON == bodyType;
    }

    @Override
    public void write(ClassicHttpRequest request, ApiRequest apiRequest) {
        String json = toJson(apiRequest.body());
        request.setEntity(new StringEntity(json, ContentType.APPLICATION_JSON.withCharset(StandardCharsets.UTF_8)));
    }

    private String toJson(Object body) {
        if (body == null) {
            return "";
        }
        if (body instanceof String) {
            return (String) body;
        }
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException ex) {
            throw new PostApiException("Failed to serialize JSON request body", ex);
        }
    }
}
