package org.kopingenieria.api.request.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.IdentityProvider;

@Data
@Builder
public class OpcUaAuthenticationRequest {

    @NotBlank(message = "El proveedor de identidad es obligatorio")
    private IdentityProvider identityProvider;

    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "El nombre de usuario solo puede contener letras, números, guiones y guiones bajos")
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @NotBlank(message = "El password es obligatorio")
    private String password;

    @Pattern(regexp = "^(.+)\\.([p,P][e,E][m,M]|[c,C][e,E][r,R][t,T]|[d,D][e,E][r,R])$",
            message = "La ruta del certificado debe terminar en .pem, .cert o .der")
    @NotBlank(message = "La ruta del certificado es obligatoria")
    private String certificatePath;

    @Pattern(regexp = "^(.+)\\.([p,P][e,E][m,M]|[k,K][e,E][y,Y])$",
            message = "La ruta de la clave privada debe terminar en .pem o .key")
    @NotBlank(message = "La ruta de la clave privada es obligatoria")
    private String privateKeyPath;

    @NotNull(message = "El password del certificado es obligatorio")
    private String certificatePassword;

    private String token;
}
