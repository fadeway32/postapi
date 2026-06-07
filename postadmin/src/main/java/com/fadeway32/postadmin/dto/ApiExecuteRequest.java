package com.fadeway32.postadmin.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class ApiExecuteRequest {
    private Map<String, Object> payload = new LinkedHashMap<String, Object>();

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload == null ? new LinkedHashMap<String, Object>() : payload;
    }
}
