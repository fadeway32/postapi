package com.fadeway32.postadmin.controller;

import com.alibaba.fastjson2.JSONObject;
import com.fadeway32.postadmin.dto.ApiDefinitionSaveRequest;
import com.fadeway32.postadmin.entity.ApiDefinition;
import com.fadeway32.postadmin.service.ApiDefinitionService;
import com.fadeway32.postadmin.service.CurrentUserService;
import com.fadeway32.postadmin.web.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping("/api/definitions")
public class ApiDefinitionController {
    private static final Logger log = LoggerFactory.getLogger(ApiDefinitionController.class);
    private final ApiDefinitionService apiDefinitionService;
    private final CurrentUserService currentUserService;

    public ApiDefinitionController(ApiDefinitionService apiDefinitionService, CurrentUserService currentUserService) {
        this.apiDefinitionService = apiDefinitionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public Result<List<ApiDefinition>> list() {
        log.info(JSONObject.toJSONString(apiDefinitionService.listByTenant(currentUserService.tenantId())));
        return Result.ok(apiDefinitionService.listByTenant(currentUserService.tenantId()));
    }

    @GetMapping("/{apiCode}")
    public Result<ApiDefinition> detail(@PathVariable String apiCode) {
        ApiDefinition api = apiDefinitionService.findByCode(currentUserService.tenantId(), apiCode);
        if (api == null) {
            throw new IllegalArgumentException("api definition not found");
        }
        return Result.ok(api);
    }

    @GetMapping("/{apiCode}/versions")
    public Result<List<ApiDefinition>> history(@PathVariable String apiCode) {
        return Result.ok(apiDefinitionService.history(currentUserService.tenantId(), apiCode));
    }

    @PostMapping
    public Result<ApiDefinition> create(@Valid @RequestBody ApiDefinitionSaveRequest request) {
        return Result.ok(apiDefinitionService.create(currentUserService.tenantId(), request));
    }

    @PutMapping("/{id}")
    public Result<ApiDefinition> update(@PathVariable Long id, @Valid @RequestBody ApiDefinitionSaveRequest request) {
        return Result.ok(apiDefinitionService.update(currentUserService.tenantId(), id, request));
    }

    @PostMapping("/{id}/copy")
    public Result<ApiDefinition> copy(@PathVariable Long id) {
        return Result.ok(apiDefinitionService.copy(currentUserService.tenantId(), id));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ApiDefinition api = apiDefinitionService.getById(id);
        if (api == null || !currentUserService.tenantId().equals(api.getTenantId())) {
            throw new IllegalArgumentException("api definition not found");
        }
        apiDefinitionService.removeById(id);
        return Result.ok(null);
    }
}
