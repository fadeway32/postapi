package com.fadeway32.postadmin.service;

import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DigestAlgorithm;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class PasswordService {
    private final CryptoService cryptoService;

    public PasswordService(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    public String hash(Long tenantId, String username, String password) {
        String text = tenantId + ":" + username + ":" + password;
        return cryptoService.digestHex(DigestAlgorithm.SHA256, text.getBytes(StandardCharsets.UTF_8));
    }

    public boolean matches(Long tenantId, String username, String password, String hash) {
        return hash(tenantId, username, password).equalsIgnoreCase(hash);
    }
}
