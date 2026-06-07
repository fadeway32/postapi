package com.fadeway32.postapi.client.adapter;

import com.fadeway32.postapi.client.SseEventHandler;
import com.fadeway32.postapi.model.SseEvent;
import java.io.BufferedReader;
import java.io.IOException;

final class SseParser {
    private SseParser() {
    }

    static void read(BufferedReader reader, SseEventHandler handler) throws IOException {
        String id = null;
        String event = null;
        StringBuilder data = new StringBuilder();
        Long retry = null;
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty()) {
                if (data.length() > 0) {
                    handler.onEvent(new SseEvent(id, event, stripTrailingNewline(data), retry));
                    data.setLength(0);
                    event = null;
                }
                continue;
            }
            if (line.startsWith(":")) {
                continue;
            }
            int separator = line.indexOf(':');
            String field = separator >= 0 ? line.substring(0, separator) : line;
            String value = separator >= 0 ? stripLeading(line.substring(separator + 1)) : "";
            switch (field) {
                case "id":
                    id = value;
                    break;
                case "event":
                    event = value;
                    break;
                case "data":
                    data.append(value).append('\n');
                    break;
                case "retry":
                    retry = parseRetry(value);
                    break;
                default:
                    break;
            }
        }
    }

    private static String stripTrailingNewline(StringBuilder data) {
        if (data.length() > 0 && data.charAt(data.length() - 1) == '\n') {
            return data.substring(0, data.length() - 1);
        }
        return data.toString();
    }

    private static String stripLeading(String value) {
        int index = 0;
        while (index < value.length() && Character.isWhitespace(value.charAt(index))) {
            index++;
        }
        return value.substring(index);
    }

    private static Long parseRetry(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
