package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

@Data
@Builder
public class OpcUaAuthenticationRequest {

    @NotNull(message = "El proveedor de identidad es obligatorio")
    private IdentityProvider identityProvider;
    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String userName;
    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
    @NotNull(message = "La política de seguridad es obligatoria")
    private SecurityPolicy securityPolicy;
    @NotNull(message = "El modo de seguridad del mensaje es obligatorio")
    private MessageSecurityMode messageSecurityMode;
    @NotBlank(message = "El certificado es obligatorio")
    private String certificatePath;
    @NotBlank(message = "La clave privada es obligatoria")
    private String privateKeyPath;
    @NotBlank(message = "La ruta de la lista de confianza es obligatoria")
    private String trustListPath;
    @NotBlank(message = "La ruta de la lista resolutora es obligatoria")
    private String issuerListPath;
    @NotBlank(message = "La ruta de la lista de revocacion es obligatoria")
    private String revocationListPath;
}
