package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.HybridCiphertext;
import com.fadeway32.crypto.core.SymmetricAlgorithm;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CryptoUtilsTest {

    @Test
    void symmetricUtilsEncryptAndDecryptWithGeneratedKey() {
        SymmetricCiphertext ciphertext = SymmetricCryptoUtils.encrypt(SymmetricAlgorithm.AES, "util aes");

        assertThat(ciphertext.getKey()).isNotBlank();
        assertThat(ciphertext.getIv()).isNotBlank();
        assertThat(SymmetricCryptoUtils.decrypt(ciphertext)).isEqualTo("util aes");
    }

    @Test
    void symmetricUtilsEncryptAndDecryptWithProvidedKey() {
        String key = SymmetricCryptoUtils.generateKeyBase64(SymmetricAlgorithm.SM4);
        String iv = SymmetricCryptoUtils.randomIvBase64(SymmetricAlgorithm.SM4);

        String ciphertext = SymmetricCryptoUtils.encryptToBase64(SymmetricAlgorithm.SM4, key, iv, "util sm4");

        assertThat(SymmetricCryptoUtils.decryptFromBase64(SymmetricAlgorithm.SM4, key, iv, ciphertext))
                .isEqualTo("util sm4");
    }

    @Test
    void asymmetricUtilsEncryptAndDecrypt() {
        CryptoKeyPair keyPair = AsymmetricCryptoUtils.generateRsaKeyPair(2048);

        String ciphertext = AsymmetricCryptoUtils.encryptToBase64(
                AsymmetricAlgorithm.RSA,
                keyPair.getPublicKey(),
                "util rsa"
        );

        assertThat(AsymmetricCryptoUtils.decryptFromBase64(
                AsymmetricAlgorithm.RSA,
                keyPair.getPrivateKey(),
                ciphertext
        )).isEqualTo("util rsa");
    }

    @Test
    void asymmetricUtilsHybridEncryptAndDecrypt() {
        CryptoKeyPair keyPair = AsymmetricCryptoUtils.generateRsaKeyPair(2048);

        HybridCiphertext ciphertext = AsymmetricCryptoUtils.hybridEncrypt(
                SymmetricAlgorithm.AES,
                AsymmetricAlgorithm.RSA,
                keyPair.getPublicKey(),
                "util hybrid"
        );

        assertThat(AsymmetricCryptoUtils.hybridDecrypt(ciphertext, keyPair.getPrivateKey()))
                .isEqualTo("util hybrid");
    }

    @Test
    void digestUtilsCalculateHex() {
        assertThat(DigestUtils.md5Hex("abc")).isEqualTo("900150983cd24fb0d6963f7d28e17f72");
        assertThat(DigestUtils.sha256Hex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(DigestUtils.sm3Hex("abc"))
                .isEqualTo("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0");
    }
}
