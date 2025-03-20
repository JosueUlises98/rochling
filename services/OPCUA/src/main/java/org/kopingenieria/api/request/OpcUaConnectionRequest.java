package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;

@Data
@Builder
public class OpcUaConnectionRequest {

    @NotBlank(message = "La URL del endpoint es obligatoria")
    private String endpointUrl;

    @NotBlank(message = "El nombre de la aplicación es obligatorio")
    private String applicationName;

    @NotBlank(message = "El URI de la aplicación es obligatorio")
    private String applicationUri;

    private String productUri;
    private String name;
    private String hostname;
    private Integer port;
    private String method;
    private ConnectionType type;
    private long timeout;
    private long channelLifetime;

}
