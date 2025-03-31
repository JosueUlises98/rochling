package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class DefaultEncryptionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 12L;

    private String securityPolicy;
    private String messageSecurityMode;
    private byte[] clientCertificate;
    private byte[] privateKey;
    private List<byte[]> trustedCertificates;
    private String keyLength;
    private String algorithmName;
    private String protocolVersion;
}
