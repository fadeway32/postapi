package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;

import java.io.Serializable;

public class CryptoKeyPair implements Serializable {

    private static final long serialVersionUID = 1L;

    private AsymmetricAlgorithm algorithm;

    private String publicKey;

    private String privateKey;

    public CryptoKeyPair() {
    }

    public CryptoKeyPair(AsymmetricAlgorithm algorithm, String publicKey, String privateKey) {
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public AsymmetricAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(AsymmetricAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
