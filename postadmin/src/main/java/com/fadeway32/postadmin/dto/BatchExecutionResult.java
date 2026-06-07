package com.fadeway32.postadmin.dto;

import java.util.ArrayList;
import java.util.List;

public class BatchExecutionResult {
    private String batchId;
    private int total;
    private int success;
    private int failure;
    private List<ApiExecutionResult> results = new ArrayList<ApiExecutionResult>();

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public List<ApiExecutionResult> getResults() {
        return results;
    }

    public void setResults(List<ApiExecutionResult> results) {
        this.results = results;
    }
}
