package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class DefaultConfigurationOpcUa implements Serializable {
    @Serial
    private static final long serialVersionUID = 16L;

    private DefaultConnectionConfiguration connection;
    private DefaultEncryptionConfiguration encryption;
    private DefaultIndustrialConfiguration industrial;
    private DefaultAuthenticationConfiguration authentication;
    private DefaultSubscriptionConfiguration subscription;
    private DefaultSessionConfiguration session;
}
