package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.logging.Logger;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_configurations")
@EntityListeners(AuditingEntityListener.class)
public final class OpcUaConfiguration extends Configuration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de conexión OPC UA
    @Column(name = "connection", nullable = false)
    private OpcUaConnection connection;

    // Atributos de seguridad OPC UA
    @Column(name = "authentication", nullable = false)
    private OpcUaAuthentication authentication;
    @Column(name = "encryption",nullable = false)
    private OpcUaEncryption encryption;

    // Atributos de sesión OPC UA
    @Column(name = "session", nullable = false)
    private OpcUaSession session;

    // Atributos de suscripción OPC UA
    @Column(name = "subscription")
    private Suscription subscription;

    //Atributos de monitoreo OPC UA
    @Column(name = "monitoring_event")
    private MonitoringEvent monitoringEvent;

    // Atributos de calidad de red
    @Column(name = "quality_connection", nullable = false)
    private QualityNetwork qualityConnection;

    @Override
    public String toString() {
        return "OpcUaConfiguration{" +
                ",name=' " + name + '\'' +
                ",description=' " + description + '\'' +
                ",enabled=' " + enabled + '\'' +
                ", connection=" + connection +
                ", authentication=" + authentication +
                ", encryption=" + encryption +
                ", session=" + session +
                ", subscription=" + subscription +
                ", monitoringEvent=" + monitoringEvent +
                ", qualityConnection=" + qualityConnection +
                '}';
    }
}
