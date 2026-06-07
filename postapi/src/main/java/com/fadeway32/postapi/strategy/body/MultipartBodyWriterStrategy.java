package com.fadeway32.postapi.strategy.body;

import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.BodyType;
import com.fadeway32.postapi.model.UploadFile;
import java.nio.charset.StandardCharsets;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;

public class MultipartBodyWriterStrategy implements BodyWriterStrategy {
    @Override
    public boolean supports(BodyType bodyType) {
        return BodyType.MULTIPART == bodyType;
    }

    @Override
    public void write(ClassicHttpRequest request, ApiRequest apiRequest) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setCharset(StandardCharsets.UTF_8);
        apiRequest.multipartFields().forEach((name, value) ->
                builder.addTextBody(name, value, ContentType.TEXT_PLAIN.withCharset(StandardCharsets.UTF_8)));
        for (UploadFile file : apiRequest.files()) {
            ContentType contentType = resolveContentType(file.contentType());
            if (file.path() != null) {
                builder.addBinaryBody(file.fieldName(), file.path().toFile(), contentType, file.fileName());
            } else {
                builder.addBinaryBody(file.fieldName(), file.content(), contentType, file.fileName());
            }
        }
        request.setEntity(builder.build());
    }

    private ContentType resolveContentType(String contentType) {
        return contentType == null || contentType.trim().isEmpty()
                ? ContentType.APPLICATION_OCTET_STREAM
                : ContentType.parse(contentType);
    }
}
