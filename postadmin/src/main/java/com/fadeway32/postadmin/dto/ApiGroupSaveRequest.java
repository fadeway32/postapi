package com.fadeway32.postadmin.dto;

import javax.validation.constraints.NotBlank;

public class ApiGroupSaveRequest {
    @NotBlank(message = "name must not be blank")
    private String name;
    private Integer sortOrder;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }
}
