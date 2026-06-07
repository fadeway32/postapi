package com.fadeway32.postadmin.controller;

import com.fadeway32.postadmin.dto.TenantSaveRequest;
import com.fadeway32.postadmin.entity.Tenant;
import com.fadeway32.postadmin.service.TenantService;
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
@RequestMapping("/admin/tenants")
public class TenantController {
    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public Result<List<Tenant>> list() {
        return Result.ok(tenantService.list());
    }

    @PostMapping
    public Result<Tenant> create(@Valid @RequestBody TenantSaveRequest request) {
        return Result.ok(tenantService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Tenant> update(@PathVariable Long id, @Valid @RequestBody TenantSaveRequest request) {
        return Result.ok(tenantService.updateTenant(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tenantService.removeById(id);
        return Result.ok(null);
    }
}
