package org.kopingenieria.domain.classes;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_configurations")
@EntityListeners(AuditingEntityListener.class)
public final class SSHConfiguration extends Configuration {

    @Serial
    private static final long serialVersionUID = 125L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de conexión SSH
    @Column(name = "connection",nullable = false)
    private SSHConnection connection;

    //Atributos de seguridad SSH
    @Column(name = "authentication",nullable = false)
    private SSHAuthentication authentication;
    @Column(name = "encryption",nullable = false)
    private SSHEncryption encryption;

    // Atributos de sesión SSH
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
        return "SSHConfiguration{" +
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
