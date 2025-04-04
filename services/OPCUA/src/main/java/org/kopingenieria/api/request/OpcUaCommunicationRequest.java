package org.kopingenieria.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class OpcUaCommunicationRequest {

    @NotBlank(message = "El nombre del cliente no puede estar vacío")
    private String clientName;
    @NotBlank(message = "La URI del cliente no puede estar vacía")
    private String clientUri;
    @NotBlank(message = "El endpoint del servidor no puede estar vacío")
    private String serverEndpoint;
    @NotBlank(message = "El NodeId de operaciones no puede estar vacio")
    private String NodeId;
    @NotBlank
    private Object value;
    @NotBlank
    private Map<String, Object> options;

}
