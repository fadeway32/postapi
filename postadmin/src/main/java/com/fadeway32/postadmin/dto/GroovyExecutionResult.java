package com.fadeway32.postadmin.dto;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroovyExecutionResult {

    private boolean success;
    private boolean timeout;
    private Object returnValue;
    private Map<String, Object> bindings = new LinkedHashMap<String, Object>();
    private String errorType;
    private String errorMessage;
    private long elapsedMillis;
    private boolean securityManagerRequested;
    private boolean securityManagerActive;
    private String securityManagerMessage;
    private List<String> allowedImports;
    private List<String> blockedImports;
    private List<String> blockedReceivers;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public Object getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(Object returnValue) {
        this.returnValue = returnValue;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
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

    public boolean isSecurityManagerRequested() {
        return securityManagerRequested;
    }

    public void setSecurityManagerRequested(boolean securityManagerRequested) {
        this.securityManagerRequested = securityManagerRequested;
    }

    public boolean isSecurityManagerActive() {
        return securityManagerActive;
    }

    public void setSecurityManagerActive(boolean securityManagerActive) {
        this.securityManagerActive = securityManagerActive;
    }

    public String getSecurityManagerMessage() {
        return securityManagerMessage;
    }

    public void setSecurityManagerMessage(String securityManagerMessage) {
        this.securityManagerMessage = securityManagerMessage;
    }

    public List<String> getAllowedImports() {
        return allowedImports;
    }

    public void setAllowedImports(List<String> allowedImports) {
        this.allowedImports = allowedImports;
    }

    public List<String> getBlockedImports() {
        return blockedImports;
    }

    public void setBlockedImports(List<String> blockedImports) {
        this.blockedImports = blockedImports;
    }

    public List<String> getBlockedReceivers() {
        return blockedReceivers;
    }

    public void setBlockedReceivers(List<String> blockedReceivers) {
        this.blockedReceivers = blockedReceivers;
    }
}
