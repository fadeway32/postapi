package com.fadeway32.postadmin.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "postadmin")
public class PostAdminProperties {
    private final Crypto crypto = new Crypto();
    private final Bootstrap bootstrap = new Bootstrap();

    public Crypto getCrypto() {
        return crypto;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public static class Crypto {
        private String keyBase64;
        private String ivBase64;

        public String getKeyBase64() {
            return keyBase64;
        }

        public void setKeyBase64(String keyBase64) {
            this.keyBase64 = keyBase64;
        }

        public String getIvBase64() {
            return ivBase64;
        }

        public void setIvBase64(String ivBase64) {
            this.ivBase64 = ivBase64;
        }
    }

    public static class Bootstrap {
        private String tenantCode = "demo";
        private String tenantName = "Demo Tenant";
        private String username = "admin";
        private String password = "admin123";

        public String getTenantCode() {
            return tenantCode;
        }

        public void setTenantCode(String tenantCode) {
            this.tenantCode = tenantCode;
        }

        public String getTenantName() {
            return tenantName;
        }

        public void setTenantName(String tenantName) {
            this.tenantName = tenantName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
