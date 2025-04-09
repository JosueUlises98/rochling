package org.kopingenieria.api.request.connection.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.UrlType;

@Data
@Builder
public class UserConnectionRequest {

    @NotBlank(message = "El id por defecto es obligatorio")
    private String clientId;
    @NotBlank(message = "El tipo de conexion es obligatorio")
    private ConnectionType connectionType;
    @NotBlank(message = "El endpoint URL es obligatorio")
    private UrlType endpointUrl;

}
