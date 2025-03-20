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

    @NotBlank(message = "El nombre del evento es obligatorio y no puede estar vacío")
    private String eventName;

    @NotNull(message = "La marca de tiempo es obligatoria y no puede ser nula")
    private LocalDateTime timestamp;

    @NotNull(message = "La severidad es obligatoria y no puede ser nula")
    @Min(value = 0, message = "La severidad debe ser mayor o igual a 0")
    private Integer severity;

    @NotBlank(message = "El nodo fuente no puede estar vacío")
    private String sourceNode;

    @NotBlank(message = "El mensaje no puede estar vacío")
    private String message;

    @NotNull(message = "Los metadatos son obligatorios y no pueden ser nulos")
    private Map<String, Object> metadata;

}
