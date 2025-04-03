package org.kopingenieria.domain.model.bydefault;


import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Builder
public class DefaultIndustrialConfiguration implements Serializable {

    @Serial
    private static final long serialVersionUID = 6L;

    private final String industrialZone;
    private final String equipmentId;
    private final String areaId;
    private final String processId;
    private final String operatorName;
    private final String operatorId;
}
