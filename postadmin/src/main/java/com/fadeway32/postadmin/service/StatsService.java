package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fadeway32.postadmin.entity.ApiDefinition;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StatsService {
    private final ApiDefinitionService apiDefinitionService;
    private final ApiGroupService apiGroupService;

    public StatsService(ApiDefinitionService apiDefinitionService, ApiGroupService apiGroupService) {
        this.apiDefinitionService = apiDefinitionService;
        this.apiGroupService = apiGroupService;
    }

    public List<Map<String, Object>> topApis(Long tenantId, int limit) {
        List<ApiDefinition> apis = apiDefinitionService.list(new LambdaQueryWrapper<ApiDefinition>()
                .eq(ApiDefinition::getTenantId, tenantId)
                .orderByDesc(ApiDefinition::getCallCount)
                .last("LIMIT " + Math.max(1, limit)));
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (ApiDefinition api : apis) {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("apiCode", api.getApiCode());
            row.put("apiName", api.getApiName());
            row.put("callCount", api.getCallCount());
            row.put("successCount", api.getSuccessCount());
            row.put("failureCount", api.getFailureCount());
            row.put("lastCallTime", api.getLastCallTime());
            rows.add(row);
        }
        return rows;
    }

    public List<Map<String, Object>> groupStats(Long tenantId) {
        Map<Long, Map<String, Object>> rows = new LinkedHashMap<Long, Map<String, Object>>();
        apiGroupService.listByTenant(tenantId).forEach(group -> {
            Map<String, Object> row = new LinkedHashMap<String, Object>();
            row.put("groupId", group.getId());
            row.put("groupName", group.getName());
            row.put("callCount", 0L);
            row.put("successCount", 0L);
            row.put("failureCount", 0L);
            rows.put(group.getId(), row);
        });
        List<ApiDefinition> apis = apiDefinitionService.listByTenant(tenantId);
        for (ApiDefinition api : apis) {
            Long groupId = api.getGroupId() == null ? 0L : api.getGroupId();
            Map<String, Object> row = rows.get(groupId);
            if (row == null) {
                row = new LinkedHashMap<String, Object>();
                row.put("groupId", groupId);
                row.put("groupName", groupId == 0L ? "未分组" : "未知分组");
                row.put("callCount", 0L);
                row.put("successCount", 0L);
                row.put("failureCount", 0L);
                rows.put(groupId, row);
            }
            row.put("callCount", ((Long) row.get("callCount")) + safe(api.getCallCount()));
            row.put("successCount", ((Long) row.get("successCount")) + safe(api.getSuccessCount()));
            row.put("failureCount", ((Long) row.get("failureCount")) + safe(api.getFailureCount()));
        }
        return new ArrayList<Map<String, Object>>(rows.values());
    }

    private long safe(Long value) {
        return value == null ? 0L : value;
    }
}
