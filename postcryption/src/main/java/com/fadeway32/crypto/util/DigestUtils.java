package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.DigestAlgorithm;

public final class DigestUtils {

    private DigestUtils() {
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
