package com.fadeway32.crypto.autoconfigure;

import com.fadeway32.crypto.core.AsymmetricAlgorithm;
import com.fadeway32.crypto.core.DigestAlgorithm;
import com.fadeway32.crypto.core.SymmetricAlgorithm;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "postcryption")
public class CryptoProperties {

    private boolean enabled = true;

    private boolean registerBouncyCastle = true;

    private SymmetricAlgorithm defaultSymmetric = SymmetricAlgorithm.AES;

    private AsymmetricAlgorithm defaultAsymmetric = AsymmetricAlgorithm.RSA;

    private DigestAlgorithm defaultDigest = DigestAlgorithm.SHA256;

    private int rsaKeySize = 2048;

    private String ecCurveName = "secp256r1";

    private TestController testController = new TestController();

    private MockController mockController = new MockController();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isRegisterBouncyCastle() {
        return registerBouncyCastle;
    }

    public void setRegisterBouncyCastle(boolean registerBouncyCastle) {
        this.registerBouncyCastle = registerBouncyCastle;
    }

    public SymmetricAlgorithm getDefaultSymmetric() {
        return defaultSymmetric;
    }

    public void setDefaultSymmetric(SymmetricAlgorithm defaultSymmetric) {
        this.defaultSymmetric = defaultSymmetric;
    }

    public AsymmetricAlgorithm getDefaultAsymmetric() {
        return defaultAsymmetric;
    }

    public void setDefaultAsymmetric(AsymmetricAlgorithm defaultAsymmetric) {
        this.defaultAsymmetric = defaultAsymmetric;
    }

    public DigestAlgorithm getDefaultDigest() {
        return defaultDigest;
    }

    public void setDefaultDigest(DigestAlgorithm defaultDigest) {
        this.defaultDigest = defaultDigest;
    }

    public int getRsaKeySize() {
        return rsaKeySize;
    }

    public void setRsaKeySize(int rsaKeySize) {
        this.rsaKeySize = rsaKeySize;
    }

    public String getEcCurveName() {
        return ecCurveName;
    }

    public void setEcCurveName(String ecCurveName) {
        this.ecCurveName = ecCurveName;
    }

    public TestController getTestController() {
        return testController;
    }

    public void setTestController(TestController testController) {
        this.testController = testController;
    }

    public MockController getMockController() {
        return mockController;
    }

    public void setMockController(MockController mockController) {
        this.mockController = mockController;
    }

    public static class TestController {

        private boolean enabled = false;

        private String path = "/postcryption/test";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }

    public static class MockController {

        private boolean enabled = false;

        private String path = "/postadmin/postcryption/mock";

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }
    }
}
