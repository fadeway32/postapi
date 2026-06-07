package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fadeway32.postadmin.config.PostAdminProperties;
import com.fadeway32.postadmin.dto.TenantSaveRequest;
import com.fadeway32.postadmin.entity.AdminUser;
import com.fadeway32.postadmin.entity.Tenant;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class BootstrapDataInitializer implements CommandLineRunner {
    private final PostAdminProperties properties;
    private final TenantService tenantService;
    private final AdminUserService adminUserService;

    public BootstrapDataInitializer(PostAdminProperties properties,
                                    TenantService tenantService,
                                    AdminUserService adminUserService) {
        this.properties = properties;
        this.tenantService = tenantService;
        this.adminUserService = adminUserService;
    }

    @Override
    public void run(String... args) {
        Tenant tenant = tenantService.getOne(new LambdaQueryWrapper<Tenant>()
                .eq(Tenant::getCode, properties.getBootstrap().getTenantCode()), false);
        if (tenant == null) {
            TenantSaveRequest request = new TenantSaveRequest();
            request.setCode(properties.getBootstrap().getTenantCode());
            request.setName(properties.getBootstrap().getTenantName());
            request.setEnabled(true);
            tenant = tenantService.create(request);
        }
        AdminUser user = adminUserService.getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getTenantId, tenant.getId())
                .eq(AdminUser::getUsername, properties.getBootstrap().getUsername()), false);
        if (user == null) {
            adminUserService.create(tenant.getId(), properties.getBootstrap().getUsername(), properties.getBootstrap().getPassword());
        }
    }
}
