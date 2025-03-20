package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.MessageSecurityMode;
import org.kopingenieria.domain.enums.connection.SecurityPolicy;

@Data
@Builder
public class OpcUaAuthenticationRequest {

    @NotNull(message = "La política de seguridad es obligatoria")
    private SecurityPolicy securityPolicy;

    @NotNull(message = "El modo de seguridad del mensaje es obligatorio")
    private MessageSecurityMode messageSecurityMode;

    private String certificatePath;
    private String privateKeyPath;
    private String trustListPath;
    private String issuerListPath;
    private String revocationListPath;
    private String username;
    private String password;

}
