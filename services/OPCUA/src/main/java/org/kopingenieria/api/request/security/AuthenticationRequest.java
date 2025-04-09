package org.kopingenieria.api.request.security;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.IdentityProvider;

@Data
@Builder
public class AuthenticationRequest {

    @NotNull(message = "El identificador de conexión es obligatorio")
    private String connectionId;

    // Solo si es necesario actualizar la política de seguridad
    private IdentityProvider identityProvider;
    //Solo si es necesario actualizar el usuario
    private String username;
    //Solo si es necesario actualizar el password
    private String password;
    // Solo si es necesario actualizar certificados específicos
    private String certificatePath;
}
