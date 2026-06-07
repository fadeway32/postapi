package com.fadeway32.crypto.core;

public enum AsymmetricAlgorithm {
    RSA("RSA", "RSA/ECB/OAEPWithSHA-256AndMGF1Padding", null, 2048, null),
    ECC("EC", "ECIES", BouncyCastleSupport.PROVIDER_NAME, 256, "secp256r1"),
    SM2("EC", "SM2", BouncyCastleSupport.PROVIDER_NAME, 256, "sm2p256v1");

    private final String keyAlgorithm;
    private final String transformation;
    private final String provider;
    private final int defaultKeySize;
    private final String defaultCurveName;

    AsymmetricAlgorithm(String keyAlgorithm, String transformation, String provider, int defaultKeySize, String defaultCurveName) {
        this.keyAlgorithm = keyAlgorithm;
        this.transformation = transformation;
        this.provider = provider;
        this.defaultKeySize = defaultKeySize;
        this.defaultCurveName = defaultCurveName;
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

    public int getDefaultKeySize() {
        return defaultKeySize;
    }

    public String getDefaultCurveName() {
        return defaultCurveName;
    }
}
