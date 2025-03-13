package org.kopingenieria.model.dto;

import lombok.Builder;
import java.time.LocalDateTime;

@Builder
public record AuditEventDTO(String id, String eventtype, String component, String action, String userid,
                            LocalDateTime timestamp, String details, String outcome, String traceid, String ipaddress,Long executiontime,Long version) {
}
