package com.fadeway32.postadmin.dto;

import javax.validation.constraints.NotBlank;
import java.util.Map;

public class ApiDefinitionSaveRequest {
    private Long groupId;
    @NotBlank(message = "apiCode must not be blank")
    private String apiCode;
    private String version;
    @NotBlank(message = "apiName must not be blank")
    private String apiName;
    @NotBlank(message = "method must not be blank")
    private String method;
    @NotBlank(message = "url must not be blank")
    private String url;
    private Map<String, Object> headers;
    private Map<String, Object> query;
    private String bodyType;
    private String bodyTemplate;
    private String scriptText;
    private Map<String, Object> secret;
    private Long timeoutMillis;
    private Boolean enabled;

    public Long getGroupId() {
        return groupId;
    }

    public void setGroupId(Long groupId) {
        this.groupId = groupId;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    public Map<String, Object> getQuery() {
        return query;
    }

    public void setQuery(Map<String, Object> query) {
        this.query = query;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getBodyTemplate() {
        return bodyTemplate;
    }

    public void setBodyTemplate(String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public String getScriptText() {
        return scriptText;
    }

    public void setScriptText(String scriptText) {
        this.scriptText = scriptText;
    }

    public Map<String, Object> getSecret() {
        return secret;
    }

    public void setSecret(Map<String, Object> secret) {
        this.secret = secret;
    }

    public Long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(Long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
