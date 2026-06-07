package com.fadeway32.crypto.core;

import java.io.Serializable;

public class HybridCiphertext implements Serializable {

    private static final long serialVersionUID = 1L;

    private SymmetricAlgorithm symmetricAlgorithm;

    private AsymmetricAlgorithm asymmetricAlgorithm;

    private String encryptedKey;

    private String iv;

    private String ciphertext;

    public HybridCiphertext() {
    }

    public HybridCiphertext(SymmetricAlgorithm symmetricAlgorithm,
                            AsymmetricAlgorithm asymmetricAlgorithm,
                            String encryptedKey,
                            String iv,
                            String ciphertext) {
        this.symmetricAlgorithm = symmetricAlgorithm;
        this.asymmetricAlgorithm = asymmetricAlgorithm;
        this.encryptedKey = encryptedKey;
        this.iv = iv;
        this.ciphertext = ciphertext;
    }

    public SymmetricAlgorithm getSymmetricAlgorithm() {
        return symmetricAlgorithm;
    }

    public void setSymmetricAlgorithm(SymmetricAlgorithm symmetricAlgorithm) {
        this.symmetricAlgorithm = symmetricAlgorithm;
    }

    public AsymmetricAlgorithm getAsymmetricAlgorithm() {
        return asymmetricAlgorithm;
    }

    public void setAsymmetricAlgorithm(AsymmetricAlgorithm asymmetricAlgorithm) {
        this.asymmetricAlgorithm = asymmetricAlgorithm;
    }

    public String getEncryptedKey() {
        return encryptedKey;
    }

    public void setEncryptedKey(String encryptedKey) {
        this.encryptedKey = encryptedKey;
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
