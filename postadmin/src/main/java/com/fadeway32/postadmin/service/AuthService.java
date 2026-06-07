package com.fadeway32.postadmin.service;

import cn.dev33.satoken.stp.StpUtil;
import com.fadeway32.postadmin.dto.LoginRequest;
import com.fadeway32.postadmin.entity.AdminUser;
import com.fadeway32.postadmin.entity.Tenant;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AuthService {
    private final TenantService tenantService;
    private final AdminUserService adminUserService;
    private final PasswordService passwordService;

    public AuthService(TenantService tenantService, AdminUserService adminUserService, PasswordService passwordService) {
        this.tenantService = tenantService;
        this.adminUserService = adminUserService;
        this.passwordService = passwordService;
    }

    public Map<String, Object> login(LoginRequest request) {
        Tenant tenant = tenantService.findEnabledByCode(request.getTenantCode());
        if (tenant == null) {
            throw new IllegalArgumentException("tenant not found or disabled");
        }
        AdminUser user = adminUserService.findEnabled(tenant.getId(), request.getUsername());
        if (user == null || !passwordService.matches(tenant.getId(), request.getUsername(), request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("invalid username or password");
        }
        StpUtil.login(user.getId());
        StpUtil.getSession().set("tenantId", tenant.getId());
        StpUtil.getSession().set("tenantCode", tenant.getCode());
        StpUtil.getSession().set("username", user.getUsername());

        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("tokenInfo", StpUtil.getTokenInfo());
        result.put("tenantId", tenant.getId());
        result.put("tenantCode", tenant.getCode());
        result.put("username", user.getUsername());
        return result;
    }

    public Map<String, Object> me() {
        Map<String, Object> result = new LinkedHashMap<String, Object>();
        result.put("userId", StpUtil.getLoginIdAsLong());
        result.put("tenantId", StpUtil.getSession().get("tenantId"));
        result.put("tenantCode", StpUtil.getSession().get("tenantCode"));
        result.put("username", StpUtil.getSession().get("username"));
        return result;
    }
}
