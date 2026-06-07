package com.fadeway32.crypto.core;

import com.fadeway32.crypto.autoconfigure.CryptoProperties;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class DefaultCryptoService implements CryptoService {

    private final CryptoProperties properties;
    private final SecureRandom secureRandom;

    public DefaultCryptoService(CryptoProperties properties) {
        this.properties = properties == null ? new CryptoProperties() : properties;
        this.secureRandom = new SecureRandom();
        if (this.properties.isRegisterBouncyCastle()) {
            BouncyCastleSupport.ensureRegistered();
        }
    }

    @Override
    public SecretKey generateSymmetricKey(SymmetricAlgorithm algorithm) {
        requireNonNull(algorithm, "algorithm");
        try {
            KeyGenerator keyGenerator = algorithm.getProvider() == null
                    ? KeyGenerator.getInstance(algorithm.getKeyAlgorithm())
                    : KeyGenerator.getInstance(algorithm.getKeyAlgorithm(), algorithm.getProvider());
            keyGenerator.init(algorithm.getDefaultKeyBytes() * 8, secureRandom);
            return keyGenerator.generateKey();
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to generate symmetric key for " + algorithm, ex);
        }
    }

    @Override
    public byte[] randomIv(SymmetricAlgorithm algorithm) {
        requireNonNull(algorithm, "algorithm");
        byte[] iv = new byte[algorithm.getIvBytes()];
        secureRandom.nextBytes(iv);
        return iv;
    }

    @Override
    public KeyPair generateKeyPair(AsymmetricAlgorithm algorithm) {
        requireNonNull(algorithm, "algorithm");
        if (algorithm == AsymmetricAlgorithm.RSA) {
            return generateRsaKeyPair(properties.getRsaKeySize());
        }
        if (algorithm == AsymmetricAlgorithm.SM2) {
            return generateSm2KeyPair();
        }
        return generateEcKeyPair(properties.getEcCurveName());
    }

    @Override
    public KeyPair generateRsaKeyPair(int keySize) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(keySize, secureRandom);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to generate RSA key pair", ex);
        }
    }

    @Override
    public KeyPair generateEcKeyPair(String curveName) {
        return generateEcKeyPair(curveName == null || curveName.trim().isEmpty() ? "secp256r1" : curveName, "EC key pair");
    }

    @Override
    public KeyPair generateSm2KeyPair() {
        return generateEcKeyPair("sm2p256v1", "SM2 key pair");
    }

    @Override
    public String encodePublicKey(PublicKey publicKey) {
        requireNonNull(publicKey, "publicKey");
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    @Override
    public String encodePrivateKey(PrivateKey privateKey) {
        requireNonNull(privateKey, "privateKey");
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    @Override
    public PublicKey decodePublicKey(AsymmetricAlgorithm algorithm, String publicKeyBase64) {
        requireNonNull(algorithm, "algorithm");
        requireNonNull(publicKeyBase64, "publicKeyBase64");
        try {
            return keyFactory(algorithm).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to decode public key for " + algorithm, ex);
        }
    }

    @Override
    public PrivateKey decodePrivateKey(AsymmetricAlgorithm algorithm, String privateKeyBase64) {
        requireNonNull(algorithm, "algorithm");
        requireNonNull(privateKeyBase64, "privateKeyBase64");
        try {
            return keyFactory(algorithm).generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to decode private key for " + algorithm, ex);
        }
    }

    @Override
    public byte[] encrypt(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, byte[] plaintext) {
        return doSymmetric(Cipher.ENCRYPT_MODE, algorithm, key, iv, plaintext);
    }

    @Override
    public byte[] decrypt(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, byte[] ciphertext) {
        return doSymmetric(Cipher.DECRYPT_MODE, algorithm, key, iv, ciphertext);
    }

    @Override
    public String encryptToBase64(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, String plaintext) {
        requireNonNull(plaintext, "plaintext");
        byte[] encrypted = encrypt(algorithm, key, iv, plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    @Override
    public String decryptFromBase64(SymmetricAlgorithm algorithm, byte[] key, byte[] iv, String ciphertextBase64) {
        requireNonNull(ciphertextBase64, "ciphertextBase64");
        byte[] ciphertext = Base64.getDecoder().decode(ciphertextBase64);
        byte[] decrypted = decrypt(algorithm, key, iv, ciphertext);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    @Override
    public byte[] encrypt(AsymmetricAlgorithm algorithm, PublicKey publicKey, byte[] plaintext) {
        return doAsymmetric(Cipher.ENCRYPT_MODE, algorithm, publicKey, plaintext);
    }

    @Override
    public byte[] decrypt(AsymmetricAlgorithm algorithm, PrivateKey privateKey, byte[] ciphertext) {
        return doAsymmetric(Cipher.DECRYPT_MODE, algorithm, privateKey, ciphertext);
    }

    @Override
    public String encryptToBase64(AsymmetricAlgorithm algorithm, PublicKey publicKey, byte[] plaintext) {
        return Base64.getEncoder().encodeToString(encrypt(algorithm, publicKey, plaintext));
    }

    @Override
    public byte[] decryptFromBase64(AsymmetricAlgorithm algorithm, PrivateKey privateKey, String ciphertextBase64) {
        requireNonNull(ciphertextBase64, "ciphertextBase64");
        return decrypt(algorithm, privateKey, Base64.getDecoder().decode(ciphertextBase64));
    }

    @Override
    public HybridCiphertext hybridEncrypt(SymmetricAlgorithm symmetricAlgorithm,
                                          AsymmetricAlgorithm asymmetricAlgorithm,
                                          PublicKey publicKey,
                                          byte[] plaintext) {
        requireNonNull(plaintext, "plaintext");
        SecretKey dataKey = generateSymmetricKey(symmetricAlgorithm);
        byte[] iv = randomIv(symmetricAlgorithm);
        byte[] encryptedKey = encrypt(asymmetricAlgorithm, publicKey, dataKey.getEncoded());
        byte[] encryptedPayload = encrypt(symmetricAlgorithm, dataKey.getEncoded(), iv, plaintext);
        return new HybridCiphertext(
                symmetricAlgorithm,
                asymmetricAlgorithm,
                Base64.getEncoder().encodeToString(encryptedKey),
                Base64.getEncoder().encodeToString(iv),
                Base64.getEncoder().encodeToString(encryptedPayload)
        );
    }

    @Override
    public byte[] hybridDecrypt(HybridCiphertext ciphertext, PrivateKey privateKey) {
        requireNonNull(ciphertext, "ciphertext");
        SymmetricAlgorithm symmetricAlgorithm = requireNonNull(ciphertext.getSymmetricAlgorithm(), "symmetricAlgorithm");
        AsymmetricAlgorithm asymmetricAlgorithm = requireNonNull(ciphertext.getAsymmetricAlgorithm(), "asymmetricAlgorithm");
        byte[] dataKey = decrypt(asymmetricAlgorithm, privateKey, Base64.getDecoder().decode(ciphertext.getEncryptedKey()));
        byte[] iv = Base64.getDecoder().decode(ciphertext.getIv());
        byte[] encryptedPayload = Base64.getDecoder().decode(ciphertext.getCiphertext());
        return decrypt(symmetricAlgorithm, dataKey, iv, encryptedPayload);
    }

    @Override
    public String digestHex(DigestAlgorithm algorithm, byte[] data) {
        requireNonNull(algorithm, "algorithm");
        requireNonNull(data, "data");
        try {
            MessageDigest digest = algorithm.getProvider() == null
                    ? MessageDigest.getInstance(algorithm.getAlgorithm())
                    : MessageDigest.getInstance(algorithm.getAlgorithm(), algorithm.getProvider());
            return Hex.encode(digest.digest(data));
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to calculate digest using " + algorithm, ex);
        }
    }

    private KeyPair generateEcKeyPair(String curveName, String usage) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", BouncyCastleSupport.PROVIDER_NAME);
            generator.initialize(new ECGenParameterSpec(curveName), secureRandom);
            return generator.generateKeyPair();
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to generate " + usage + " with curve " + curveName, ex);
        }
    }

    private byte[] doSymmetric(int mode, SymmetricAlgorithm algorithm, byte[] key, byte[] iv, byte[] input) {
        requireNonNull(algorithm, "algorithm");
        requireNonNull(key, "key");
        requireNonNull(iv, "iv");
        requireNonNull(input, "input");
        if (iv.length != algorithm.getIvBytes()) {
            throw new CryptoException("Invalid IV length for " + algorithm + ": expected "
                    + algorithm.getIvBytes() + " bytes, got " + iv.length);
        }
        try {
            Cipher cipher = newCipher(algorithm.getTransformation(), algorithm.getProvider());
            cipher.init(mode, new SecretKeySpec(key, algorithm.getKeyAlgorithm()), new IvParameterSpec(iv));
            return cipher.doFinal(input);
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to process symmetric cipher " + algorithm, ex);
        }
    }

    private byte[] doAsymmetric(int mode, AsymmetricAlgorithm algorithm, java.security.Key key, byte[] input) {
        requireNonNull(algorithm, "algorithm");
        requireNonNull(key, "key");
        requireNonNull(input, "input");
        try {
            Cipher cipher = newCipher(algorithm.getTransformation(), algorithm.getProvider());
            cipher.init(mode, key, secureRandom);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException ex) {
            throw new CryptoException("Failed to process asymmetric cipher " + algorithm, ex);
        }
    }

    private Cipher newCipher(String transformation, String provider) throws GeneralSecurityException {
        return provider == null ? Cipher.getInstance(transformation) : Cipher.getInstance(transformation, provider);
    }

    private java.security.KeyFactory keyFactory(AsymmetricAlgorithm algorithm) throws GeneralSecurityException {
        return algorithm.getProvider() == null
                ? java.security.KeyFactory.getInstance(algorithm.getKeyAlgorithm())
                : java.security.KeyFactory.getInstance(algorithm.getKeyAlgorithm(), algorithm.getProvider());
    }

    private static <T> T requireNonNull(T value, String name) {
        if (value == null) {
            throw new CryptoException(name + " must not be null");
        }
        return value;
    }
}
