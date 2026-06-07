package com.fadeway32.postadmin.controller;

import com.fadeway32.postadmin.dto.ApiExecuteRequest;
import com.fadeway32.postadmin.dto.ApiExecutionResult;
import com.fadeway32.postadmin.dto.BatchExecuteRequest;
import com.fadeway32.postadmin.dto.BatchExecutionResult;
import com.fadeway32.postadmin.service.ApiExecutionService;
import com.fadeway32.postadmin.service.CurrentUserService;
import com.fadeway32.postadmin.web.Result;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/runtime")
public class ApiRuntimeController {
    private final ApiExecutionService apiExecutionService;
    private final CurrentUserService currentUserService;

    public ApiRuntimeController(ApiExecutionService apiExecutionService, CurrentUserService currentUserService) {
        this.apiExecutionService = apiExecutionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/{apiCode}/simulate")
    public Result<ApiExecutionResult> simulate(@PathVariable String apiCode, @RequestBody ApiExecuteRequest request) {
        return Result.ok(apiExecutionService.simulate(currentUserService.tenantId(), apiCode, request.getPayload()));
    }

    @PostMapping("/{apiCode}/execute")
    public Result<ApiExecutionResult> execute(@PathVariable String apiCode, @RequestBody ApiExecuteRequest request) {
        return Result.ok(apiExecutionService.execute(currentUserService.tenantId(), apiCode, request.getPayload()));
    }

    @PostMapping("/batch")
    public Result<BatchExecutionResult> batch(@Valid @RequestBody BatchExecuteRequest request) {
        return Result.ok(apiExecutionService.batch(currentUserService.tenantId(), request));
    }
}
