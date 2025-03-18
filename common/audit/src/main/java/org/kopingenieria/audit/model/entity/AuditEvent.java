package org.kopingenieria.audit.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.audit.model.AuditEntryType;

import java.time.LocalDateTime;

@Data
@Builder
@Entity
@Table(name = "audit_events")
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AuditEntryType eventType;

    @Column(nullable = false, length = 50)
    private String component;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "user_id", nullable = false, length = 50)
    private String userId;

    @Column(name = "current_user", length = 50)
    private String currentUser;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 500)
    private String details;

    @Column(length = 20)
    @Pattern(regexp = "SUCCESS|FAILURE")
    private String outcome;

    @Column(name = "trace_id", length = 36)
    private String traceId;

    @Column(name = "ip_address", length = 45)
    @Pattern(regexp = "^(\\d{1,3}\\.){3}\\d{1,3}$")
    private String ipAddress;

    @Column(name = "execution_time")
    private Long executionTime;

    @Column(name = "class_name", length = 100)
    private String className;

    @Column(name = "method_name", length = 100)
    private String methodName;

    @Version
    private Long version;
}
