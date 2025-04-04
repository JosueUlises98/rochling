package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;

@Data
@Builder
public class OpcUaConnectionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El endpoint URL es obligatorio")
    private String endpointUrl;

    @NotBlank(message = "El nombre de la aplicación es obligatorio")
    private String applicationName;

    @NotBlank(message = "El URI de la aplicación es obligatorio")
    private String applicationUri;

    @NotBlank(message = "El URI del producto es obligatorio")
    private String productUri;

    @NotNull(message = "El tipo de conexión es obligatorio")
    private ConnectionType type;

    private final Timeouts timeout = Timeouts.CONNECTION;

    @NotNull(message = "El estado de la conexión es obligatorio")
    private ConnectionStatus status;
}
