package com.fadeway32.postadmin.dto;

import java.util.Map;

public class ApiExecutionResult {
    private String apiCode;
    private boolean success;
    private Integer statusCode;
    private String responseBody;
    private String errorMessage;
    private long elapsedMillis;
    private String traceId;
    private Map<String, Object> requestDetail;

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getElapsedMillis() {
        return elapsedMillis;
    }

    public void setElapsedMillis(long elapsedMillis) {
        this.elapsedMillis = elapsedMillis;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, Object> getRequestDetail() {
        return requestDetail;
    }

    public void setRequestDetail(Map<String, Object> requestDetail) {
        this.requestDetail = requestDetail;
    }
}
