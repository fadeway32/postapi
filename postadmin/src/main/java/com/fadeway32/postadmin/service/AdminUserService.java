package com.fadeway32.postadmin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fadeway32.postadmin.entity.AdminUser;
import com.fadeway32.postadmin.mapper.AdminUserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AdminUserService extends ServiceImpl<AdminUserMapper, AdminUser> {
    private final PasswordService passwordService;

    public AdminUserService(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    public AdminUser findEnabled(Long tenantId, String username) {
        return getOne(new LambdaQueryWrapper<AdminUser>()
                .eq(AdminUser::getTenantId, tenantId)
                .eq(AdminUser::getUsername, username)
                .eq(AdminUser::getEnabled, true), false);
    }

    public AdminUser create(Long tenantId, String username, String password) {
        LocalDateTime now = LocalDateTime.now();
        AdminUser user = new AdminUser();
        user.setTenantId(tenantId);
        user.setUsername(username);
        user.setPasswordHash(passwordService.hash(tenantId, username, password));
        user.setEnabled(true);
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        save(user);
        return user;
    }
}
