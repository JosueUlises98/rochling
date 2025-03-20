package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OpcUaSessionRequest {

    @NotBlank(message = "El nombre de la sesión es obligatorio")
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

}
