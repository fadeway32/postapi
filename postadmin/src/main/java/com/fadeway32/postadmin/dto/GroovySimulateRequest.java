package com.fadeway32.postadmin.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GroovySimulateRequest {

    @NotBlank(message = "script is required")
    @Size(max = 20000, message = "script length must be <= 20000")
    private String script;

    @Size(max = 50, message = "bindings size must be <= 50")
    private Map<String, Object> bindings = new LinkedHashMap<String, Object>();

    @Min(value = 100, message = "timeoutMillis must be >= 100")
    @Max(value = 10000, message = "timeoutMillis must be <= 10000")
    private Integer timeoutMillis = 2000;

    @Size(max = 64, message = "allowedImports size must be <= 64")
    private List<String> allowedImports = new ArrayList<String>();

    @Size(max = 64, message = "blockedImports size must be <= 64")
    private List<String> blockedImports = new ArrayList<String>();

    @Size(max = 64, message = "blockedReceivers size must be <= 64")
    private List<String> blockedReceivers = new ArrayList<String>();

    private Boolean installSecurityManager = Boolean.TRUE;

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public Map<String, Object> getBindings() {
        return bindings;
    }

    public void setBindings(Map<String, Object> bindings) {
        this.bindings = bindings == null ? new LinkedHashMap<String, Object>() : bindings;
    }

    public Integer getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(Integer timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public List<String> getAllowedImports() {
        return allowedImports;
    }

    public void setAllowedImports(List<String> allowedImports) {
        this.allowedImports = allowedImports == null ? new ArrayList<String>() : allowedImports;
    }

    public List<String> getBlockedImports() {
        return blockedImports;
    }

    public void setBlockedImports(List<String> blockedImports) {
        this.blockedImports = blockedImports == null ? new ArrayList<String>() : blockedImports;
    }

    public List<String> getBlockedReceivers() {
        return blockedReceivers;
    }

    public void setBlockedReceivers(List<String> blockedReceivers) {
        this.blockedReceivers = blockedReceivers == null ? new ArrayList<String>() : blockedReceivers;
    }

    public Boolean getInstallSecurityManager() {
        return installSecurityManager;
    }

    public void setInstallSecurityManager(Boolean installSecurityManager) {
        this.installSecurityManager = installSecurityManager;
    }
}
