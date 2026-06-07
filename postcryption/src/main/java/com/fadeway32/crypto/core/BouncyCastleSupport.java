package com.fadeway32.crypto.core;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

public final class BouncyCastleSupport {

    public static final String PROVIDER_NAME = "BC";

    private BouncyCastleSupport() {
    }

    public static void ensureRegistered() {
        if (Security.getProvider(PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }
}
