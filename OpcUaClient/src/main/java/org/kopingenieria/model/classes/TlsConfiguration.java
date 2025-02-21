package org.kopingenieria.model.classes;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.model.enums.tls.TlsVersion;

@Entity
@Getter
@Setter
public class TlsConfiguration extends Configuration<TlsConfiguration> {

    // Atributos de certificados
    private String keyStorePath;
    private String keyStorePassword;
    private String keyStoreType;
    private String trustStorePath;
    private String trustStorePassword;
    private String trustStoreType;

    // Atributos de protocolo
    private String[] enabledProtocols;
    private String[] enabledCipherSuites;

    private boolean clientAuthRequired;
    private boolean hostnameVerification;

    @Enumerated(EnumType.STRING)
    private TlsVersion minimumTlsVersion;

    private Integer sessionCacheSize;
    private Integer sessionTimeout;

}
