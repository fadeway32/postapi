package com.fadeway32.postapi.client.adapter;

import com.fadeway32.postapi.exception.PostApiException;
import com.fadeway32.postapi.model.ApiRequest;
import com.fadeway32.postapi.model.UploadFile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

final class RequestBodyEncoder {
    private RequestBodyEncoder() {
    }

    static RequestBodyData encode(ApiRequest request, ObjectMapper objectMapper) {
        switch (request.bodyType()) {
            case NONE:
                return new RequestBodyData(new byte[0], "application/octet-stream");
            case JSON:
                return text(toJson(request.body(), objectMapper), "application/json; charset=utf-8");
            case XML:
                return text(request.body() == null ? "" : String.valueOf(request.body()), "application/xml; charset=utf-8");
            case FORM_URLENCODED:
                return text(form(request), "application/x-www-form-urlencoded; charset=utf-8");
            case MULTIPART:
                return multipart(request);
            case BINARY:
            case PROTOBUF:
            case KRYO:
                return binary(request);
            default:
                throw new PostApiException("Unsupported body type: " + request.bodyType());
        }
    }

    private static RequestBodyData text(String value, String contentType) {
        return new RequestBodyData(value.getBytes(StandardCharsets.UTF_8), contentType);
    }

    private static RequestBodyData binary(ApiRequest request) {
        Object body = request.body();
        byte[] bytes = body instanceof byte[] ? (byte[]) body : new byte[0];
        String contentType = request.contentType() == null ? "application/octet-stream" : request.contentType();
        return new RequestBodyData(bytes, contentType);
    }

    private static String form(ApiRequest request) {
        StringBuilder builder = new StringBuilder();
        request.formFields().forEach((name, value) -> {
            if (builder.length() > 0) {
                builder.append('&');
            }
            builder.append(encode(name)).append('=').append(encode(value));
        });
        return builder.toString();
    }

    private static RequestBodyData multipart(ApiRequest request) {
        String boundary = "postapi-" + UUID.randomUUID();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        request.multipartFields().forEach((name, value) -> {
            writeAscii(output, "--" + boundary + "\r\n");
            writeAscii(output, "Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n");
            writeAscii(output, value + "\r\n");
        });
        for (UploadFile file : request.files()) {
            writeAscii(output, "--" + boundary + "\r\n");
            writeAscii(output, "Content-Disposition: form-data; name=\"" + file.fieldName() + "\"; filename=\"" + file.fileName() + "\"\r\n");
            writeAscii(output, "Content-Type: " + (file.contentType() == null ? "application/octet-stream" : file.contentType()) + "\r\n\r\n");
            writeBytes(output, readFile(file));
            writeAscii(output, "\r\n");
        }
        writeAscii(output, "--" + boundary + "--\r\n");
        return new RequestBodyData(output.toByteArray(), "multipart/form-data; boundary=" + boundary);
    }

    private static byte[] readFile(UploadFile file) {
        if (file.content() != null) {
            return file.content();
        }
        try {
            return Files.readAllBytes(file.path());
        } catch (IOException ex) {
            throw new PostApiException("Failed to read upload file: " + file.path(), ex);
        }
    }

    private static String toJson(Object body, ObjectMapper objectMapper) {
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

    private static String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new PostApiException("UTF-8 encoding is not supported", ex);
        }
    }

    private static void writeAscii(ByteArrayOutputStream output, String value) {
        writeBytes(output, value.getBytes(StandardCharsets.UTF_8));
    }

    private static void writeBytes(ByteArrayOutputStream output, byte[] bytes) {
        try {
            output.write(bytes);
        } catch (IOException ex) {
            throw new PostApiException("Failed to write request body", ex);
        }
    }
}
