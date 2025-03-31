package org.kopingenieria.domain.model.bydefault;


import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
public class DefaultIndustrialConfiguration implements Serializable {

    @Serial
    private static final long serialVersionUID = 14L;

    private String industrialZone;
    private String equipmentId;
    private String areaId;
    private String processId;
    private String operatorName;
    private String operatorId;
}
