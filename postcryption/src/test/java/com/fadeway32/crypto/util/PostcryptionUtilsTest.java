package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.HybridCiphertext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostcryptionUtilsTest {

    @Test
    void directlyCallsSymmetricAlgorithms() {
        SymmetricCiphertext aes = PostcryptionUtils.aesEncrypt("direct aes");
        SymmetricCiphertext sm4 = PostcryptionUtils.sm4Encrypt("direct sm4");
        SymmetricCiphertext des = PostcryptionUtils.desEncrypt("direct des");
        SymmetricCiphertext des3 = PostcryptionUtils.des3Encrypt("direct des3");

        assertThat(PostcryptionUtils.aesDecrypt(aes)).isEqualTo("direct aes");
        assertThat(PostcryptionUtils.sm4Decrypt(sm4)).isEqualTo("direct sm4");
        assertThat(PostcryptionUtils.desDecrypt(des)).isEqualTo("direct des");
        assertThat(PostcryptionUtils.des3Decrypt(des3)).isEqualTo("direct des3");
    }

    @Test
    void directlyCallsAsymmetricAlgorithms() {
        CryptoKeyPair rsa = PostcryptionUtils.generateRsaKeyPair();
        CryptoKeyPair ecc = PostcryptionUtils.generateEccKeyPair();
        CryptoKeyPair sm2 = PostcryptionUtils.generateSm2KeyPair();

        String rsaCiphertext = PostcryptionUtils.rsaEncryptToBase64(rsa.getPublicKey(), "direct rsa");
        String eccCiphertext = PostcryptionUtils.eccEncryptToBase64(ecc.getPublicKey(), "direct ecc");
        String sm2Ciphertext = PostcryptionUtils.sm2EncryptToBase64(sm2.getPublicKey(), "direct sm2");

        assertThat(PostcryptionUtils.rsaDecryptFromBase64(rsa.getPrivateKey(), rsaCiphertext)).isEqualTo("direct rsa");
        assertThat(PostcryptionUtils.eccDecryptFromBase64(ecc.getPrivateKey(), eccCiphertext)).isEqualTo("direct ecc");
        assertThat(PostcryptionUtils.sm2DecryptFromBase64(sm2.getPrivateKey(), sm2Ciphertext)).isEqualTo("direct sm2");
    }

    @Test
    void directlyCallsHybridAndDigestAlgorithms() {
        CryptoKeyPair rsa = PostcryptionUtils.generateRsaKeyPair();

        HybridCiphertext ciphertext = PostcryptionUtils.hybridAesRsaEncrypt(rsa.getPublicKey(), "direct hybrid");

        assertThat(PostcryptionUtils.hybridAesRsaDecrypt(ciphertext, rsa.getPrivateKey())).isEqualTo("direct hybrid");
        assertThat(PostcryptionUtils.md5Hex("abc")).isEqualTo("900150983cd24fb0d6963f7d28e17f72");
        assertThat(PostcryptionUtils.sha256Hex("abc"))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(PostcryptionUtils.sm3Hex("abc"))
                .isEqualTo("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0");
    }
}
