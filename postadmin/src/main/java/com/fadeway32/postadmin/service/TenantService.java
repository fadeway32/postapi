package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fadeway32.postadmin.dto.TenantSaveRequest;
import com.fadeway32.postadmin.entity.Tenant;
import com.fadeway32.postadmin.mapper.TenantMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TenantService extends ServiceImpl<TenantMapper, Tenant> {
    public Tenant findEnabledByCode(String code) {
        return getOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, code)
                .eq(Tenant::getEnabled, true), false);
    }

    public Tenant create(TenantSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Tenant tenant = new Tenant();
        tenant.setCode(request.getCode());
        tenant.setName(request.getName());
        tenant.setEnabled(request.getEnabled() == null || request.getEnabled());
        tenant.setCreatedAt(now);
        tenant.setUpdatedAt(now);
        save(tenant);
        return tenant;
    }

    public Tenant updateTenant(Long id, TenantSaveRequest request) {
        Tenant tenant = getById(id);
        if (tenant == null) {
            throw new IllegalArgumentException("tenant not found");
        }
        tenant.setCode(request.getCode());
        tenant.setName(request.getName());
        tenant.setEnabled(request.getEnabled() == null || request.getEnabled());
        tenant.setUpdatedAt(LocalDateTime.now());
        updateById(tenant);
        return tenant;
    }
}
