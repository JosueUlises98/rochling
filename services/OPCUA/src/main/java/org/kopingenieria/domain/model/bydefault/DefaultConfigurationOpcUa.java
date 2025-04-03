package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class DefaultConfigurationOpcUa implements Serializable {
    @Serial
    private static final long serialVersionUID = 7L;

    private final DefaultConnectionConfiguration connection;
    private final DefaultEncryptionConfiguration encryption;
    private final DefaultIndustrialConfiguration industrial;
    private final DefaultAuthenticationConfiguration authentication;
    private final DefaultSubscriptionConfiguration subscription;
    private final DefaultSessionConfiguration session;
}
