package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

@Data
@Builder
public class OpcUaAuthenticationRequest {

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
    @NotBlank(message = "El username es obligatorio")
    private String username;
    @NotBlank(message = "El password es obligatorio")
    private String password;
}
