package com.fadeway32.postadmin.dto;

import javax.validation.constraints.NotBlank;

public class TenantSaveRequest {
    @NotBlank(message = "code must not be blank")
    private String code;
    @NotBlank(message = "name must not be blank")
    private String name;
    private Boolean enabled;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}
