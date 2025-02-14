package org.kopingenieria.model;

import jakarta.validation.constraints.NotBlank;
import org.kopingenieria.services.SSLConnection;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "ssl")
@Validated
public class SSLConfigurations {
    @NotBlank
    private String keystorePath;

    @NotBlank
    private String keystorePassword;

    @NotBlank
    private String truststorePath;

    @NotBlank
    private String truststorePassword;

    @NotBlank
    private String keystoreType = "PKCS12";

    @NotBlank
    private String truststoreType = "PKCS12";

    public String keystorePath() {
        return keystorePath;
    }

    public SSLConfigurations setKeystorePath(String keystorePath) {
        this.keystorePath = keystorePath;
        return this;
    }

    public String keystorePassword() {
        return keystorePassword;
    }

    public SSLConfigurations setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        return this;
    }

    public String truststorePath() {
        return truststorePath;
    }

    public SSLConfigurations setTruststorePath(String truststorePath) {
        this.truststorePath = truststorePath;
        return this;
    }

    public String truststorePassword() {
        return truststorePassword;
    }

    public SSLConfigurations setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
        return this;
    }

    public String keystoreType() {
        return keystoreType;
    }

    public SSLConfigurations setKeystoreType(String keystoreType) {
        this.keystoreType = keystoreType;
        return this;
    }

    public String truststoreType() {
        return truststoreType;
    }

    public SSLConfigurations setTruststoreType(String truststoreType) {
        this.truststoreType = truststoreType;
        return this;
    }

    @Bean
    public SSLConnection secureSSLConnection() {
        return new SSLConnection(this);
    }
}
