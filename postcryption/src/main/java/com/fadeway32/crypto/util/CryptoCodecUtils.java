package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.CryptoException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public final class CryptoCodecUtils {

    private CryptoCodecUtils() {
    }

    public static byte[] utf8(String value) {
        if (value == null) {
            throw new CryptoException("value must not be null");
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static String utf8(byte[] value) {
        if (value == null) {
            throw new CryptoException("value must not be null");
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public static String base64Encode(byte[] value) {
        if (value == null) {
            throw new CryptoException("value must not be null");
        }
        return Base64.getEncoder().encodeToString(value);
    }

    public static byte[] base64Decode(String value) {
        if (value == null) {
            throw new CryptoException("value must not be null");
        }
        return Base64.getDecoder().decode(value);
    }

    public static void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new CryptoException(name + " must not be blank");
        }
    }
}
