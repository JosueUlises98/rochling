package org.kopingenieria.model;

import java.time.LocalDateTime;

public record AuditEventDTO(String id, String eventtype, String component, String action, String userid,
                            LocalDateTime timestamp, String details, String outcome, String traceid, String ipaddress,Long executiontime,Long version) {
}
