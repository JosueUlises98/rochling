package org.kopingenieria.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class OpcUaSession implements Serializable {
    private String sessionId;
    private String sessionName;
    private String serverUri;
    private LocalDateTime creationTime;
    private LocalDateTime lastAccessTime;
    private Integer timeout;
    private Long maxResponseMessageSize;
    private String securityMode;
    private String securityPolicyUri;
    private String clientCertificate;
    private String serverCertificate;
    private String authenticationToken;
    private String error;
    private SessionStatus status;
    private Connection<?> connectionDetails;
}
