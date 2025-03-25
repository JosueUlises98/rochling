package org.kopingenieria.domain.model;


import lombok.Builder;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class IndustrialConfiguration implements Serializable {

    @Serial
    private static final long serialVersionUID = 6L;

    private String industrialZone;
    private String equipmentId;
    private String areaId;
    private String processId;
    private String operatorName;
    private String operatorId;
}
