package org.kopingenieria.domain.model.user;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class UserConfigurationOpcUa implements Serializable {
    @Serial
    private static final long serialVersionUID = 7L;

    private UserConnectionConfiguration connection;
    private UserEncryptionConfiguration encryption;
    private UserIndustrialConfiguration industrial;
    private UserAuthenticationConfiguration authentication;
    private UserSubscriptionConfiguration subscription;
    private UserSessionConfiguration session;
}
