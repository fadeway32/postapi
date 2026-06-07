package com.fadeway32.postapi.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class WebSocketRequest {
    private final URI uri;
    private final Map<String, List<String>> headers;

    private WebSocketRequest(Builder builder) {
        this.uri = builder.uri;
        Map<String, List<String>> copy = new LinkedHashMap<>();
        builder.headers.forEach((key, value) -> copy.put(key, Collections.unmodifiableList(new ArrayList<>(value))));
        this.headers = Collections.unmodifiableMap(copy);
    }

    public static Builder ws(String uri) {
        return new Builder(URI.create(uri));
    }

    public URI uri() {
        return uri;
    }

    public Map<String, List<String>> headers() {
        return headers;
    }

    public static final class Builder {
        private final URI uri;
        private final Map<String, List<String>> headers = new LinkedHashMap<>();

        private Builder(URI uri) {
            this.uri = uri;
        }

        public Builder header(String name, Object value) {
            headers.computeIfAbsent(name, ignored -> new ArrayList<>()).add(String.valueOf(value));
            return this;
        }

        public WebSocketRequest build() {
            return new WebSocketRequest(this);
        }
    }
}
