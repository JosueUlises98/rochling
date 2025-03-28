package org.kopingenieria.domain.dto;

import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

public record AuthenticationDTO(IdentityProvider identityProvider, String userName,
                                String password,
                                SecurityPolicy securityPolicy,
                                MessageSecurityMode messageSecurityMode,
                                String certificatePath,
                                String privateKeyPath,
                                String trustListPath,
                                String issuerListPath,
                                String revocationListPath) {
}
