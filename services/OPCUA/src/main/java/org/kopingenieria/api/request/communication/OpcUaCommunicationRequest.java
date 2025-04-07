package org.kopingenieria.api.request.communication;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.Operation;

import java.util.Map;

@Data
@Builder
public class OpcUaCommunicationRequest {

    @NotBlank(message = "La operacion es obligatoria")
    private Operation operation;
    @NotBlank(message = "El endpoint del servidor no puede estar vacío")
    private String serverEndpoint;
    @NotBlank(message = "El NodeId de operaciones no puede estar vacio")
    private String NodeId;
    @NotBlank(message = "El valor es obligatorio")
    private Object value;
    @NotBlank(message = "Las opciones son obligatorias")
    private Map<String, Object> options;

}
