package org.kopingenieria.api.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class MonitoringEventRequest {

    @NotBlank(message = "El nombre del evento es obligatorio")
    private String eventName;

    @NotNull(message = "La marca de tiempo es obligatoria")
    private LocalDateTime timestamp;

    @Min(value = 0, message = "La severidad debe ser mayor o igual a 0")
    private Integer severity;

    private String sourceNode;
    private String message;
    private Map<String, Object> metadata;

}
