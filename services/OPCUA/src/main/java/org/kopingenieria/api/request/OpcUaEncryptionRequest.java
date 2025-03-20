package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class OpcUaEncryptionRequest {

    @NotBlank(message = "La política de seguridad es obligatoria")
    private String securityPolicy;

    @NotBlank(message = "El modo de seguridad del mensaje es obligatorio")
    private String messageSecurityMode;

    @NotNull(message = "El certificado del cliente es obligatorio")
    private byte[] clientCertificate;

    @NotNull(message = "La clave privada es obligatoria")
    private byte[] privateKey;

    @NotNull(message = "La lista de certificados de confianza es obligatoria")
    @Size(min = 1, message = "Debe haber al menos un certificado confiable")
    private List<@NotNull(message = "El certificado no puede ser nulo") byte[]> trustedCertificates;

    @NotBlank(message = "La longitud de la clave es obligatoria")
    private String keyLength;

    @NotBlank(message = "El nombre del algoritmo es obligatorio")
    private String algorithmName;

    @NotBlank(message = "La versión del protocolo es obligatoria")
    private String protocolVersion;

}
