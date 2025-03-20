package org.kopingenieria.application.db.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_subscriptions")
@EntityListeners(AuditingEntityListener.class)
public final class Subscription implements Serializable {

    @Serial
    private static final long serialVersionUID = 700L;

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
