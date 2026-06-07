package com.fadeway32.crypto.core;

final class Hex {

    private static final char[] DIGITS = "0123456789abcdef".toCharArray();

    private Hex() {
    }

    static String encode(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xff;
            chars[i * 2] = DIGITS[value >>> 4];
            chars[i * 2 + 1] = DIGITS[value & 0x0f];
        }
        return new String(chars);
    }
}
