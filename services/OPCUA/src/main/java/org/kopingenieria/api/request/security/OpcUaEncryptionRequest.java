package org.kopingenieria.api.request.security;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.EncryptionAlgorithm;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import java.util.List;

@Data
@Builder
public class OpcUaEncryptionRequest {

    @NotNull(message = "La política de seguridad es obligatoria")
    private SecurityPolicy securityPolicy;

    @NotNull(message = "El modo de seguridad del mensaje es obligatorio")
    private MessageSecurityMode messageSecurityMode;

    @NotNull(message = "El certificado del cliente no puede ser nulo")
    @Size(min = 1, message = "El certificado del cliente no puede estar vacío")
    private byte[] clientCertificate;

    @NotNull(message = "La clave privada no puede ser nula")
    @Size(min = 1, message = "La clave privada no puede estar vacía")
    private byte[] privateKey;

    @NotNull(message = "La lista de certificados de confianza no puede ser nula")
    @Size(min = 1, message = "Debe haber al menos un certificado de confianza")
    private List<@NotNull(message = "Los certificados de confianza no pueden ser nulos")
    @Size(min = 1, message = "Los certificados de confianza no pueden estar vacíos")
            byte[]> trustedCertificates;

    @NotNull(message = "El algoritmo de encriptación es obligatorio")
    private EncryptionAlgorithm algorithmName;

}