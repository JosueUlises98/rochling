package org.kopingenieria.model.classes;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tcp_configurations")
@EntityListeners(AuditingEntityListener.class)
public final class TCPConfiguration extends Configuration{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de conexión TCP
    @Column(name = "connection",nullable = false)
    private TCPConnection connection;

    // Atributos de sesión TCP
    @Column(name = "session",nullable = false)
    private TCPSession session;

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
        return "TCPConfiguration{" +
                ",name=' " + name + '\'' +
                ",description=' " + description + '\'' +
                ",enabled=' " + enabled + '\'' +
                ", connection=" + connection +
                ", session=" + session +
                ", subscription=" + subscription +
                ", monitoringEvent=" + monitoringEvent +
                ", qualityConnection=" + qualityConnection +
                '}';
    }
}
