package com.fadeway32.postadmin.controller;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.CryptoException;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DigestAlgorithm;
import com.fadeway32.crypto.core.HybridCiphertext;
import com.fadeway32.crypto.core.SymmetricAlgorithm;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("${postcryption.mock-controller.path:/postadmin/postcryption/mock}")
@ConditionalOnProperty(prefix = "postcryption.mock-controller", name = "enabled", havingValue = "true")
public class PostcryptionMockController {

    private static final String SUCCESS_CODE = "OK";
    private static final String VALIDATION_FAILED_CODE = "VALIDATION_FAILED";

    private final CryptoService cryptoService;

    public PostcryptionMockController(CryptoService cryptoService) {
        this.cryptoService = cryptoService;
    }

    @PostMapping("/aes")
    public MockResponse<Map<String, Object>> aes(@RequestBody MockCryptoRequest request) {
        return validateSymmetric(SymmetricAlgorithm.AES, request);
    }

    @PostMapping("/sm4")
    public MockResponse<Map<String, Object>> sm4(@RequestBody MockCryptoRequest request) {
        return validateSymmetric(SymmetricAlgorithm.SM4, request);
    }

    @PostMapping("/sms4")
    public MockResponse<Map<String, Object>> sms4(@RequestBody MockCryptoRequest request) {
        return validateSymmetric(SymmetricAlgorithm.SM4, request);
    }

    @PostMapping("/des")
    public MockResponse<Map<String, Object>> des(@RequestBody MockCryptoRequest request) {
        return validateSymmetric(SymmetricAlgorithm.DES, request);
    }

    @PostMapping("/des3")
    public MockResponse<Map<String, Object>> des3(@RequestBody MockCryptoRequest request) {
        return validateSymmetric(SymmetricAlgorithm.DES3, request);
    }

    @PostMapping("/3des")
    public MockResponse<Map<String, Object>> threeDes(@RequestBody MockCryptoRequest request) {
        return validateSymmetric(SymmetricAlgorithm.DES3, request);
    }

    @PostMapping("/rsa")
    public MockResponse<Map<String, Object>> rsa(@RequestBody MockCryptoRequest request) {
        return validateAsymmetric(AsymmetricAlgorithm.RSA, request);
    }

    @PostMapping("/ecc")
    public MockResponse<Map<String, Object>> ecc(@RequestBody MockCryptoRequest request) {
        return validateAsymmetric(AsymmetricAlgorithm.ECC, request);
    }

    @PostMapping("/sm2")
    public MockResponse<Map<String, Object>> sm2(@RequestBody MockCryptoRequest request) {
        return validateAsymmetric(AsymmetricAlgorithm.SM2, request);
    }

    @PostMapping("/md5")
    public MockResponse<Map<String, Object>> md5(@RequestBody MockCryptoRequest request) {
        return validateDigest(DigestAlgorithm.MD5, request);
    }

    @PostMapping("/sha256")
    public MockResponse<Map<String, Object>> sha256(@RequestBody MockCryptoRequest request) {
        return validateDigest(DigestAlgorithm.SHA256, request);
    }

    @PostMapping("/sha3-256")
    public MockResponse<Map<String, Object>> sha3_256(@RequestBody MockCryptoRequest request) {
        return validateDigest(DigestAlgorithm.SHA3_256, request);
    }

    @PostMapping("/sm3")
    public MockResponse<Map<String, Object>> sm3(@RequestBody MockCryptoRequest request) {
        return validateDigest(DigestAlgorithm.SM3, request);
    }

    @PostMapping("/hybrid/{algorithms}")
    public MockResponse<Map<String, Object>> hybrid(@PathVariable("algorithms") String algorithms,
                                                    @RequestBody MockCryptoRequest request) {
        AlgorithmPair pair = parseAlgorithmPair(algorithms);
        if (pair == null) {
            return failed("Unsupported hybrid algorithm pair");
        }
        return validateHybrid(pair.symmetricAlgorithm, pair.asymmetricAlgorithm, request);
    }

    private MockResponse<Map<String, Object>> validateSymmetric(SymmetricAlgorithm algorithm, MockCryptoRequest request) {
        try {
            requireText(request.getPlaintext(), "plaintext");
            requireText(request.getKey(), "key");
            requireText(request.getIv(), "iv");
            requireText(request.getCiphertext(), "ciphertext");
            String decrypted = cryptoService.decryptFromBase64(
                    algorithm,
                    Base64.getDecoder().decode(request.getKey()),
                    Base64.getDecoder().decode(request.getIv()),
                    request.getCiphertext()
            );
            if (!request.getPlaintext().equals(decrypted)) {
                return failed("Ciphertext does not match plaintext");
            }
            return success(mockData("symmetric", algorithm.name(), request.getPlaintext()));
        } catch (RuntimeException ex) {
            return failed(validationMessage(ex));
        }
    }

    private MockResponse<Map<String, Object>> validateAsymmetric(AsymmetricAlgorithm algorithm, MockCryptoRequest request) {
        try {
            requireText(request.getPlaintext(), "plaintext");
            if (hasText(request.getCiphertext())) {
                requireText(request.getPrivateKey(), "privateKey");
                PrivateKey privateKey = cryptoService.decodePrivateKey(algorithm, request.getPrivateKey());
                String decrypted = new String(
                        cryptoService.decryptFromBase64(algorithm, privateKey, request.getCiphertext()),
                        StandardCharsets.UTF_8
                );
                if (!request.getPlaintext().equals(decrypted)) {
                    return failed("Ciphertext does not match plaintext");
                }
            } else {
                requireText(request.getPublicKey(), "publicKey");
                if (hasText(request.getPrivateKey())) {
                    return failed("privateKey is only used when ciphertext is provided");
                }
                PublicKey publicKey = cryptoService.decodePublicKey(algorithm, request.getPublicKey());
                cryptoService.encryptToBase64(algorithm, publicKey, request.getPlaintext().getBytes(StandardCharsets.UTF_8));
            }
            return success(mockData("asymmetric", algorithm.name(), request.getPlaintext()));
        } catch (RuntimeException ex) {
            return failed(validationMessage(ex));
        }
    }

    private MockResponse<Map<String, Object>> validateDigest(DigestAlgorithm algorithm, MockCryptoRequest request) {
        try {
            requireText(request.getPlaintext(), "plaintext");
            String digest = firstText(request.getDigest(), request.getSign(), request.getSignature());
            requireText(digest, "digest");
            String expected = cryptoService.digestHex(algorithm, request.getPlaintext().getBytes(StandardCharsets.UTF_8));
            if (!expected.equalsIgnoreCase(digest)) {
                return failed("Digest does not match plaintext");
            }
            return success(mockData("digest", algorithm.name(), request.getPlaintext()));
        } catch (RuntimeException ex) {
            return failed(validationMessage(ex));
        }
    }

    private MockResponse<Map<String, Object>> validateHybrid(SymmetricAlgorithm symmetricAlgorithm,
                                                            AsymmetricAlgorithm asymmetricAlgorithm,
                                                            MockCryptoRequest request) {
        try {
            requireText(request.getPlaintext(), "plaintext");
            requireText(request.getPrivateKey(), "privateKey");
            requireText(request.getEncryptedKey(), "encryptedKey");
            requireText(request.getIv(), "iv");
            requireText(request.getCiphertext(), "ciphertext");
            HybridCiphertext ciphertext = new HybridCiphertext(
                    symmetricAlgorithm,
                    asymmetricAlgorithm,
                    request.getEncryptedKey(),
                    request.getIv(),
                    request.getCiphertext()
            );
            PrivateKey privateKey = cryptoService.decodePrivateKey(asymmetricAlgorithm, request.getPrivateKey());
            String decrypted = new String(cryptoService.hybridDecrypt(ciphertext, privateKey), StandardCharsets.UTF_8);
            if (!request.getPlaintext().equals(decrypted)) {
                return failed("Ciphertext does not match plaintext");
            }
            return success(mockData("hybrid", symmetricAlgorithm.name() + "-" + asymmetricAlgorithm.name(), request.getPlaintext()));
        } catch (RuntimeException ex) {
            return failed(validationMessage(ex));
        }
    }

    private Map<String, Object> mockData(String type, String algorithm, String plaintext) {
        Map<String, Object> data = new LinkedHashMap<String, Object>();
        data.put("mockId", "mock-" + normalizeToken(algorithm));
        data.put("type", type);
        data.put("algorithm", algorithm);
        data.put("validated", true);
        data.put("echo", plaintext);
        data.put("serverTime", System.currentTimeMillis());
        return data;
    }

    private MockResponse<Map<String, Object>> success(Map<String, Object> data) {
        return new MockResponse<Map<String, Object>>(true, SUCCESS_CODE, "Validation passed", data);
    }

    private MockResponse<Map<String, Object>> failed(String message) {
        return new MockResponse<Map<String, Object>>(false, VALIDATION_FAILED_CODE, message, null);
    }

    private static void requireText(String value, String name) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private static String firstText(String first, String second, String third) {
        if (hasText(first)) {
            return first;
        }
        if (hasText(second)) {
            return second;
        }
        return third;
    }

    private static String validationMessage(RuntimeException ex) {
        if (ex instanceof CryptoException || ex instanceof IllegalArgumentException) {
            return ex.getMessage();
        }
        return "Invalid encrypted payload";
    }

    private static SymmetricAlgorithm symmetricAlgorithm(String value) {
        String token = normalizeToken(value);
        if ("aes".equals(token)) {
            return SymmetricAlgorithm.AES;
        }
        if ("sm4".equals(token)) {
            return SymmetricAlgorithm.SM4;
        }
        if ("des".equals(token)) {
            return SymmetricAlgorithm.DES;
        }
        if ("des3".equals(token) || "3des".equals(token)) {
            return SymmetricAlgorithm.DES3;
        }
        return null;
    }

    private static AsymmetricAlgorithm asymmetricAlgorithm(String value) {
        String token = normalizeToken(value);
        if ("rsa".equals(token)) {
            return AsymmetricAlgorithm.RSA;
        }
        if ("ecc".equals(token) || "ec".equals(token)) {
            return AsymmetricAlgorithm.ECC;
        }
        if ("sm2".equals(token)) {
            return AsymmetricAlgorithm.SM2;
        }
        return null;
    }

    private static AlgorithmPair parseAlgorithmPair(String algorithms) {
        if (!hasText(algorithms)) {
            return null;
        }
        String[] tokens = algorithms.split("-", 2);
        if (tokens.length != 2) {
            return null;
        }
        SymmetricAlgorithm symmetricAlgorithm = symmetricAlgorithm(tokens[0]);
        AsymmetricAlgorithm asymmetricAlgorithm = asymmetricAlgorithm(tokens[1]);
        if (symmetricAlgorithm == null || asymmetricAlgorithm == null) {
            return null;
        }
        return new AlgorithmPair(symmetricAlgorithm, asymmetricAlgorithm);
    }

    private static String normalizeToken(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().toLowerCase(Locale.ENGLISH).replace("_", "").replace("-", "");
    }

    private static class AlgorithmPair {

        private final SymmetricAlgorithm symmetricAlgorithm;
        private final AsymmetricAlgorithm asymmetricAlgorithm;

        private AlgorithmPair(SymmetricAlgorithm symmetricAlgorithm, AsymmetricAlgorithm asymmetricAlgorithm) {
            this.symmetricAlgorithm = symmetricAlgorithm;
            this.asymmetricAlgorithm = asymmetricAlgorithm;
        }
    }

    public static class MockCryptoRequest {

        private String algorithm;

        private String pathAlgorithm;

        private String plaintext;

        private String key;

        private String iv;

        private String ciphertext;

        private String publicKey;

        private String privateKey;

        private String encryptedKey;

        private String digest;

        private String sign;

        private String signature;

        public String getAlgorithm() {
            return algorithm;
        }

        public void setAlgorithm(String algorithm) {
            this.algorithm = algorithm;
        }

        public String getPathAlgorithm() {
            return pathAlgorithm;
        }

        public void setPathAlgorithm(String pathAlgorithm) {
            this.pathAlgorithm = pathAlgorithm;
        }

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

        public String getEncryptedKey() {
            return encryptedKey;
        }

        public void setEncryptedKey(String encryptedKey) {
            this.encryptedKey = encryptedKey;
        }

        public String getDigest() {
            return digest;
        }

        public void setDigest(String digest) {
            this.digest = digest;
        }

        public String getSign() {
            return sign;
        }

        public void setSign(String sign) {
            this.sign = sign;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }
    }

    public static class MockResponse<T> {

        private boolean success;

        private String code;

        private String message;

        private T data;

        public MockResponse() {
        }

        public MockResponse(boolean success, String code, String message, T data) {
            this.success = success;
            this.code = code;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }
}
