package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.domain.enums.security.SecurityPolicyUri;

@Data
@Builder
public class OpcUaAuthenticationRequest {

    @NotNull(message = "El proveedor de identidad es obligatorio")
    private IdentityProvider identityProvider;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    private String userName;

    @NotBlank(message = "La contraseña no puede estar vacía")
    private String password;

    @NotNull(message = "La política de seguridad es obligatoria")
    private SecurityPolicy securityPolicy;

    @NotNull(message = "El modo de seguridad del mensaje es obligatorio")
    private MessageSecurityMode messageSecurityMode;

    @NotBlank(message = "La ruta del certificado no puede estar vacía")
    private String certificatePath;

    @NotBlank(message = "La ruta de la clave privada no puede estar vacía")
    private String privateKeyPath;

    @NotBlank(message = "La ruta de la lista de confianza no puede estar vacía")
    private String trustListPath;

    @NotBlank(message = "La ruta de la lista de emisores no puede estar vacía")
    private String issuerListPath;

    @NotBlank(message = "La ruta de la lista de revocación no puede estar vacía")
    private String revocationListPath;

    @NotNull(message = "El URI de la política de seguridad es obligatorio")
    private SecurityPolicyUri securityPolicyUri;

    private final int expirationWarningDays = 30;
}
