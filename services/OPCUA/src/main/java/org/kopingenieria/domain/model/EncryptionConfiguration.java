package org.kopingenieria.domain.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class EncryptionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;
    private String securityPolicy;
    private String messageSecurityMode;
    private byte[] clientCertificate;
    private byte[] privateKey;
    private List<byte[]> trustedCertificates;
    private String keyLength;
    private String algorithmName;
    private String protocolVersion;
}
