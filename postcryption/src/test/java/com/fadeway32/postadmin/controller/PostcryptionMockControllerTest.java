package com.fadeway32.postadmin.controller;

import com.fadeway32.crypto.autoconfigure.CryptoProperties;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DefaultCryptoService;
import com.fadeway32.crypto.core.DigestAlgorithm;
import com.fadeway32.crypto.core.SymmetricAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PostcryptionMockControllerTest {

    private CryptoService cryptoService;

    private PostcryptionMockController controller;

    @BeforeEach
    void setUp() {
        cryptoService = new DefaultCryptoService(new CryptoProperties());
        controller = new PostcryptionMockController(cryptoService);
    }

    @Test
    void aesEndpointReturnsMockDataWhenValidationPasses() {
        PostcryptionMockController.MockCryptoRequest request = symmetricRequest(SymmetricAlgorithm.AES, "hello aes");

        PostcryptionMockController.MockResponse<Map<String, Object>> response = controller.aes(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getCode()).isEqualTo("OK");
        assertThat(response.getData()).containsEntry("algorithm", "AES");
    }

    @Test
    void des3EndpointReturnsMockDataWhenValidationPasses() {
        PostcryptionMockController.MockCryptoRequest request = symmetricRequest(SymmetricAlgorithm.DES3, "hello 3des");

        PostcryptionMockController.MockResponse<Map<String, Object>> response = controller.des3(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("algorithm", "DES3");
    }

    @Test
    void sms4AliasReturnsMockDataWhenValidationPasses() {
        PostcryptionMockController.MockCryptoRequest request = symmetricRequest(SymmetricAlgorithm.SM4, "hello sms4");

        PostcryptionMockController.MockResponse<Map<String, Object>> response = controller.sms4(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("algorithm", "SM4");
    }

    @Test
    void symmetricEndpointReturnsFailureWhenValidationFails() {
        PostcryptionMockController.MockCryptoRequest request = symmetricRequest(SymmetricAlgorithm.AES, "hello aes");
        request.setCiphertext(request.getCiphertext().substring(0, request.getCiphertext().length() - 2) + "aa");

        PostcryptionMockController.MockResponse<Map<String, Object>> response = controller.aes(request);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getCode()).isEqualTo("VALIDATION_FAILED");
    }

    @Test
    void md5EndpointReturnsMockDataWhenDigestMatches() {
        PostcryptionMockController.MockCryptoRequest request = new PostcryptionMockController.MockCryptoRequest();
        request.setPlaintext("hello md5");
        request.setDigest(cryptoService.digestHex(DigestAlgorithm.MD5, request.getPlaintext().getBytes(StandardCharsets.UTF_8)));

        PostcryptionMockController.MockResponse<Map<String, Object>> response = controller.md5(request);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData()).containsEntry("algorithm", "MD5");
    }

    private PostcryptionMockController.MockCryptoRequest symmetricRequest(SymmetricAlgorithm algorithm, String plaintext) {
        SecretKey key = cryptoService.generateSymmetricKey(algorithm);
        byte[] iv = cryptoService.randomIv(algorithm);
        String ciphertext = cryptoService.encryptToBase64(algorithm, key.getEncoded(), iv, plaintext);

        PostcryptionMockController.MockCryptoRequest request = new PostcryptionMockController.MockCryptoRequest();
        request.setPlaintext(plaintext);
        request.setKey(Base64.getEncoder().encodeToString(key.getEncoded()));
        request.setIv(Base64.getEncoder().encodeToString(iv));
        request.setCiphertext(ciphertext);
        return request;
    }
}
