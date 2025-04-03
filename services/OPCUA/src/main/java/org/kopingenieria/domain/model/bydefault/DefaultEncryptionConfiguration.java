package org.kopingenieria.domain.model.bydefault;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.kopingenieria.domain.enums.security.EncryptionAlgorithm;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Getter
@Builder
public class DefaultEncryptionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    private final SecurityPolicy securityPolicy;
    private final MessageSecurityMode messageSecurityMode;
    private final byte[] clientCertificate;
    private final byte[] privateKey;
    private final List<byte[]> trustedCertificates;
    private final Integer keyLength;
    private final EncryptionAlgorithm algorithmName;
    private final String protocolVersion;
    private final String type;
}
