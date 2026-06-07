package com.fadeway32.postadmin.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateRenderer {
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([A-Za-z0-9_.-]+)}");

    public String render(String template, Map<String, Object> payload, Map<String, Object> secret) {
        if (template == null) {
            return null;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = resolve(key, payload, secret);
            matcher.appendReplacement(buffer, Matcher.quoteReplacement(value == null ? "" : String.valueOf(value)));
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    private Object resolve(String key, Map<String, Object> payload, Map<String, Object> secret) {
        if (key.startsWith("secret.")) {
            return nested(secret, key.substring("secret.".length()));
        }
        if (key.startsWith("payload.")) {
            return nested(payload, key.substring("payload.".length()));
        }
        Object value = nested(payload, key);
        return value == null ? nested(secret, key) : value;
    }

    @SuppressWarnings("unchecked")
    private Object nested(Map<String, Object> source, String path) {
        if (source == null || path == null) {
            return null;
        }
        Object current = source;
        String[] parts = path.split("\\.");
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
        }
        return current;
    }
}
