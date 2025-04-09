package org.kopingenieria.api.request.connection.bydefault;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;

@Data
@Builder
public class DefaultSessionRequest {

    @NotBlank(message = "El nombre de la sesion es obligatorio")
    private String sessionName;
    @NotBlank(message = "El endpoint URL es obligatorio")
    private String serverUri;
}
