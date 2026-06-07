package com.fadeway32.crypto.core;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface CryptoService {

    SecretKey generateSymmetricKey(SymmetricAlgorithm algorithm);

    byte[] randomIv(SymmetricAlgorithm algorithm);

    KeyPair generateKeyPair(AsymmetricAlgorithm algorithm);

    KeyPair generateRsaKeyPair(int keySize);

    KeyPair generateEcKeyPair(String curveName);

    KeyPair generateSm2KeyPair();

    String encodePublicKey(PublicKey publicKey);

    String encodePrivateKey(PrivateKey privateKey);

    PublicKey decodePublicKey(AsymmetricAlgorithm algorithm, String publicKeyBase64);

    PrivateKey decodePrivateKey(AsymmetricAlgorithm algorithm, String privateKeyBase64);

    byte[] encrypt(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, byte[] plaintext);

    byte[] decrypt(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, byte[] ciphertext);

    String encryptToBase64(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, String plaintext);

    String decryptFromBase64(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, String ciphertextBase64);

    byte[] encrypt(AsymmetricAlgorithm algorithm, PublicKey publicKey, byte[] plaintext);

    byte[] decrypt(AsymmetricAlgorithm algorithm, PrivateKey privateKey, byte[] ciphertext);

    String encryptToBase64(AsymmetricAlgorithm algorithm, PublicKey publicKey, byte[] plaintext);

    byte[] decryptFromBase64(AsymmetricAlgorithm algorithm, PrivateKey privateKey, String ciphertextBase64);

    HybridCiphertext hybridEncrypt(SymmetricAlgorithm symmetricAlgorithm,
                                   AsymmetricAlgorithm asymmetricAlgorithm,
                                   PublicKey publicKey,
                                   byte[] plaintext);

    byte[] hybridDecrypt(HybridCiphertext ciphertext, PrivateKey privateKey);

    String digestHex(DigestAlgorithm algorithm, byte[] data);
}
