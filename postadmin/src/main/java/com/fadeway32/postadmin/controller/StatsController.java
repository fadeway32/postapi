package com.fadeway32.postadmin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fadeway32.postadmin.entity.ApiCallLog;
import com.fadeway32.postadmin.service.ApiExecutionService;
import com.fadeway32.postadmin.service.CurrentUserService;
import com.fadeway32.postadmin.service.StatsService;
import com.fadeway32.postadmin.web.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    private final StatsService statsService;
    private final ApiExecutionService apiExecutionService;
    private final CurrentUserService currentUserService;

    public StatsController(StatsService statsService,
                           ApiExecutionService apiExecutionService,
                           CurrentUserService currentUserService) {
        this.statsService = statsService;
        this.apiExecutionService = apiExecutionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/top")
    public Result<List<Map<String, Object>>> top(@RequestParam(defaultValue = "10") int limit) {
        return Result.ok(statsService.topApis(currentUserService.tenantId(), limit));
    }

    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> groups() {
        return Result.ok(statsService.groupStats(currentUserService.tenantId()));
    }

    @GetMapping("/logs")
    public Result<Page<ApiCallLog>> logs(@RequestParam(defaultValue = "1") int page,
                                         @RequestParam(defaultValue = "20") int size,
                                         @RequestParam(required = false) String apiCode) {
        return Result.ok(apiExecutionService.logs(currentUserService.tenantId(), page, size, apiCode));
    }
}
