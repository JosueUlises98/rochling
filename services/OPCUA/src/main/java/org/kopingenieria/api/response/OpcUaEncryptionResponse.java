package org.kopingenieria.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpcUaEncryptionResponse {
    private String securityPolicy;
    private String messageSecurityMode;
    private boolean hasCertificate;
    private boolean hasPrivateKey;
    private int trustedCertificatesCount;
    private String keyLength;
    private String algorithmName;
    private String protocolVersion;
}
