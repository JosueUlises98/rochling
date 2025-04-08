package org.kopingenieria.api.request.connection.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;
import org.kopingenieria.domain.model.user.UserOpcUa;

@Data
@Builder
public class UserConnectionRequest {

    @NotBlank(message = "La configuracion por defecto es obligatoria")
    private UserOpcUa client;
    @NotBlank(message = "El nombre es obligatorio")
    private ConnectionType connectionType;
    @NotBlank(message = "El endpoint URL es obligatorio")
    private UrlType endpointUrl;

}
