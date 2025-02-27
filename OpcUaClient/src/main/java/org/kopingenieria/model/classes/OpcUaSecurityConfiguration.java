package org.kopingenieria.model.classes;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.model.enums.opcua.communication.SecurityPolicy;

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
