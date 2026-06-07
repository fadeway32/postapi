package com.fadeway32.postadmin.service;

import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
    public Long userId() {
        return StpUtil.getLoginIdAsLong();
    }

    public Long tenantId() {
        Object tenantId = StpUtil.getSession().get("tenantId");
        if (tenantId instanceof Number) {
            return ((Number) tenantId).longValue();
        }
        if (tenantId instanceof String) {
            return Long.valueOf((String) tenantId);
        }
        throw new IllegalArgumentException("tenant context missing");
    }

    public String username() {
        Object username = StpUtil.getSession().get("username");
        return username == null ? null : String.valueOf(username);
    }
}
