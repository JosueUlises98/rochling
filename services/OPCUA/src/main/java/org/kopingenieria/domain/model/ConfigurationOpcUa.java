package org.kopingenieria.domain.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class ConfigurationOpcUa implements Serializable {
    @Serial
    private static final long serialVersionUID = 7L;

    private ConnectionConfiguration connection;
    private EncryptionConfiguration encryption;
    private IndustrialConfiguration industrial;
    private AuthenticationConfiguration authentication;
    private SubscriptionConfiguration subscription;
    private SessionConfiguration session;
}
