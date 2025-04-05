package org.kopingenieria.api.request.connection.bydefault;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.UrlType;

@Data
@Builder
public class DefaultConnectionRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private ConnectionType connectionType;
    @NotBlank(message = "El endpoint URL es obligatorio")
    private UrlType endpointUrl;

}
