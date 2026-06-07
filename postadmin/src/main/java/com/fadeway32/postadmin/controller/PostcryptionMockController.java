package com.fadeway32.postadmin.controller;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DigestAlgorithm;
import com.fadeway32.crypto.core.HybridCiphertext;
import com.fadeway32.crypto.core.SymmetricAlgorithm;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("${postcryption.mock-controller.path:/postadmin/postcryption/mock}")
public class PostcryptionMockController {

    private static final String DEFAULT_PLAINTEXT = "postcryption mock payload";

    private final CryptoService cryptoService;

    public PostcryptionMockController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @PostMapping("/aes")
    public Map<String, Object> aes(@RequestBody SymmetricMockRequest request) {
        return symmetric(SymmetricAlgorithm.AES, request);
    }

    @PostMapping("/sm4")
    public Map<String, Object> sm4(@RequestBody SymmetricMockRequest request) {
        return symmetric(SymmetricAlgorithm.SM4, request);
    }

    @PostMapping("/des")
    public Map<String, Object> des(@RequestBody SymmetricMockRequest request) {
        return symmetric(SymmetricAlgorithm.DES, request);
    }

    @PostMapping("/des3")
    public Map<String, Object> des3(@RequestBody SymmetricMockRequest request) {
        return symmetric(SymmetricAlgorithm.DES3, request);
    }

    @PostMapping("/rsa")
    public Map<String, Object> rsa(@RequestBody AsymmetricMockRequest request) {
        return asymmetric(AsymmetricAlgorithm.RSA, request);
    }

    @PostMapping("/ecc")
    public Map<String, Object> ecc(@RequestBody AsymmetricMockRequest request) {
        return asymmetric(AsymmetricAlgorithm.ECC, request);
    }

    @PostMapping("/sm2")
    public Map<String, Object> sm2(@RequestBody AsymmetricMockRequest request) {
        return asymmetric(AsymmetricAlgorithm.SM2, request);
    }

    @PostMapping("/md5")
    public Map<String, Object> md5(@RequestBody DigestMockRequest request) {
        return digest(DigestAlgorithm.MD5, request);
    }

    @PostMapping("/sha256")
    public Map<String, Object> sha256(@RequestBody DigestMockRequest request) {
        return digest(DigestAlgorithm.SHA256, request);
    }

    @PostMapping("/sha3-256")
    public Map<String, Object> sha3256(@RequestBody DigestMockRequest request) {
        return digest(DigestAlgorithm.SHA3_256, request);
    }

    @PostMapping("/sm3")
    public Map<String, Object> sm3(@RequestBody DigestMockRequest request) {
        return digest(DigestAlgorithm.SM3, request);
    }

    @PostMapping("/hybrid/aes-rsa")
    public Map<String, Object> hybridAesRsa(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.AES, AsymmetricAlgorithm.RSA, request);
    }

    @PostMapping("/hybrid/aes-ecc")
    public Map<String, Object> hybridAesEcc(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.AES, AsymmetricAlgorithm.ECC, request);
    }

    @PostMapping("/hybrid/aes-sm2")
    public Map<String, Object> hybridAesSm2(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.AES, AsymmetricAlgorithm.SM2, request);
    }

    @PostMapping("/hybrid/sm4-rsa")
    public Map<String, Object> hybridSm4Rsa(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.SM4, AsymmetricAlgorithm.RSA, request);
    }

    @PostMapping("/hybrid/sm4-ecc")
    public Map<String, Object> hybridSm4Ecc(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.SM4, AsymmetricAlgorithm.ECC, request);
    }

    @PostMapping("/hybrid/sm4-sm2")
    public Map<String, Object> hybridSm4Sm2(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.SM4, AsymmetricAlgorithm.SM2, request);
    }

    @PostMapping("/hybrid/des-rsa")
    public Map<String, Object> hybridDesRsa(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.DES, AsymmetricAlgorithm.RSA, request);
    }

    @PostMapping("/hybrid/des-ecc")
    public Map<String, Object> hybridDesEcc(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.DES, AsymmetricAlgorithm.ECC, request);
    }

    @PostMapping("/hybrid/des-sm2")
    public Map<String, Object> hybridDesSm2(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.DES, AsymmetricAlgorithm.SM2, request);
    }

    @PostMapping("/hybrid/des3-rsa")
    public Map<String, Object> hybridDes3Rsa(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.DES3, AsymmetricAlgorithm.RSA, request);
    }

    @PostMapping("/hybrid/des3-ecc")
    public Map<String, Object> hybridDes3Ecc(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.DES3, AsymmetricAlgorithm.ECC, request);
    }

    @PostMapping("/hybrid/des3-sm2")
    public Map<String, Object> hybridDes3Sm2(@RequestBody HybridMockRequest request) {
        return hybrid(SymmetricAlgorithm.DES3, AsymmetricAlgorithm.SM2, request);
    }

    private Map<String, Object> symmetric(SymmetricAlgorithm algorithm, SymmetricMockRequest request) {
        try {
            SymmetricMockRequest payload = request == null ? new SymmetricMockRequest() : request;
            String plaintext = defaultText(payload.getPlaintext());
            byte[] key = hasText(payload.getKey())
                    ? Base64.getDecoder().decode(payload.getKey())
                    : cryptoService.generateSymmetricKey(algorithm).getEncoded();
            byte[] iv = hasText(payload.getIv())
                    ? Base64.getDecoder().decode(payload.getIv())
                    : cryptoService.randomIv(algorithm);
            String ciphertext = hasText(payload.getCiphertext())
                    ? payload.getCiphertext()
                    : cryptoService.encryptToBase64(algorithm, key, iv, plaintext);
            String decrypted = cryptoService.decryptFromBase64(algorithm, key, iv, ciphertext);
            if (!plaintext.equals(decrypted)) {
                return failure("SYMMETRIC", algorithm.name(), "validation failed: decrypted text does not match plaintext");
            }
            Map<String, Object> validation = new LinkedHashMap<String, Object>();
            validation.put("plaintext", plaintext);
            validation.put("decrypted", decrypted);
            validation.put("key", Base64.getEncoder().encodeToString(key));
            validation.put("iv", Base64.getEncoder().encodeToString(iv));
            validation.put("ciphertext", ciphertext);
            return success("SYMMETRIC", algorithm.name(), validation);
        } catch (Exception ex) {
            return failure("SYMMETRIC", algorithm.name(), ex.getMessage());
        }
    }

    private Map<String, Object> asymmetric(AsymmetricAlgorithm algorithm, AsymmetricMockRequest request) {
        try {
            AsymmetricMockRequest payload = request == null ? new AsymmetricMockRequest() : request;
            String plaintext = defaultText(payload.getPlaintext());
            KeyMaterial keyMaterial = asymmetricKeyMaterial(algorithm, payload.getPublicKey(), payload.getPrivateKey(),
                    hasText(payload.getCiphertext()));
            String ciphertext = hasText(payload.getCiphertext())
                    ? payload.getCiphertext()
                    : cryptoService.encryptToBase64(algorithm, keyMaterial.getPublicKey(), bytes(plaintext));
            String decrypted = new String(
                    cryptoService.decryptFromBase64(algorithm, keyMaterial.getPrivateKey(), ciphertext),
                    StandardCharsets.UTF_8);
            if (!plaintext.equals(decrypted)) {
                return failure("ASYMMETRIC", algorithm.name(), "validation failed: decrypted text does not match plaintext");
            }
            Map<String, Object> validation = new LinkedHashMap<String, Object>();
            validation.put("plaintext", plaintext);
            validation.put("decrypted", decrypted);
            if (keyMaterial.getPublicKey() != null) {
                validation.put("publicKey", cryptoService.encodePublicKey(keyMaterial.getPublicKey()));
            }
            validation.put("privateKey", cryptoService.encodePrivateKey(keyMaterial.getPrivateKey()));
            validation.put("ciphertext", ciphertext);
            return success("ASYMMETRIC", algorithm.name(), validation);
        } catch (Exception ex) {
            return failure("ASYMMETRIC", algorithm.name(), ex.getMessage());
        }
    }

    private Map<String, Object> digest(DigestAlgorithm algorithm, DigestMockRequest request) {
        try {
            DigestMockRequest payload = request == null ? new DigestMockRequest() : request;
            String plaintext = defaultText(payload.getPlaintext());
            String digestHex = cryptoService.digestHex(algorithm, bytes(plaintext));
            if (hasText(payload.getDigestHex()) && !digestHex.equalsIgnoreCase(payload.getDigestHex())) {
                return failure("DIGEST", algorithm.name(), "validation failed: digestHex does not match plaintext");
            }
            Map<String, Object> validation = new LinkedHashMap<String, Object>();
            validation.put("plaintext", plaintext);
            validation.put("digestHex", digestHex);
            validation.put("expectedDigestHex", hasText(payload.getDigestHex())
                    ? payload.getDigestHex().toLowerCase(Locale.ENGLISH) : digestHex);
            return success("DIGEST", algorithm.name(), validation);
        } catch (Exception ex) {
            return failure("DIGEST", algorithm.name(), ex.getMessage());
        }
    }

    private Map<String, Object> hybrid(SymmetricAlgorithm symmetricAlgorithm,
                                       AsymmetricAlgorithm asymmetricAlgorithm,
                                       HybridMockRequest request) {
        String algorithm = symmetricAlgorithm.name() + "+" + asymmetricAlgorithm.name();
        try {
            HybridMockRequest payload = request == null ? new HybridMockRequest() : request;
            String plaintext = defaultText(payload.getPlaintext());
            KeyMaterial keyMaterial = hybridKeyMaterial(asymmetricAlgorithm, payload);
            HybridCiphertext ciphertext = payload.getCiphertext() == null
                    ? cryptoService.hybridEncrypt(symmetricAlgorithm, asymmetricAlgorithm,
                    keyMaterial.getPublicKey(), bytes(plaintext))
                    : payload.getCiphertext();
            String decrypted = new String(cryptoService.hybridDecrypt(ciphertext, keyMaterial.getPrivateKey()),
                    StandardCharsets.UTF_8);
            if (!plaintext.equals(decrypted)) {
                return failure("HYBRID", algorithm, "validation failed: decrypted text does not match plaintext");
            }
            Map<String, Object> validation = new LinkedHashMap<String, Object>();
            validation.put("plaintext", plaintext);
            validation.put("decrypted", decrypted);
            if (keyMaterial.getPublicKey() != null) {
                validation.put("publicKey", cryptoService.encodePublicKey(keyMaterial.getPublicKey()));
            }
            validation.put("privateKey", cryptoService.encodePrivateKey(keyMaterial.getPrivateKey()));
            validation.put("ciphertext", ciphertext);
            return success("HYBRID", algorithm, validation);
        } catch (Exception ex) {
            return failure("HYBRID", algorithm, ex.getMessage());
        }
    }

    private KeyMaterial asymmetricKeyMaterial(AsymmetricAlgorithm algorithm,
                                              String publicKeyBase64,
                                              String privateKeyBase64,
                                              boolean decryptOnly) {
        if (decryptOnly && !hasText(privateKeyBase64)) {
            throw new IllegalArgumentException("privateKey is required when ciphertext is provided");
        }
        if (hasText(publicKeyBase64) && hasText(privateKeyBase64)) {
            return new KeyMaterial(
                    cryptoService.decodePublicKey(algorithm, publicKeyBase64),
                    cryptoService.decodePrivateKey(algorithm, privateKeyBase64));
        }
        if (decryptOnly) {
            PrivateKey privateKey = cryptoService.decodePrivateKey(algorithm, privateKeyBase64);
            return new KeyMaterial(null, privateKey);
        }
        KeyPair keyPair = cryptoService.generateKeyPair(algorithm);
        return new KeyMaterial(keyPair.getPublic(), keyPair.getPrivate());
    }

    private KeyMaterial hybridKeyMaterial(AsymmetricAlgorithm algorithm, HybridMockRequest request) {
        boolean decryptOnly = request.getCiphertext() != null;
        if (decryptOnly && !hasText(request.getPrivateKey())) {
            throw new IllegalArgumentException("privateKey is required when ciphertext is provided");
        }
        if (hasText(request.getPublicKey()) && hasText(request.getPrivateKey())) {
            return new KeyMaterial(
                    cryptoService.decodePublicKey(algorithm, request.getPublicKey()),
                    cryptoService.decodePrivateKey(algorithm, request.getPrivateKey()));
        }
        if (decryptOnly) {
            return new KeyMaterial(null, cryptoService.decodePrivateKey(algorithm, request.getPrivateKey()));
        }
        KeyPair keyPair = cryptoService.generateKeyPair(algorithm);
        return new KeyMaterial(keyPair.getPublic(), keyPair.getPrivate());
    }

    private Map<String, Object> success(String type, String algorithm, Map<String, Object> validation) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", true);
        response.put("code", "SUCCESS");
        response.put("message", "validation passed");
        response.put("type", type);
        response.put("algorithm", algorithm);
        response.put("validation", validation);
        response.put("data", mockData(type, algorithm, validation.get("plaintext")));
        return response;
    }

    private Map<String, Object> failure(String type, String algorithm, String message) {
        Map<String, Object> response = new LinkedHashMap<String, Object>();
        response.put("success", false);
        response.put("code", "VALIDATION_FAILED");
        response.put("message", message == null ? "validation failed" : message);
        response.put("type", type);
        response.put("algorithm", algorithm);
        return response;
    }

    private Map<String, Object> mockData(String type, String algorithm, Object plaintext) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("id", "mock-" + algorithm.toLowerCase(Locale.ENGLISH).replace('+', '-'));
        data.put("name", "postcryption mock response");
        data.put("status", "APPROVED");
        data.put("type", type);
        data.put("algorithm", algorithm);
        data.put("payload", plaintext);
        data.put("timestamp", System.currentTimeMillis());
        return data;
    }

    private String defaultText(String plaintext) {
        return hasText(plaintext) ? plaintext : DEFAULT_PLAINTEXT;
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private boolean hasText(String value) {
        return value != null && value.trim().length() > 0;
    }

    private static class KeyMaterial {

        private final PublicKey publicKey;

        private final PrivateKey privateKey;

        KeyMaterial(PublicKey publicKey, PrivateKey privateKey) {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        }

        PublicKey getPublicKey() {
            return publicKey;
        }

        PrivateKey getPrivateKey() {
            return privateKey;
        }
    }

    public static class SymmetricMockRequest {

        private String plaintext;

        private String key;

        private String iv;

        private String ciphertext;

        public String getPlaintext() {
            return plaintext;
        }

        public void setPlaintext(String plaintext) {
            this.plaintext = plaintext;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }
    }

    public static class AsymmetricMockRequest {

        private String plaintext;

        private String publicKey;

        private String privateKey;

        private String ciphertext;

        public String getPlaintext() {
            return plaintext;
        }

        public void setPlaintext(String plaintext) {
            this.plaintext = plaintext;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public String getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(String ciphertext) {
            this.ciphertext = ciphertext;
        }
    }

    public static class DigestMockRequest {

        private String plaintext;

        private String digestHex;

        public String getPlaintext() {
            return plaintext;
        }

        public void setPlaintext(String plaintext) {
            this.plaintext = plaintext;
        }

        public String getDigestHex() {
            return digestHex;
        }

        public void setDigestHex(String digestHex) {
            this.digestHex = digestHex;
        }
    }

    public static class HybridMockRequest {

        private String plaintext;

        private String publicKey;

        private String privateKey;

        private HybridCiphertext ciphertext;

        public String getPlaintext() {
            return plaintext;
        }

        public void setPlaintext(String plaintext) {
            this.plaintext = plaintext;
        }

        public String getPublicKey() {
            return publicKey;
        }

        public void setPublicKey(String publicKey) {
            this.publicKey = publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }

        public void setPrivateKey(String privateKey) {
            this.privateKey = privateKey;
        }

        public HybridCiphertext getCiphertext() {
            return ciphertext;
        }

        public void setCiphertext(HybridCiphertext ciphertext) {
            this.ciphertext = ciphertext;
        }
    }
}
