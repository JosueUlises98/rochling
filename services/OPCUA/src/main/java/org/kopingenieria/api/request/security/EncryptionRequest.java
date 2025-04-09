package org.kopingenieria.api.request.security;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.EncryptionAlgorithm;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import java.util.List;

@Data
@Builder
public class EncryptionRequest {

    @NotNull(message = "El identificador de conexión es obligatorio")
    private String connectionId;

    // Solo si necesitas actualizar la política de seguridad
    private SecurityPolicy securityPolicy;

    // Solo si necesitas actualizar el modo de seguridad
    private MessageSecurityMode messageSecurityMode;

    // Solo si necesitas actualizar el algoritmo de encriptación
    private EncryptionAlgorithm algorithmName;

    // Para validación de certificados actuales
    private Boolean validateCurrentCertificates;

    // Solo si necesitas actualizar certificados específicos
    private List<byte[]> certificatesToUpdate;
}