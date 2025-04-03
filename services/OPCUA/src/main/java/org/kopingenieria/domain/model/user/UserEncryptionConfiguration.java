package org.kopingenieria.domain.model.user;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.EncryptionAlgorithm;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class UserEncryptionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    private SecurityPolicy securityPolicy;
    private MessageSecurityMode messageSecurityMode;
    private byte[] clientCertificate;
    private byte[] privateKey;
    private List<byte[]> trustedCertificates;
    private Integer keyLength;
    private EncryptionAlgorithm algorithmName;
    private String protocolVersion;
    private String type;
}
