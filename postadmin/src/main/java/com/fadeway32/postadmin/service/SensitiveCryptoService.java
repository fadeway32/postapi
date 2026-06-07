package com.fadeway32.postadmin.service;

import com.fadeway32.postadmin.config.PostAdminProperties;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.SymmetricAlgorithm;
import com.fadeway32.crypto.util.CryptoCodecUtils;
import org.springframework.stereotype.Service;

@Service
public class SensitiveCryptoService {
    private final CryptoService cryptoService;
    private final byte[] key;
    private final byte[] iv;

    public SensitiveCryptoService(CryptoService cryptoService, PostAdminProperties properties) {
        this.cryptoService = cryptoService;
        this.key = CryptoCodecUtils.base64Decode(properties.getCrypto().getKeyBase64());
        this.iv = CryptoCodecUtils.base64Decode(properties.getCrypto().getIvBase64());
    }

    public String encrypt(String plaintext) {
        if (plaintext == null || plaintext.trim().isEmpty()) {
            return null;
        }
        return cryptoService.encryptToBase64(SymmetricAlgorithm.AES, key, iv, plaintext);
    }

    public String decrypt(String ciphertext) {
        if (ciphertext == null || ciphertext.trim().isEmpty()) {
            return null;
        }
        return cryptoService.decryptFromBase64(SymmetricAlgorithm.AES, key, iv, ciphertext);
    }
}
