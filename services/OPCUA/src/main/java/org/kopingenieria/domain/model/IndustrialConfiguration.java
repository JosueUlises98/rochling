package org.kopingenieria.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndustrialConfiguration {

    @NotNull(message = "Industrial zone cannot be null")
    private String industrialZone;
    @NotNull(message = "Equipment ID cannot be null")
    private String equipmentId;
    @NotNull(message = "Area ID cannot be null")
    private String areaId;
    @NotNull(message = "Process ID cannot be null")
    private String processId;
}
