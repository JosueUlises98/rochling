package org.kopingenieria.application.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "monitoring_event")
@EntityListeners(AuditingEntityListener.class)
public class MonitoringEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 800L;

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

}
