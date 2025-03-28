package org.kopingenieria.domain.dto;

import java.util.List;

public record SessionDTO(String sessionId,
                         String sessionName,
                         String serverUri,
                         Long maxResponseMessageSize,
                         String securityMode,
                         String securityPolicyUri,
                         String clientCertificate,
                         String serverCertificate,
                         List<String> localeIds,
                         Integer maxChunkCount,
                         Long timeout,
                         String clientIp) {
}
