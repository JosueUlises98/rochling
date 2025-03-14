package org.kopingenieria.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class BatchLogRequest {
    @NotEmpty(message = "La lista de logs no puede estar vacía")
    private List<LogRequest> entries;
}
