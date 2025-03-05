package org.kopingenieria.domain.classes;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndustrialConfiguration {

    // Configuración industrial común
    @Column(name = "industrial_zone")
    private String industrialZone;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "area_id")
    private String areaId;

    @Column(name = "process_id")
    private String processId;
}
