package org.kopingenieria.domain.classes;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitoringEvent {
    private Long id;
    private String eventName;
    private java.util.Date timestamp;
    private Integer severity;
    private String sourceNode;
    private String message;
}
