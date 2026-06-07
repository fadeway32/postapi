package com.fadeway32.crypto.core;

public enum DigestAlgorithm {
    MD5("MD5", null),
    SHA256("SHA-256", null),
    SHA3_256("SHA3-256", BouncyCastleSupport.PROVIDER_NAME),
    SM3("SM3", BouncyCastleSupport.PROVIDER_NAME);

    private final String algorithm;
    private final String provider;

    DigestAlgorithm(String algorithm, String provider) {
        this.algorithm = algorithm;
        this.provider = provider;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getProvider() {
        return provider;
    }
}
