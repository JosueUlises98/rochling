package org.kopingenieria.api.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.connection.ConnectionType;

@Data
@Builder
public class OpcUaConnectionRequest {

    @NotBlank(message = "El endpoint URL es obligatorio")
    private String endpointUrl;

    @NotBlank(message = "El nombre de la aplicación es obligatorio")
    private String applicationName;

    @NotBlank(message = "El URI de la aplicación es obligatorio")
    private String applicationUri;

    @NotBlank(message = "El URI del producto es obligatorio")
    private String productUri;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El nombre del host es obligatorio")
    private String hostname;

    @NotNull(message = "El puerto es obligatorio")
    @Min(value = 1, message = "El puerto debe ser mayor a 0")
    @Max(value = 65535, message = "El puerto debe ser menor a 65535")
    private Integer port;

    @NotBlank(message = "El método es obligatorio")
    private String method;

    @NotNull(message = "El tipo de conexión es obligatorio")
    private ConnectionType type;

    @Min(value = 1000, message = "El timeout debe ser mayor a 1000 ms")
    @Max(value = 30000, message = "El timeout debe ser menor a 30000 ms")
    private long timeout;

    @Min(value = 1000, message = "El tiempo de vida del canal debe ser mayor 1000 ms")
    @Max(value = 300000, message = "El tiempo de vida del canal debe ser menor a 300000 ms")
    private long channelLifetime;

}
