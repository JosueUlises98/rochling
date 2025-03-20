package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
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

    private byte[] clientCertificate;
    private byte[] privateKey;
    private List<byte[]> trustedCertificates;
    private String keyLength;
    private String algorithmName;
    private String protocolVersion;

}
