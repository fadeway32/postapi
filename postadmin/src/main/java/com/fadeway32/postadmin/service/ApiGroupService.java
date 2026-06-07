package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fadeway32.postadmin.dto.ApiGroupSaveRequest;
import com.fadeway32.postadmin.entity.ApiGroup;
import com.fadeway32.postadmin.mapper.ApiGroupMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApiGroupService extends ServiceImpl<ApiGroupMapper, ApiGroup> {
    public List<ApiGroup> listByTenant(Long tenantId) {
        return list(new LambdaQueryWrapper<ApiGroup>()
                .eq(ApiGroup::getTenantId, tenantId)
                .orderByAsc(ApiGroup::getSortOrder)
                .orderByDesc(ApiGroup::getCreatedAt));
    }

    public ApiGroup create(Long tenantId, ApiGroupSaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        ApiGroup group = new ApiGroup();
        group.setTenantId(tenantId);
        group.setName(request.getName());
        group.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        group.setCreatedAt(now);
        group.setUpdatedAt(now);
        save(group);
        return group;
    }

    public ApiGroup update(Long tenantId, Long id, ApiGroupSaveRequest request) {
        ApiGroup group = getById(id);
        if (group == null || !tenantId.equals(group.getTenantId())) {
            throw new IllegalArgumentException("api group not found");
        }
        group.setName(request.getName());
        group.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        group.setUpdatedAt(LocalDateTime.now());
        updateById(group);
        return group;
    }
}
