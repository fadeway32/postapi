# postcryption-spring-boot-starter

`postcryption-spring-boot-starter` is the cryptography toolkit used by `postapiAll` to protect secrets and build encrypted API mock calls.

Java 8 + Spring Boot 2.x starter for common cryptography operations:

- Symmetric encryption: AES, SM4, DES, 3DES
- Asymmetric encryption: RSA, ECC/ECIES, SM2
- Digest: MD5, SHA-256, SHA3-256, SM3
- Hybrid encryption: asymmetric encryption protects a random symmetric key; symmetric encryption protects the payload

## Usage

Add the starter, then inject `CryptoService`.

```java
@Autowired
private CryptoService cryptoService;
```

```java
SecretKey dataKey = cryptoService.generateSymmetricKey(SymmetricAlgorithm.AES);
byte[] iv = cryptoService.randomIv(SymmetricAlgorithm.AES);

String encrypted = cryptoService.encryptToBase64(
        SymmetricAlgorithm.AES,
        dataKey.getEncoded(),
        iv,
        "payload"
);

String plain = cryptoService.decryptFromBase64(
        SymmetricAlgorithm.AES,
        dataKey.getEncoded(),
        iv,
        encrypted
);
```

Hybrid encryption:

```java
KeyPair keyPair = cryptoService.generateKeyPair(AsymmetricAlgorithm.RSA);
HybridCiphertext ciphertext = cryptoService.hybridEncrypt(
        SymmetricAlgorithm.AES,
        AsymmetricAlgorithm.RSA,
        keyPair.getPublic(),
        "payload".getBytes(StandardCharsets.UTF_8)
);
byte[] plain = cryptoService.hybridDecrypt(ciphertext, keyPair.getPrivate());
```

Key import/export:

```java
String publicKey = cryptoService.encodePublicKey(keyPair.getPublic());
String privateKey = cryptoService.encodePrivateKey(keyPair.getPrivate());

PublicKey decodedPublicKey = cryptoService.decodePublicKey(AsymmetricAlgorithm.RSA, publicKey);
PrivateKey decodedPrivateKey = cryptoService.decodePrivateKey(AsymmetricAlgorithm.RSA, privateKey);
```

Static util facade:

```java
SymmetricCiphertext aes = SymmetricCryptoUtils.encrypt(SymmetricAlgorithm.AES, "payload");
String plain = SymmetricCryptoUtils.decrypt(aes);

CryptoKeyPair rsa = AsymmetricCryptoUtils.generateRsaKeyPair(2048);
String cipher = AsymmetricCryptoUtils.encryptToBase64(AsymmetricAlgorithm.RSA, rsa.getPublicKey(), "payload");
String data = AsymmetricCryptoUtils.decryptFromBase64(AsymmetricAlgorithm.RSA, rsa.getPrivateKey(), cipher);

HybridCiphertext envelope = AsymmetricCryptoUtils.hybridEncrypt(
        SymmetricAlgorithm.AES,
        AsymmetricAlgorithm.RSA,
        rsa.getPublicKey(),
        "payload"
);
String envelopePlain = AsymmetricCryptoUtils.hybridDecrypt(envelope, rsa.getPrivateKey());

String sm3 = DigestUtils.sm3Hex("payload");
```

All-in-one direct facade:

```java
SymmetricCiphertext aes = PostcryptionUtils.aesEncrypt("payload");
String aesPlain = PostcryptionUtils.aesDecrypt(aes);

CryptoKeyPair rsa = PostcryptionUtils.generateRsaKeyPair();
String rsaCipher = PostcryptionUtils.rsaEncryptToBase64(rsa.getPublicKey(), "payload");
String rsaPlain = PostcryptionUtils.rsaDecryptFromBase64(rsa.getPrivateKey(), rsaCipher);

HybridCiphertext envelope = PostcryptionUtils.hybridAesRsaEncrypt(rsa.getPublicKey(), "payload");
String envelopePlain = PostcryptionUtils.hybridAesRsaDecrypt(envelope, rsa.getPrivateKey());

String sha256 = PostcryptionUtils.sha256Hex("payload");
```

## Configuration

```yaml
postcryption:
  enabled: true
  register-bouncy-castle: true
  default-symmetric: AES
  default-asymmetric: RSA
  default-digest: SHA256
  rsa-key-size: 2048
  ec-curve-name: secp256r1
  test-controller:
    enabled: false
    path: /postcryption/test
  mock-controller:
    enabled: false
    path: /postadmin/postcryption/mock
```

## Optional test controller

For local verification in a Spring MVC application, enable the optional controller:

```yaml
postcryption:
  test-controller:
    enabled: true
```

Available endpoints:

- `GET /postcryption/test/algorithms`: lists supported symmetric, asymmetric and digest parameters.
- `GET /postcryption/test/examples?plaintext=hello`: runs all symmetric, asymmetric, digest and hybrid examples and returns whether decrypted content matches the plaintext.
- `POST /postcryption/test/symmetric`: accepts `algorithm`, `plaintext`, optional Base64 `key`, optional Base64 `iv`, and optional Base64 `ciphertext`.
- `POST /postcryption/test/asymmetric`: accepts `algorithm`, `plaintext`, optional Base64 `publicKey`, optional Base64 `privateKey`, and optional Base64 `ciphertext`.
- `POST /postcryption/test/hybrid`: accepts `symmetricAlgorithm`, `asymmetricAlgorithm`, `plaintext`, optional Base64 key pair, and optional `ciphertext` envelope.
- `POST /postcryption/test/digest`: accepts `algorithm` and `plaintext`.

## Optional postadmin mock controller

Enable `com.fadeway32.postadmin.controller.PostcryptionMockController` for frontend mock API testing:

```yaml
postcryption:
  mock-controller:
    enabled: true
```

Each endpoint validates the encrypted or digest parameters first. When validation passes it returns `success=true` and a mock `data` object. When validation fails it returns `success=false` with `code=VALIDATION_FAILED`.

Endpoints:

- Symmetric: `POST /postadmin/postcryption/mock/aes`, `/sm4`, `/sms4`, `/des`, `/des3`, `/3des`
- Asymmetric: `POST /postadmin/postcryption/mock/rsa`, `/ecc`, `/sm2`
- Digest: `POST /postadmin/postcryption/mock/md5`, `/sha256`, `/sha3-256`, `/sm3`
- Hybrid: `POST /postadmin/postcryption/mock/hybrid/{symmetric}-{asymmetric}`, covering `aes|sm4|des|des3` with `rsa|ecc|sm2`

Request fields:

- Symmetric validation uses `plaintext`, Base64 `key`, Base64 `iv`, and Base64 `ciphertext`.
- Asymmetric validation uses `plaintext` and `publicKey` for public-key payload checks, or `plaintext`, `privateKey`, and Base64 `ciphertext` for decrypt-and-compare checks.
- Digest validation uses `plaintext` and one of `digest`, `sign`, or `signature`.
- Hybrid validation uses `plaintext`, `privateKey`, `encryptedKey`, `iv`, and `ciphertext`.

Groovy mock caller scripts are provided under `src/main/resources/postadmin/groovy`. Each script is a standalone API call example: it uses `com.fadeway32.crypto.util.PostcryptionUtils` to build encrypted parameters, Jackson `ObjectMapper` to serialize the request, and `com.fadeway32.postapi.util.PostApiUtils` to call the mock endpoint.

The scripts are written for the postadmin Groovy sandbox. They only import whitelisted postcryption utility classes. Jackson and PostApiUtils are referenced by fully qualified class names to avoid sandbox import failures.

If the Groovy execution result reports `ClassNotFoundException: com.fadeway32.crypto.util.PostcryptionUtils`, the postadmin application runtime does not include this starter. Add the dependency to the application that executes `/api/groovy/sumulate`, install this starter if it is still local, then restart postadmin:

```xml
<dependency>
    <groupId>com.fadeway32.postcryption</groupId>
    <artifactId>postcryption-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```powershell
mvn install
```

Run `postadmin/groovy/check-postcryption-classpath.groovy` first. It should return `loaded=true` and the MD5 value for `abc`.

Example from the project root:

```powershell
mvn -q dependency:build-classpath -Dmdep.outputFile=target/runtime-classpath.txt
$cp = "target/classes;" + (Get-Content target/runtime-classpath.txt)
groovy -cp $cp src/main/resources/postadmin/groovy/aes.groovy
```

Override the target service when needed:

```powershell
groovy -cp $cp src/main/resources/postadmin/groovy/md5.groovy
```

By default scripts call `PostApiUtils.postJson(url, jsonString)`. Each file also contains a commented `PostApiUtils.postJson(url, map)` line for Map body calls. The `com.fadeway32.postapi` module must be on the Groovy runtime classpath.

Prefer AES-GCM or authenticated envelope formats for high-security production protocols. This starter exposes CBC/PKCS5Padding for broad Java 8 compatibility and includes hybrid encryption primitives that can be wrapped into an application-level signed or authenticated protocol.
