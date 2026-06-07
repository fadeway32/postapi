package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.SymmetricAlgorithm;

public final class SymmetricCryptoUtils {

    private SymmetricCryptoUtils() {
    }

    public static String generateKeyBase64(SymmetricAlgorithm algorithm) {
        return CryptoUtils.generateSymmetricKeyBase64(algorithm);
    }

    public static String randomIvBase64(SymmetricAlgorithm algorithm) {
        return CryptoUtils.randomIvBase64(algorithm);
    }

    public static SymmetricCiphertext encrypt(SymmetricAlgorithm algorithm, String plaintext) {
        return CryptoUtils.encryptSymmetric(algorithm, plaintext);
    }

    public static String decrypt(SymmetricCiphertext ciphertext) {
        return CryptoUtils.decryptSymmetric(ciphertext);
    }

    public static String encryptToBase64(SymmetricAlgorithm algorithm, String keyBase64, String ivBase64, String plaintext) {
        return CryptoUtils.encryptSymmetricToBase64(algorithm, keyBase64, ivBase64, plaintext);
    }

    public static String decryptFromBase64(SymmetricAlgorithm algorithm, String keyBase64, String ivBase64, String ciphertextBase64) {
        return CryptoUtils.decryptSymmetricFromBase64(algorithm, keyBase64, ivBase64, ciphertextBase64);
    }
}
