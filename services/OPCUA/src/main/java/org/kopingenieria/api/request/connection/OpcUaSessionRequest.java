package org.kopingenieria.api.request.connection;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.Timeouts;

import java.util.List;

@Data
@Builder
public class OpcUaSessionRequest {

    @NotBlank(message = "El nombre de la sesión es obligatorio")
    private String sessionName;

    @NotBlank(message = "El URI del servidor es obligatorio")
    private String serverUri;

    @Min(value = 1024, message = "El tamaño máximo de respuesta debe ser al menos 1024 bytes")
    private Long maxResponseMessageSize;

    @NotBlank(message = "El modo de seguridad es obligatorio")
    private String securityMode;

    @NotBlank(message = "El URI de la política de seguridad es obligatorio")
    private String securityPolicyUri;

    @NotBlank(message = "El certificado del cliente es obligatorio")
    private String clientCertificate;

    @NotBlank(message = "El certificado del servidor es obligatorio")
    private String serverCertificate;

    @NotNull(message = "La lista de IDs de localización no puede ser nula")
    @Size(min = 1, message = "Debe haber al menos un ID de localización")
    private List<@NotBlank(message = "Los IDs de localización no pueden estar vacíos") String> localeIds;

    @Min(value = 1, message = "El conteo máximo de fragmentos debe ser mayor o igual a 1")
    private Integer maxChunkCount;

    @Min(value = 1000, message = "El tiempo de espera debe ser al menos 1000 ms")
    private final Timeouts timeout = Timeouts.SESSION;

}
