package org.kopingenieria.domain.model.user;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.domain.enums.security.SecurityPolicyUri;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class UserAuthenticationConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    private IdentityProvider identityProvider;
    private String userName;
    private String password;
    private SecurityPolicy securityPolicy;
    private MessageSecurityMode messageSecurityMode;
    private String certificatePath;
    private String privateKeyPath;
    private String trustListPath;
    private String issuerListPath;
    private String revocationListPath;
    private SecurityPolicyUri securityPolicyUri;
    private final int expirationWarningDays = 30;
}
