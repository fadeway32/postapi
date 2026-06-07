package com.fadeway32.crypto.util;

import com.fadeway32.crypto.core.SymmetricAlgorithm;

import java.io.Serializable;

public class SymmetricCiphertext implements Serializable {

    private static final long serialVersionUID = 1L;

    private SymmetricAlgorithm algorithm;

    private String key;

    private String iv;

    private String ciphertext;

    public SymmetricCiphertext() {
    }

    public SymmetricCiphertext(SymmetricAlgorithm algorithm, String key, String iv, String ciphertext) {
        this.algorithm = algorithm;
        this.key = key;
        this.iv = iv;
        this.ciphertext = ciphertext;
    }

    public SymmetricAlgorithm getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(SymmetricAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }
}
