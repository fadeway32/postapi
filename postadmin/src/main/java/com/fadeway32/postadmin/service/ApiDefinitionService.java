package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fadeway32.postadmin.dto.ApiDefinitionSaveRequest;
import com.fadeway32.postadmin.entity.ApiDefinition;
import com.fadeway32.postadmin.mapper.ApiDefinitionMapper;
import com.fadeway32.postadmin.util.Jsons;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ApiDefinitionService extends ServiceImpl<ApiDefinitionMapper, ApiDefinition> {
    private final Jsons jsons;
    private final SensitiveCryptoService sensitiveCryptoService;

    public ApiDefinitionService(Jsons jsons, SensitiveCryptoService sensitiveCryptoService) {
        this.jsons = jsons;
        this.sensitiveCryptoService = sensitiveCryptoService;
    }

    public List<ApiDefinition> listByTenant(Long tenantId) {
        return baseMapper.selectLatestByTenant(tenantId);
    }

    public List<ApiDefinition> history(Long tenantId, String apiCode) {
        return list(new LambdaQueryWrapper<ApiDefinition>()
                .eq(ApiDefinition::getTenantId, tenantId)
                .eq(ApiDefinition::getApiCode, apiCode)
                .orderByDesc(ApiDefinition::getCreatedAt));
    }

    public ApiDefinition findByCode(Long tenantId, String apiCode) {
        List<ApiDefinition> rows = list(new LambdaQueryWrapper<ApiDefinition>()
                .eq(ApiDefinition::getTenantId, tenantId)
                .eq(ApiDefinition::getApiCode, apiCode)
                .orderByDesc(ApiDefinition::getCreatedAt));
        return rows.isEmpty() ? null : rows.get(0);
    }

    public ApiDefinition create(Long tenantId, ApiDefinitionSaveRequest request) {
        String version = normalizeVersion(request.getVersion(), "v1");
        if (findByCodeAndVersion(tenantId, request.getApiCode(), version) != null) {
            throw new IllegalArgumentException("apiCode version already exists");
        }
        LocalDateTime now = LocalDateTime.now();
        ApiDefinition api = new ApiDefinition();
        api.setTenantId(tenantId);
        fill(api, request, true);
        api.setVersion(version);
        api.setCallCount(0L);
        api.setSuccessCount(0L);
        api.setFailureCount(0L);
        api.setCreatedAt(now);
        api.setUpdatedAt(now);
        save(api);
        return api;
    }

    public ApiDefinition update(Long tenantId, Long id, ApiDefinitionSaveRequest request) {
        ApiDefinition api = getById(id);
        if (api == null || !tenantId.equals(api.getTenantId())) {
            throw new IllegalArgumentException("api definition not found");
        }
        String version = nextVersion(tenantId, request.getApiCode());
        ApiDefinition exists = findByCodeAndVersion(tenantId, request.getApiCode(), version);
        if (exists != null) {
            throw new IllegalArgumentException("apiCode version already exists");
        }
        ApiDefinition next = new ApiDefinition();
        next.setTenantId(tenantId);
        fill(next, request, false);
        if (request.getSecret() == null) {
            next.setEncryptedSecretJson(api.getEncryptedSecretJson());
        }
        next.setVersion(version);
        next.setCallCount(0L);
        next.setSuccessCount(0L);
        next.setFailureCount(0L);
        next.setLastCallTime(null);
        LocalDateTime now = LocalDateTime.now();
        next.setCreatedAt(now);
        next.setUpdatedAt(now);
        save(next);
        return next;
    }

    public ApiDefinition copy(Long tenantId, Long id) {
        ApiDefinition source = getById(id);
        if (source == null || !tenantId.equals(source.getTenantId())) {
            throw new IllegalArgumentException("api definition not found");
        }
        LocalDateTime now = LocalDateTime.now();
        ApiDefinition copied = new ApiDefinition();
        copied.setTenantId(tenantId);
        copied.setGroupId(source.getGroupId());
        copied.setApiCode(nextCopyCode(tenantId, source.getApiCode()));
        copied.setVersion("v1");
        copied.setApiName(nextCopyName(tenantId, source.getApiName()));
        copied.setMethod(source.getMethod());
        copied.setUrl(source.getUrl());
        copied.setHeadersJson(source.getHeadersJson());
        copied.setQueryJson(source.getQueryJson());
        copied.setBodyType(source.getBodyType());
        copied.setBodyTemplate(source.getBodyTemplate());
        copied.setScriptText(source.getScriptText());
        copied.setEncryptedSecretJson(source.getEncryptedSecretJson());
        copied.setTimeoutMillis(source.getTimeoutMillis());
        copied.setEnabled(source.getEnabled());
        copied.setCallCount(0L);
        copied.setSuccessCount(0L);
        copied.setFailureCount(0L);
        copied.setLastCallTime(null);
        copied.setCreatedAt(now);
        copied.setUpdatedAt(now);
        save(copied);
        return copied;
    }

    public void increaseStats(Long apiId, boolean success) {
        baseMapper.increaseCallStats(apiId, success ? 1 : 0, success ? 0 : 1);
    }

    private String nextCopyCode(Long tenantId, String sourceCode) {
        String base = (sourceCode == null || sourceCode.trim().isEmpty() ? "api" : sourceCode.trim()) + "_copy";
        String candidate = base;
        int index = 2;
        while (findByCode(tenantId, candidate) != null) {
            candidate = base + "_" + index;
            index++;
        }
        return candidate;
    }

    private ApiDefinition findByCodeAndVersion(Long tenantId, String apiCode, String version) {
        return getOne(new LambdaQueryWrapper<ApiDefinition>()
                .eq(ApiDefinition::getTenantId, tenantId)
                .eq(ApiDefinition::getApiCode, apiCode)
                .eq(ApiDefinition::getVersion, version), false);
    }

    private String nextVersion(Long tenantId, String apiCode) {
        int maxVersion = 0;
        List<ApiDefinition> versions = history(tenantId, apiCode);
        for (ApiDefinition item : versions) {
            maxVersion = Math.max(maxVersion, versionNumber(item.getVersion()));
        }
        return "v" + (maxVersion + 1);
    }

    private String normalizeVersion(String version, String fallback) {
        if (version == null || version.trim().isEmpty()) {
            return fallback;
        }
        String value = version.trim().toLowerCase();
        return value.startsWith("v") ? value : "v" + value;
    }

    private int versionNumber(String version) {
        String value = normalizeVersion(version, "v1").substring(1);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            return 1;
        }
    }

    private String nextCopyName(Long tenantId, String sourceName) {
        String base = (sourceName == null || sourceName.trim().isEmpty() ? "接口" : sourceName.trim()) + " 副本";
        String candidate = base;
        int index = 2;
        while (existsName(tenantId, candidate)) {
            candidate = base + " " + index;
            index++;
        }
        return candidate;
    }

    private boolean existsName(Long tenantId, String apiName) {
        return count(new LambdaQueryWrapper<ApiDefinition>()
                .eq(ApiDefinition::getTenantId, tenantId)
                .eq(ApiDefinition::getApiName, apiName)) > 0;
    }

    private void fill(ApiDefinition api, ApiDefinitionSaveRequest request, boolean create) {
        api.setGroupId(request.getGroupId());
        api.setApiCode(request.getApiCode());
        api.setApiName(request.getApiName());
        api.setMethod(request.getMethod().toUpperCase());
        api.setUrl(request.getUrl());
        Map<String, Object> headers = request.getHeaders();
        if (headers == null || !headers.containsKey("Content-Type") || !headers.containsKey("content-type")) {
            if (headers == null) {
                headers = new HashMap<>();
            }
            headers.put("Content-Type", "application/json; charset=utf-8");
        }
        api.setHeadersJson(jsons.write(headers));
        api.setQueryJson(jsons.write(request.getQuery()));
        api.setBodyType(request.getBodyType() == null ? "NONE" : request.getBodyType().toUpperCase());
        api.setBodyTemplate(request.getBodyTemplate());
        api.setScriptText(request.getScriptText());
        api.setTimeoutMillis(request.getTimeoutMillis());
        api.setEnabled(request.getEnabled() == null || request.getEnabled());
        if (request.getSecret() != null) {
            api.setEncryptedSecretJson(sensitiveCryptoService.encrypt(jsons.write(request.getSecret())));
        } else if (create) {
            api.setEncryptedSecretJson(null);
        }
    }
}
