package org.kopingenieria.domain.classes;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_subscriptions")
@EntityListeners(AuditingEntityListener.class)
public final class Suscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de suscripción
    @Column(name = "publishing_interval")
    private Double publishingInterval;

    @Column(name = "lifetime_count")
    private Integer lifetimeCount;

    @Column(name = "max_keep_alive_count")
    private Integer maxKeepAliveCount;

    @Column(name = "max_notifications_per_publish")
    private Integer maxNotificationsPerPublish;

    @Column(name = "priority")
    private Integer priority;

    @Column(name = "publishing_enabled")
    private Boolean publishingEnabled;
}
