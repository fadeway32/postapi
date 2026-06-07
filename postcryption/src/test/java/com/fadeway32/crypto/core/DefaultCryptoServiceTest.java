package com.fadeway32.crypto.core;

import com.fadeway32.crypto.autoconfigure.CryptoProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultCryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new DefaultCryptoService(new CryptoProperties());
    }

    @Test
    void encryptsAndDecryptsAes() {
        SecretKey key = cryptoService.generateSymmetricKey(SymmetricAlgorithm.AES);
        byte[] iv = cryptoService.randomIv(SymmetricAlgorithm.AES);

        String encrypted = cryptoService.encryptToBase64(SymmetricAlgorithm.AES, key.getEncoded(), iv, "hello aes");

        assertThat(cryptoService.decryptFromBase64(SymmetricAlgorithm.AES, key.getEncoded(), iv, encrypted))
                .isEqualTo("hello aes");
    }

    @Test
    void encryptsAndDecryptsSm4() {
        SecretKey key = cryptoService.generateSymmetricKey(SymmetricAlgorithm.SM4);
        byte[] iv = cryptoService.randomIv(SymmetricAlgorithm.SM4);

        byte[] encrypted = cryptoService.encrypt(SymmetricAlgorithm.SM4, key.getEncoded(), iv, bytes("hello sm4"));

        assertThat(cryptoService.decrypt(SymmetricAlgorithm.SM4, key.getEncoded(), iv, encrypted))
                .isEqualTo(bytes("hello sm4"));
    }

    @Test
    void encryptsAndDecryptsDesAndTripleDes() {
        assertSymmetricRoundTrip(SymmetricAlgorithm.DES, "hello des");
        assertSymmetricRoundTrip(SymmetricAlgorithm.DES3, "hello 3des");
    }

    @Test
    void encryptsAndDecryptsWithRsa() {
        KeyPair keyPair = cryptoService.generateRsaKeyPair(2048);

        byte[] encrypted = cryptoService.encrypt(AsymmetricAlgorithm.RSA, keyPair.getPublic(), bytes("hello rsa"));

        assertThat(cryptoService.decrypt(AsymmetricAlgorithm.RSA, keyPair.getPrivate(), encrypted))
                .isEqualTo(bytes("hello rsa"));
    }

    @Test
    void encryptsAndDecryptsWithEcc() {
        KeyPair keyPair = cryptoService.generateEcKeyPair("secp256r1");

        byte[] encrypted = cryptoService.encrypt(AsymmetricAlgorithm.ECC, keyPair.getPublic(), bytes("hello ecc"));

        assertThat(cryptoService.decrypt(AsymmetricAlgorithm.ECC, keyPair.getPrivate(), encrypted))
                .isEqualTo(bytes("hello ecc"));
    }

    @Test
    void encryptsAndDecryptsWithSm2() {
        KeyPair keyPair = cryptoService.generateSm2KeyPair();

        byte[] encrypted = cryptoService.encrypt(AsymmetricAlgorithm.SM2, keyPair.getPublic(), bytes("hello sm2"));

        assertThat(cryptoService.decrypt(AsymmetricAlgorithm.SM2, keyPair.getPrivate(), encrypted))
                .isEqualTo(bytes("hello sm2"));
    }

    @Test
    void encodesAndDecodesKeyPairs() {
        KeyPair keyPair = cryptoService.generateRsaKeyPair(2048);
        String publicKey = cryptoService.encodePublicKey(keyPair.getPublic());
        String privateKey = cryptoService.encodePrivateKey(keyPair.getPrivate());

        byte[] encrypted = cryptoService.encrypt(
                AsymmetricAlgorithm.RSA,
                cryptoService.decodePublicKey(AsymmetricAlgorithm.RSA, publicKey),
                bytes("key codec")
        );

        assertThat(cryptoService.decrypt(
                AsymmetricAlgorithm.RSA,
                cryptoService.decodePrivateKey(AsymmetricAlgorithm.RSA, privateKey),
                encrypted
        )).isEqualTo(bytes("key codec"));
    }

    @Test
    void hybridEncryptsPayloadWithTransportedKey() {
        KeyPair keyPair = cryptoService.generateRsaKeyPair(2048);
        byte[] plaintext = bytes("hybrid payload with random content");

        HybridCiphertext ciphertext = cryptoService.hybridEncrypt(
                SymmetricAlgorithm.AES,
                AsymmetricAlgorithm.RSA,
                keyPair.getPublic(),
                plaintext
        );

        assertThat(ciphertext.getEncryptedKey()).isNotBlank();
        assertThat(ciphertext.getIv()).isNotBlank();
        assertThat(ciphertext.getCiphertext()).isNotBlank();
        assertThat(cryptoService.hybridDecrypt(ciphertext, keyPair.getPrivate())).isEqualTo(plaintext);
    }

    @Test
    void calculatesDigests() {
        assertThat(cryptoService.digestHex(DigestAlgorithm.MD5, bytes("abc")))
                .isEqualTo("900150983cd24fb0d6963f7d28e17f72");
        assertThat(cryptoService.digestHex(DigestAlgorithm.SHA256, bytes("abc")))
                .isEqualTo("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad");
        assertThat(cryptoService.digestHex(DigestAlgorithm.SM3, bytes("abc")))
                .isEqualTo("66c7f0f462eeedd9d1f2d46bdc10e4e24167c4875cf2f7a2297da02b8f4ba8e0");
        assertThat(cryptoService.digestHex(DigestAlgorithm.SHA3_256, bytes("abc")))
                .isEqualTo("3a985da74fe225b2045c172d6bd390bd855f086e3e9d525b46bfe24511431532");
    }

    @Test
    void autoConfigurationCreatesCryptoService() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(com.fadeway32.crypto.autoconfigure.PostcryptionAutoConfiguration.class))
                .run(context -> assertThat(context).hasSingleBean(CryptoService.class));
    }

    private void assertSymmetricRoundTrip(SymmetricAlgorithm algorithm, String plaintext) {
        SecretKey key = cryptoService.generateSymmetricKey(algorithm);
        byte[] iv = cryptoService.randomIv(algorithm);
        byte[] encrypted = cryptoService.encrypt(algorithm, key.getEncoded(), iv, bytes(plaintext));
        assertThat(cryptoService.decrypt(algorithm, key.getEncoded(), iv, encrypted)).isEqualTo(bytes(plaintext));
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
