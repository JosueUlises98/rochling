package org.kopingenieria.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class LogRequest {
    @NotEmpty(message = "El nivel de log es requerido")
    private String level;

    @NotEmpty(message = "El mensaje es requerido")
    private String message;

    private String source;

    private Map<String, Object> additionalInfo;
}
