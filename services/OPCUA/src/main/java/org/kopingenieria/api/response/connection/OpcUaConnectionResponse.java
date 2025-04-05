package org.kopingenieria.api.response.connection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.QualityLevel;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpcUaConnectionResponse {
    private Long id;
    private String endpointUrl;
    private String applicationName;
    private String applicationUri;
    private String productUri;
    private ConnectionStatus status;
    private QualityLevel quality;
    private LocalDateTime lastActivity;
    private UaClient client;
}
