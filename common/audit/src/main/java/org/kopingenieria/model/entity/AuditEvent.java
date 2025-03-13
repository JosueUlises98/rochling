package org.kopingenieria.model.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false)
    private String component;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String currentuser;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 4000)
    private String details;

    @Column(length = 255)
    private String outcome;

    @Column(name = "trace_id")
    private String traceId;

    @Column(name = "ip_address")
    private String ipAddress;

    @Column(name = "execution_time")
    private Long executionTime;

    @Version
    private Long version;
}
