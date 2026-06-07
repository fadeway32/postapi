package com.fadeway32.crypto.core;

public enum SymmetricAlgorithm {
    AES("AES", "AES/CBC/PKCS5Padding", null, 16, 16),
    SM4("SM4", "SM4/CBC/PKCS5Padding", BouncyCastleSupport.PROVIDER_NAME, 16, 16),
    DES("DES", "DES/CBC/PKCS5Padding", null, 8, 8),
    DES3("DESede", "DESede/CBC/PKCS5Padding", null, 24, 8);

    private final String keyAlgorithm;
    private final String transformation;
    private final String provider;
    private final int defaultKeyBytes;
    private final int ivBytes;

    SymmetricAlgorithm(String keyAlgorithm, String transformation, String provider, int defaultKeyBytes, int ivBytes) {
        this.keyAlgorithm = keyAlgorithm;
        this.transformation = transformation;
        this.provider = provider;
        this.defaultKeyBytes = defaultKeyBytes;
        this.ivBytes = ivBytes;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getProvider() {
        return provider;
    }

    public int getDefaultKeyBytes() {
        return defaultKeyBytes;
    }

    public int getIvBytes() {
        return ivBytes;
    }
}
