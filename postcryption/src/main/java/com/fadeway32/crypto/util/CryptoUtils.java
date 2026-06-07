package com.fadeway32.crypto.util;

import com.fadeway32.crypto.autoconfigure.CryptoProperties;
import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.CryptoException;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DefaultCryptoService;
import com.fadeway32.crypto.core.DigestAlgorithm;
import com.fadeway32.crypto.core.HybridCiphertext;
import com.fadeway32.crypto.core.SymmetricAlgorithm;

import javax.crypto.SecretKey;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class CryptoUtils {

    private static final CryptoService DEFAULT_SERVICE = new DefaultCryptoService(new CryptoProperties());

    private CryptoUtils() {
    }

    public static CryptoService defaultService() {
        return DEFAULT_SERVICE;
    }

    public static String generateSymmetricKeyBase64(SymmetricAlgorithm algorithm) {
        SecretKey key = DEFAULT_SERVICE.generateSymmetricKey(algorithm);
        return CryptoCodecUtils.base64Encode(key.getEncoded());
    }

    public static String randomIvBase64(SymmetricAlgorithm algorithm) {
        return CryptoCodecUtils.base64Encode(DEFAULT_SERVICE.randomIv(algorithm));
    }

    public static SymmetricCiphertext encryptSymmetric(SymmetricAlgorithm algorithm, String plaintext) {
        String key = generateSymmetricKeyBase64(algorithm);
        String iv = randomIvBase64(algorithm);
        String ciphertext = encryptSymmetricToBase64(algorithm, key, iv, plaintext);
        return new SymmetricCiphertext(algorithm, key, iv, ciphertext);
    }

    public static String decryptSymmetric(SymmetricCiphertext ciphertext) {
        if (ciphertext == null) {
            throw new CryptoException("ciphertext must not be null");
        }
        return decryptSymmetricFromBase64(
                ciphertext.getAlgorithm(),
                ciphertext.getKey(),
                ciphertext.getIv(),
                ciphertext.getCiphertext()
        );
    }

    public static String encryptSymmetricToBase64(SymmetricAlgorithm algorithm,
                                                  String keyBase64,
                                                  String ivBase64,
                                                  String plaintext) {
        CryptoCodecUtils.requireText(keyBase64, "keyBase64");
        CryptoCodecUtils.requireText(ivBase64, "ivBase64");
        return DEFAULT_SERVICE.encryptToBase64(
                algorithm,
                CryptoCodecUtils.base64Decode(keyBase64),
                CryptoCodecUtils.base64Decode(ivBase64),
                plaintext
        );
    }

    public static String decryptSymmetricFromBase64(SymmetricAlgorithm algorithm,
                                                    String keyBase64,
                                                    String ivBase64,
                                                    String ciphertextBase64) {
        CryptoCodecUtils.requireText(keyBase64, "keyBase64");
        CryptoCodecUtils.requireText(ivBase64, "ivBase64");
        CryptoCodecUtils.requireText(ciphertextBase64, "ciphertextBase64");
        return DEFAULT_SERVICE.decryptFromBase64(
                algorithm,
                CryptoCodecUtils.base64Decode(keyBase64),
                CryptoCodecUtils.base64Decode(ivBase64),
                ciphertextBase64
        );
    }

    public static CryptoKeyPair generateKeyPair(AsymmetricAlgorithm algorithm) {
        KeyPair keyPair = DEFAULT_SERVICE.generateKeyPair(algorithm);
        return encodeKeyPair(algorithm, keyPair);
    }

    public static CryptoKeyPair generateRsaKeyPair(int keySize) {
        KeyPair keyPair = DEFAULT_SERVICE.generateRsaKeyPair(keySize);
        return encodeKeyPair(AsymmetricAlgorithm.RSA, keyPair);
    }

    public static CryptoKeyPair generateEcKeyPair(String curveName) {
        KeyPair keyPair = DEFAULT_SERVICE.generateEcKeyPair(curveName);
        return encodeKeyPair(AsymmetricAlgorithm.ECC, keyPair);
    }

    public static CryptoKeyPair generateSm2KeyPair() {
        KeyPair keyPair = DEFAULT_SERVICE.generateSm2KeyPair();
        return encodeKeyPair(AsymmetricAlgorithm.SM2, keyPair);
    }

    public static String encryptAsymmetricToBase64(AsymmetricAlgorithm algorithm,
                                                   String publicKeyBase64,
                                                   String plaintext) {
        PublicKey publicKey = DEFAULT_SERVICE.decodePublicKey(algorithm, publicKeyBase64);
        return DEFAULT_SERVICE.encryptToBase64(algorithm, publicKey, CryptoCodecUtils.utf8(plaintext));
    }

    public static String decryptAsymmetricFromBase64(AsymmetricAlgorithm algorithm,
                                                     String privateKeyBase64,
                                                     String ciphertextBase64) {
        PrivateKey privateKey = DEFAULT_SERVICE.decodePrivateKey(algorithm, privateKeyBase64);
        byte[] plaintext = DEFAULT_SERVICE.decryptFromBase64(algorithm, privateKey, ciphertextBase64);
        return CryptoCodecUtils.utf8(plaintext);
    }

    public static HybridCiphertext hybridEncrypt(SymmetricAlgorithm symmetricAlgorithm,
                                                 AsymmetricAlgorithm asymmetricAlgorithm,
                                                 String publicKeyBase64,
                                                 String plaintext) {
        PublicKey publicKey = DEFAULT_SERVICE.decodePublicKey(asymmetricAlgorithm, publicKeyBase64);
        return DEFAULT_SERVICE.hybridEncrypt(
                symmetricAlgorithm,
                asymmetricAlgorithm,
                publicKey,
                CryptoCodecUtils.utf8(plaintext)
        );
    }

    public static String hybridDecrypt(HybridCiphertext ciphertext, String privateKeyBase64) {
        if (ciphertext == null) {
            throw new CryptoException("ciphertext must not be null");
        }
        PrivateKey privateKey = DEFAULT_SERVICE.decodePrivateKey(ciphertext.getAsymmetricAlgorithm(), privateKeyBase64);
        return CryptoCodecUtils.utf8(DEFAULT_SERVICE.hybridDecrypt(ciphertext, privateKey));
    }

    public static String digestHex(DigestAlgorithm algorithm, String plaintext) {
        return DEFAULT_SERVICE.digestHex(algorithm, CryptoCodecUtils.utf8(plaintext));
    }

    public static String md5Hex(String plaintext) {
        return digestHex(DigestAlgorithm.MD5, plaintext);
    }

    public static String sha256Hex(String plaintext) {
        return digestHex(DigestAlgorithm.SHA256, plaintext);
    }

    public static String sha3_256Hex(String plaintext) {
        return digestHex(DigestAlgorithm.SHA3_256, plaintext);
    }

    public static String sm3Hex(String plaintext) {
        return digestHex(DigestAlgorithm.SM3, plaintext);
    }

    private static CryptoKeyPair encodeKeyPair(AsymmetricAlgorithm algorithm, KeyPair keyPair) {
        return new CryptoKeyPair(
                algorithm,
                DEFAULT_SERVICE.encodePublicKey(keyPair.getPublic()),
                DEFAULT_SERVICE.encodePrivateKey(keyPair.getPrivate())
        );
    }
}
