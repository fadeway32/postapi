package com.fadeway32.postapi.model;

import java.nio.file.Path;
import java.util.Objects;

public final class UploadFile {
    private final String fieldName;
    private final String fileName;
    private final String contentType;
    private final Path path;
    private final byte[] content;

    private UploadFile(String fieldName, String fileName, String contentType, Path path, byte[] content) {
        this.fieldName = Objects.requireNonNull(fieldName, "fieldName must not be null");
        this.fileName = Objects.requireNonNull(fileName, "fileName must not be null");
        this.contentType = contentType;
        this.path = path;
        this.content = content;
    }

    public static UploadFile fromPath(String fieldName, Path path) {
        Objects.requireNonNull(path, "path must not be null");
        Path fileName = path.getFileName();
        return new UploadFile(fieldName, fileName == null ? "file" : fileName.toString(), null, path, null);
    }

    public static UploadFile fromPath(String fieldName, Path path, String contentType) {
        Objects.requireNonNull(path, "path must not be null");
        Path fileName = path.getFileName();
        return new UploadFile(fieldName, fileName == null ? "file" : fileName.toString(), contentType, path, null);
    }

    public static UploadFile fromBytes(String fieldName, String fileName, byte[] content, String contentType) {
        Objects.requireNonNull(content, "content must not be null");
        return new UploadFile(fieldName, fileName, contentType, null, content.clone());
    }

    public String fieldName() {
        return fieldName;
    }

    public String fileName() {
        return fileName;
    }

    public String contentType() {
        return contentType;
    }

    public Path path() {
        return path;
    }

    public byte[] content() {
        return content == null ? null : content.clone();
    }
}
