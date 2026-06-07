package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DigestAlgorithm;
import com.fadeway32.crypto.core.HybridCiphertext;
import com.fadeway32.crypto.core.SymmetricAlgorithm;

public final class PostcryptionUtils {

    private static final int DEFAULT_RSA_KEY_SIZE = 2048;

    private PostcryptionUtils() {
    }

    public static CryptoService defaultService() {
        return CryptoUtils.defaultService();
    }

    public static String generateSymmetricKeyBase64(SymmetricAlgorithm algorithm) {
        return CryptoUtils.generateSymmetricKeyBase64(algorithm);
    }

    public static String randomIvBase64(SymmetricAlgorithm algorithm) {
        return CryptoUtils.randomIvBase64(algorithm);
    }

    public static SymmetricCiphertext encryptSymmetric(SymmetricAlgorithm algorithm, String plaintext) {
        return CryptoUtils.encryptSymmetric(algorithm, plaintext);
    }

    public static String decryptSymmetric(SymmetricCiphertext ciphertext) {
        return CryptoUtils.decryptSymmetric(ciphertext);
    }

    public static String encryptSymmetricToBase64(SymmetricAlgorithm algorithm,
                                                  String keyBase64,
                                                  String ivBase64,
                                                  String plaintext) {
        return CryptoUtils.encryptSymmetricToBase64(algorithm, keyBase64, ivBase64, plaintext);
    }

    public static String decryptSymmetricFromBase64(SymmetricAlgorithm algorithm,
                                                    String keyBase64,
                                                    String ivBase64,
                                                    String ciphertextBase64) {
        return CryptoUtils.decryptSymmetricFromBase64(algorithm, keyBase64, ivBase64, ciphertextBase64);
    }

    public static String generateAesKeyBase64() {
        return generateSymmetricKeyBase64(SymmetricAlgorithm.AES);
    }

    public static String randomAesIvBase64() {
        return randomIvBase64(SymmetricAlgorithm.AES);
    }

    public static SymmetricCiphertext aesEncrypt(String plaintext) {
        return encryptSymmetric(SymmetricAlgorithm.AES, plaintext);
    }

    public static String aesDecrypt(SymmetricCiphertext ciphertext) {
        return decryptSymmetric(ciphertext);
    }

    public static String aesEncryptToBase64(String keyBase64, String ivBase64, String plaintext) {
        return encryptSymmetricToBase64(SymmetricAlgorithm.AES, keyBase64, ivBase64, plaintext);
    }

    public static String aesDecryptFromBase64(String keyBase64, String ivBase64, String ciphertextBase64) {
        return decryptSymmetricFromBase64(SymmetricAlgorithm.AES, keyBase64, ivBase64, ciphertextBase64);
    }

    public static String generateSm4KeyBase64() {
        return generateSymmetricKeyBase64(SymmetricAlgorithm.SM4);
    }

    public static String randomSm4IvBase64() {
        return randomIvBase64(SymmetricAlgorithm.SM4);
    }

    public static SymmetricCiphertext sm4Encrypt(String plaintext) {
        return encryptSymmetric(SymmetricAlgorithm.SM4, plaintext);
    }

    public static String sm4Decrypt(SymmetricCiphertext ciphertext) {
        return decryptSymmetric(ciphertext);
    }

    public static String sm4EncryptToBase64(String keyBase64, String ivBase64, String plaintext) {
        return encryptSymmetricToBase64(SymmetricAlgorithm.SM4, keyBase64, ivBase64, plaintext);
    }

    public static String sm4DecryptFromBase64(String keyBase64, String ivBase64, String ciphertextBase64) {
        return decryptSymmetricFromBase64(SymmetricAlgorithm.SM4, keyBase64, ivBase64, ciphertextBase64);
    }

    public static String generateDesKeyBase64() {
        return generateSymmetricKeyBase64(SymmetricAlgorithm.DES);
    }

    public static String randomDesIvBase64() {
        return randomIvBase64(SymmetricAlgorithm.DES);
    }

    public static SymmetricCiphertext desEncrypt(String plaintext) {
        return encryptSymmetric(SymmetricAlgorithm.DES, plaintext);
    }

    public static String desDecrypt(SymmetricCiphertext ciphertext) {
        return decryptSymmetric(ciphertext);
    }

    public static String desEncryptToBase64(String keyBase64, String ivBase64, String plaintext) {
        return encryptSymmetricToBase64(SymmetricAlgorithm.DES, keyBase64, ivBase64, plaintext);
    }

    public static String desDecryptFromBase64(String keyBase64, String ivBase64, String ciphertextBase64) {
        return decryptSymmetricFromBase64(SymmetricAlgorithm.DES, keyBase64, ivBase64, ciphertextBase64);
    }

    public static String generateDes3KeyBase64() {
        return generateSymmetricKeyBase64(SymmetricAlgorithm.DES3);
    }

    public static String randomDes3IvBase64() {
        return randomIvBase64(SymmetricAlgorithm.DES3);
    }

    public static SymmetricCiphertext des3Encrypt(String plaintext) {
        return encryptSymmetric(SymmetricAlgorithm.DES3, plaintext);
    }

    public static String des3Decrypt(SymmetricCiphertext ciphertext) {
        return decryptSymmetric(ciphertext);
    }

    public static String des3EncryptToBase64(String keyBase64, String ivBase64, String plaintext) {
        return encryptSymmetricToBase64(SymmetricAlgorithm.DES3, keyBase64, ivBase64, plaintext);
    }

    public static String des3DecryptFromBase64(String keyBase64, String ivBase64, String ciphertextBase64) {
        return decryptSymmetricFromBase64(SymmetricAlgorithm.DES3, keyBase64, ivBase64, ciphertextBase64);
    }

    public static CryptoKeyPair generateKeyPair(AsymmetricAlgorithm algorithm) {
        return CryptoUtils.generateKeyPair(algorithm);
    }

    public static CryptoKeyPair generateRsaKeyPair() {
        return CryptoUtils.generateRsaKeyPair(DEFAULT_RSA_KEY_SIZE);
    }

    public static CryptoKeyPair generateRsaKeyPair(int keySize) {
        return CryptoUtils.generateRsaKeyPair(keySize);
    }

    public static CryptoKeyPair generateEccKeyPair() {
        return CryptoUtils.generateKeyPair(AsymmetricAlgorithm.ECC);
    }

    public static CryptoKeyPair generateEcKeyPair(String curveName) {
        return CryptoUtils.generateEcKeyPair(curveName);
    }

    public static CryptoKeyPair generateSm2KeyPair() {
        return CryptoUtils.generateSm2KeyPair();
    }

    public static String encryptAsymmetricToBase64(AsymmetricAlgorithm algorithm,
                                                   String publicKeyBase64,
                                                   String plaintext) {
        return CryptoUtils.encryptAsymmetricToBase64(algorithm, publicKeyBase64, plaintext);
    }

    public static String decryptAsymmetricFromBase64(AsymmetricAlgorithm algorithm,
                                                     String privateKeyBase64,
                                                     String ciphertextBase64) {
        return CryptoUtils.decryptAsymmetricFromBase64(algorithm, privateKeyBase64, ciphertextBase64);
    }

    public static String rsaEncryptToBase64(String publicKeyBase64, String plaintext) {
        return encryptAsymmetricToBase64(AsymmetricAlgorithm.RSA, publicKeyBase64, plaintext);
    }

    public static String rsaDecryptFromBase64(String privateKeyBase64, String ciphertextBase64) {
        return decryptAsymmetricFromBase64(AsymmetricAlgorithm.RSA, privateKeyBase64, ciphertextBase64);
    }

    public static String eccEncryptToBase64(String publicKeyBase64, String plaintext) {
        return encryptAsymmetricToBase64(AsymmetricAlgorithm.ECC, publicKeyBase64, plaintext);
    }

    public static String eccDecryptFromBase64(String privateKeyBase64, String ciphertextBase64) {
        return decryptAsymmetricFromBase64(AsymmetricAlgorithm.ECC, privateKeyBase64, ciphertextBase64);
    }

    public static String sm2EncryptToBase64(String publicKeyBase64, String plaintext) {
        return encryptAsymmetricToBase64(AsymmetricAlgorithm.SM2, publicKeyBase64, plaintext);
    }

    public static String sm2DecryptFromBase64(String privateKeyBase64, String ciphertextBase64) {
        return decryptAsymmetricFromBase64(AsymmetricAlgorithm.SM2, privateKeyBase64, ciphertextBase64);
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

    public static HybridCiphertext hybridAesRsaEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.AES, AsymmetricAlgorithm.RSA, publicKeyBase64, plaintext);
    }

    public static String hybridAesRsaDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridAesEccEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.AES, AsymmetricAlgorithm.ECC, publicKeyBase64, plaintext);
    }

    public static String hybridAesEccDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridAesSm2Encrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.AES, AsymmetricAlgorithm.SM2, publicKeyBase64, plaintext);
    }

    public static String hybridAesSm2Decrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridSm4RsaEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.SM4, AsymmetricAlgorithm.RSA, publicKeyBase64, plaintext);
    }

    public static String hybridSm4RsaDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridSm4EccEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.SM4, AsymmetricAlgorithm.ECC, publicKeyBase64, plaintext);
    }

    public static String hybridSm4EccDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridSm4Sm2Encrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.SM4, AsymmetricAlgorithm.SM2, publicKeyBase64, plaintext);
    }

    public static String hybridSm4Sm2Decrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridDesRsaEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.DES, AsymmetricAlgorithm.RSA, publicKeyBase64, plaintext);
    }

    public static String hybridDesRsaDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridDesEccEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.DES, AsymmetricAlgorithm.ECC, publicKeyBase64, plaintext);
    }

    public static String hybridDesEccDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridDesSm2Encrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.DES, AsymmetricAlgorithm.SM2, publicKeyBase64, plaintext);
    }

    public static String hybridDesSm2Decrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridDes3RsaEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.DES3, AsymmetricAlgorithm.RSA, publicKeyBase64, plaintext);
    }

    public static String hybridDes3RsaDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridDes3EccEncrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.DES3, AsymmetricAlgorithm.ECC, publicKeyBase64, plaintext);
    }

    public static String hybridDes3EccDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static HybridCiphertext hybridDes3Sm2Encrypt(String publicKeyBase64, String plaintext) {
        return hybridEncrypt(SymmetricAlgorithm.DES3, AsymmetricAlgorithm.SM2, publicKeyBase64, plaintext);
    }

    public static String hybridDes3Sm2Decrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        return hybridDecrypt(ciphertext, privateKeyBase64);
    }

    public static String digestHex(DigestAlgorithm algorithm, String plaintext) {
        return CryptoUtils.digestHex(algorithm, plaintext);
    }

    public static String md5Hex(String plaintext) {
        return CryptoUtils.md5Hex(plaintext);
    }

    public static String sha256Hex(String plaintext) {
        return CryptoUtils.sha256Hex(plaintext);
    }

    public static String sha3_256Hex(String plaintext) {
        return CryptoUtils.sha3_256Hex(plaintext);
    }

    public static String sm3Hex(String plaintext) {
        return CryptoUtils.sm3Hex(plaintext);
    }
}
