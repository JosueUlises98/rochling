package org.kopingenieria.api.request.connection.user;


import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.model.user.UserOpcUa;


@Data
@Builder
public class UserSessionRequest {

    @NotBlank(message = "El nombre de la sesion es obligatorio")
    private String sessionName;
    @NotBlank(message = "El endpoint URL es obligatorio")
    private String serverUri;
    @NotBlank(message = "La configuracion del usuario es obligatoria")
    private UserOpcUa client;

}
