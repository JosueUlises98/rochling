package org.kopingenieria.domain.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

@Embeddable
@Getter
@Setter
public class OpcUaSecurityConfiguration {
    private boolean autoAcceptServers;
    private String trustListPath;
    private String issuerListPath;
    private String revocationListPath;
    private Integer keyPairLifetime;
    private Integer certificateLifetime;
    private String[] allowedDomains;
    private SecurityPolicy[] allowedSecurityPolicies;
}
