package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.domain.enums.security.SecurityPolicyUri;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class DefaultAuthenticationConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    private final IdentityProvider identityProvider;
    private final String userName;
    private final String password;
    private final SecurityPolicy securityPolicy;
    private final MessageSecurityMode messageSecurityMode;
    private final String certificatePath;
    private final String privateKeyPath;
    private final String trustListPath;
    private final String issuerListPath;
    private final String revocationListPath;
    private final SecurityPolicyUri securityPolicyUri;
    private final int expirationWarningDays = 30;
}
