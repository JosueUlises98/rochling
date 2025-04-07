package org.kopingenieria.domain.model.user;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
public class UserOpcUa implements Serializable {
    @Serial
    private static final long serialVersionUID = 7L;

    private final String id = UUID.randomUUID().toString();
    private String name;
    private UserConnectionConfiguration connection;
    private UserEncryptionConfiguration encryption;
    private UserAuthenticationConfiguration authentication;
    private UserIndustrialConfiguration industrial;
    private UserSessionConfiguration session;
}
