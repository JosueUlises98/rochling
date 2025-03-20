package org.kopingenieria.application.db.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.application.monitoring.quality.QualityNetwork;
import org.kopingenieria.domain.model.IndustrialConfiguration;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;
import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_configurations",
        indexes = {
                @Index(name = "idx_connection", columnList = "connection_id"),
                @Index(name = "idx_session", columnList = "session_id"),
                @Index(name = "idx_authentication", columnList = "authentication_id"),
                @Index(name = "idx_config_name", columnList = "name", unique = true)
        })
@EntityListeners(AuditingEntityListener.class)
public final class OpcUaConfiguration extends Configuration {

    @Serial
    private static final long serialVersionUID = 101;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre de la configuración es obligatorio")
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;

    // Relación con conexión OPC UA
    @NotNull(message = "La conexión es obligatoria")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "connection_id", nullable = false)
    private OpcUaConnection connection;

    // Relación con autenticación
    @NotNull(message = "La autenticación es obligatoria")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "authentication_id", nullable = false)
    private OpcUaAuthentication authentication;

    // Relación con encriptación
    @NotNull(message = "La configuración de encriptación es obligatoria")
    @Embedded
    private OpcUaEncryption encryption;

    // Relación con sesión
    @NotNull(message = "La sesión es obligatoria")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "session_id", nullable = false)
    private OpcUaSession session;

    // Relación con suscripción
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "configuration_id")
    private List<Subscription> subscriptions;

    // Relación con eventos de monitoreo
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "configuration_id")
    private List<MonitoringEvent> monitoringEvents;

    // Calidad de red como componente embebido
    @NotNull(message = "La configuración de calidad de red es obligatoria")
    @Embedded
    private QualityNetwork qualityConnection;

    //Configuracion industrial
    @NotNull(message = "La configuracion industrial es obligatoria")
    @Embedded
    private IndustrialConfiguration industrialConfiguration;

    @Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    private void onCreate() {
        if (enabled == null) {
            enabled = true;
        }
    }

    @PreUpdate
    private void onUpdate() {
        // Lógica de validación antes de actualizar
        if (enabled == null) {
            enabled = false;
        }
    }

    @Override
    public String toString() {
        return "OpcUaConfiguration{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", enabled=" + enabled +
                ", connection=" + connection +
                ", authentication=" + authentication +
                ", encryption=" + encryption +
                ", session=" + session +
                ", subscriptions=" + subscriptions +
                ", monitoringEvents=" + monitoringEvents +
                ", qualityConnection=" + qualityConnection +
                ", version=" + version +
                '}';
    }
}
