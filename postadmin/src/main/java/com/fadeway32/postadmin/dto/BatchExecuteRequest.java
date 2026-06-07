package com.fadeway32.postadmin.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BatchExecuteRequest {
    @Valid
    @NotEmpty(message = "items must not be empty")
    private List<Item> items = new ArrayList<Item>();
    private Boolean stopOnFailure = false;

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items == null ? new ArrayList<Item>() : items;
    }

    public Boolean getStopOnFailure() {
        return stopOnFailure;
    }

    public void setStopOnFailure(Boolean stopOnFailure) {
        this.stopOnFailure = stopOnFailure;
    }

    public static class Item {
        @NotBlank(message = "apiCode must not be blank")
        private String apiCode;
        private Map<String, Object> payload = new LinkedHashMap<String, Object>();

        public String getApiCode() {
            return apiCode;
        }

        public void setApiCode(String apiCode) {
            this.apiCode = apiCode;
        }

        public Map<String, Object> getPayload() {
            return payload;
        }

        public void setPayload(Map<String, Object> payload) {
            this.payload = payload == null ? new LinkedHashMap<String, Object>() : payload;
        }
    }
}
