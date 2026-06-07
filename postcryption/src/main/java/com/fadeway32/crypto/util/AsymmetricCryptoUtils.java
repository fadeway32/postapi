package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.HybridCiphertext;
import com.fadeway32.crypto.core.SymmetricAlgorithm;

public final class AsymmetricCryptoUtils {

    private AsymmetricCryptoUtils() {
    }

    public static CryptoKeyPair generateKeyPair(AsymmetricAlgorithm algorithm) {
        return CryptoUtils.generateKeyPair(algorithm);
    }

    public static CryptoKeyPair generateRsaKeyPair(int keySize) {
        return CryptoUtils.generateRsaKeyPair(keySize);
    }

    public static CryptoKeyPair generateEcKeyPair(String curveName) {
        return CryptoUtils.generateEcKeyPair(curveName);
    }

    public static CryptoKeyPair generateSm2KeyPair() {
        return CryptoUtils.generateSm2KeyPair();
    }

    public static String encryptToBase64(AsymmetricAlgorithm algorithm, String publicKeyBase64, String plaintext) {
        return CryptoUtils.encryptAsymmetricToBase64(algorithm, publicKeyBase64, plaintext);
    }

    public static String decryptFromBase64(AsymmetricAlgorithm algorithm, String privateKeyBase64, String ciphertextBase64) {
        return CryptoUtils.decryptAsymmetricFromBase64(algorithm, privateKeyBase64, ciphertextBase64);
    }

    public static HybridCiphertext hybridEncrypt(SymmetricAlgorithm symmetricAlgorithm,
                                                 AsymmetricAlgorithm asymmetricAlgorithm,
                                                 String publicKeyBase64,
                                                 String plaintext) {
        return CryptoUtils.hybridEncrypt(symmetricAlgorithm, asymmetricAlgorithm, publicKeyBase64, plaintext);
    }

    public static String hybridDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return CryptoUtils.hybridDecrypt(ciphertext, privateKeyBase64);
    }
}
