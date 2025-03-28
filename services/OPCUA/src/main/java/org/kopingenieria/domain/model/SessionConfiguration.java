package org.kopingenieria.domain.model;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class SessionConfiguration implements Serializable {
    @Serial
    private static final long serialVersionUID = 4L;

    private String sessionId;
    private String sessionName;
    private String serverUri;
    private Long maxResponseMessageSize;
    private String securityMode;
    private String securityPolicyUri;
    private String clientCertificate;
    private String serverCertificate;
    private List<String> localeIds;
    private Integer maxChunkCount;
    private Long timeout;
    private String clientIp;

}
