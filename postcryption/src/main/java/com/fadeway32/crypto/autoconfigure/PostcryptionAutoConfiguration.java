package com.fadeway32.crypto.autoconfigure;

import com.fadeway32.crypto.core.BouncyCastleSupport;
import com.fadeway32.crypto.core.CryptoService;
import com.fadeway32.crypto.core.DefaultCryptoService;
import com.fadeway32.postadmin.controller.PostcryptionMockController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(CryptoService.class)
@EnableConfigurationProperties(CryptoProperties.class)
@ConditionalOnProperty(prefix = "postcryption", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PostcryptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CryptoService cryptoService(CryptoProperties properties) {
        if (properties.isRegisterBouncyCastle()) {
            BouncyCastleSupport.ensureRegistered();
        }
        return new DefaultCryptoService(properties);
    }

    @Configuration
    @ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
    @ConditionalOnProperty(prefix = "postcryption.mock-controller", name = "enabled", havingValue = "true")
    public static class MockControllerConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public PostcryptionMockController postcryptionMockController(CryptoService cryptoService) {
            return new PostcryptionMockController(cryptoService);
        }
    }

}
