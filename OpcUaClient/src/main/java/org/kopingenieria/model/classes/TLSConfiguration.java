package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_configurations")
@EntityListeners(AuditingEntityListener.class)
public final class TLSConfiguration extends Configuration {

    @Serial
    private static final long serialVersionUID = 124L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de conexión TLS
    @Column(name = "connection",nullable = false)
    private TLSConnection connection;

    //Atributos de seguridad TLS
    @Column(name = "authentication",nullable = false)
    private TLSAuthentication authentication;
    @Column(name = "encryption",nullable = false)
    private TLSEncryption encryption;

    // Atributos de sesión TLS
    @Column(name = "session",nullable = false)
    private TLSSession session;

    // Atributos de suscripción OPCUA
    @Column(name = "subscription")
    private Suscription subscription;

    //Atributos de monitoreo de eventos OPCUA
    @Column(name = "monitoring_event")
    private MonitoringEvent monitoringEvent;

    // Atributos de calidad de red
    @Column(name = "quality_connection",nullable = false)
    private QualityNetwork qualityConnection;

    @Override
    public String toString() {
        return "TLSConfiguration{" +
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
