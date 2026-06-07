package com.fadeway32.postadmin.controller;

import com.fadeway32.postadmin.dto.ApiGroupSaveRequest;
import com.fadeway32.postadmin.entity.ApiGroup;
import com.fadeway32.postadmin.service.ApiGroupService;
import com.fadeway32.postadmin.service.CurrentUserService;
import com.fadeway32.postadmin.web.Result;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class ApiGroupController {
    private final ApiGroupService apiGroupService;
    private final CurrentUserService currentUserService;

    public ApiGroupController(ApiGroupService apiGroupService, CurrentUserService currentUserService) {
        this.apiGroupService = apiGroupService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Result<List<ApiGroup>> list() {
        return Result.ok(apiGroupService.listByTenant(currentUserService.tenantId()));
    }

    @PostMapping
    public Result<ApiGroup> create(@Valid @RequestBody ApiGroupSaveRequest request) {
        return Result.ok(apiGroupService.create(currentUserService.tenantId(), request));
    }

    @PutMapping("/{id}")
    public Result<ApiGroup> update(@PathVariable Long id, @Valid @RequestBody ApiGroupSaveRequest request) {
        return Result.ok(apiGroupService.update(currentUserService.tenantId(), id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ApiGroup group = apiGroupService.getById(id);
        if (group == null || !currentUserService.tenantId().equals(group.getTenantId())) {
            throw new IllegalArgumentException("api group not found");
        }
        apiGroupService.removeById(id);
        return Result.ok(null);
    }
}
