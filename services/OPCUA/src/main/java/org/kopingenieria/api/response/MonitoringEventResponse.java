package org.kopingenieria.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MonitoringEventResponse {
    private Long id;
    private String eventName;
    private LocalDateTime timestamp;
    private Integer severity;
    private String sourceNode;
    private String message;
    private Map<String, Object> metadata;
    private Long version;
}
