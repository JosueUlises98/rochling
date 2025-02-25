package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.kopingenieria.exceptions.InvalidConnectionStateTransitionException;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.kopingenieria.model.enums.tls.TlsVersion;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_connections")
@EntityListeners(AuditingEntityListener.class)
public final class TLSConnection extends Connection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de conexion TLS
    @Enumerated(EnumType.STRING)
    private TlsVersion tlsVersion;

    @Column(name = "keystore_path")
    private String keystorePath;

    @Column(name = "keystore_password")
    private String keystorePassword;

    @Column(name = "keystore_type")
    private String keystoreType;

    @Column(name = "truststore_path")
    private String truststorePath;

    @Column(name = "truststore_password")
    private String truststorePassword;

    @Column(name = "truststore_type")
    private String truststoreType;

    @Override
    public String toString() {
        return "TLSConnection{ name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", qualityConnection=" + qualityConnection +
                ", tlsVersion=" + tlsVersion +
                ", keystorePath='" + keystorePath + '\'' +
                ", keystorePassword='" + keystorePassword + '\'' +
                ", keystoreType='" + keystoreType + '\'' +
                ", truststorePath='" + truststorePath + '\'' +
                ", truststorePassword='" + truststorePassword + '\'' +
                ", truststoreType='" + truststoreType + '\'' +
                "} ";
    }

    public void updateConnectionStatus(ConnectionStatus newStatus) throws InvalidConnectionStateTransitionException {
        super.updateStatus(newStatus);
    }
}
