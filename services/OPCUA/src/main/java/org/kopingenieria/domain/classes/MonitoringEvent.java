package org.kopingenieria.domain.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.kopingenieria.domain.classes.auditable.NosqlAuditable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monitoring_event")
@EntityListeners(AuditingEntityListener.class)
public class MonitoringEvent {

    //Atributos de Monitoring Event
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_name")
    private String eventName;

    @Column(name = "event_timestamp")
    private LocalDateTime timestamp;

    @Column(name = "event_severity")
    private Integer severity;

    @Column(name = "event_source_node")
    private String sourceNode;

    @Column(name = "event_message")
    private String message;

    @Column(name = "event_metadata")
    private Map<String, Object> metadata;

    @Version
    private Long version;

    @Column(name = "audit_info")
    private NosqlAuditable auditable;
}
