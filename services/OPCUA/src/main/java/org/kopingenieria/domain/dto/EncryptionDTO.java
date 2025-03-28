package org.kopingenieria.domain.dto;

import java.util.List;

public record EncryptionDTO(String securityPolicy,
                            String messageSecurityMode,
                            byte[] clientCertificate,
                            byte[] privateKey,
                            List<byte[]> trustedCertificates,
                            String keyLength,
                            String algorithmName,
                            String protocolVersion) {
}
