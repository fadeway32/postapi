package com.fadeway32.postadmin.event;

import com.fadeway32.postadmin.entity.ApiDefinition;

import java.util.Map;

public class ApiExecutionCompletedEvent {

    private final ApiDefinition api;
    private final String batchId;
    private final Map<String, Object> payload;
    private final Map<String, Object> requestDetail;
    private final Integer statusCode;
    private final String responseBody;
    private final String traceId;
    private final String errorMessage;
    private final long elapsedMillis;
    private final boolean success;
    private final boolean simulate;
    private final boolean increaseStats;

    public ApiExecutionCompletedEvent(ApiDefinition api,
                                      String batchId,
                                      Map<String, Object> payload,
                                      Map<String, Object> requestDetail,
                                      Integer statusCode,
                                      String responseBody,
                                      String traceId,
                                      String errorMessage,
                                      long elapsedMillis,
                                      boolean success,
                                      boolean simulate,
                                      boolean increaseStats) {
        this.api = api;
        this.batchId = batchId;
        this.payload = payload;
        this.requestDetail = requestDetail;
        this.statusCode = statusCode;
        this.responseBody = responseBody;
        this.traceId = traceId;
        this.errorMessage = errorMessage;
        this.elapsedMillis = elapsedMillis;
        this.success = success;
        this.simulate = simulate;
        this.increaseStats = increaseStats;
    }

    public ApiDefinition getApi() {
        return api;
    }

    public String getBatchId() {
        return batchId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public Map<String, Object> getRequestDetail() {
        return requestDetail;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public String getTraceId() {
        return traceId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isSimulate() {
        return simulate;
    }

    public boolean isIncreaseStats() {
        return increaseStats;
    }
}
