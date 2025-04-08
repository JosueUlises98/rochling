package org.kopingenieria.api.request.communication;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.Operation;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CommunicationRequest {

    @NotBlank(message = "El ID de sesión es obligatorio")
    private String sessionId;

    @NotBlank(message = "El identificador del cliente es obligatorio")
    private String clientId;

    // Para especificar nodos o tags específicos
    @NotBlank(message = "La lista de nodos es obligatoria")
    private List<String> nodeIds;

    // Para especificar el tipo de operación
    @NotBlank(message = "El tipo de operacion es obligatorio")
    private Operation operationType; // READ, WRITE, SUBSCRIBE, etc.

    // Parámetros adicionales según la operación
    private Map<String, Object> operationParameters;

    //Para especificar el valor a utilizar en la comunicacion
    @NotBlank(message = "El valor es obligatorio")
    private Object value;
}
