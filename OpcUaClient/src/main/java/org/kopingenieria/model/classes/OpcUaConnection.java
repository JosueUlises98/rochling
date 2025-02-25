package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.exceptions.InvalidConnectionStateTransitionException;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_connections")
@EntityListeners(AuditingEntityListener.class)
public final class OpcUaConnection extends Connection{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos básicos de conexión
    @Column(nullable = false)
    private String endpointUrl;

    @Column(nullable = false)
    private String applicationName;

    @Column(nullable = false)
    private String applicationUri;

    @Column(name = "product_uri")
    private String productUri;

    @Override
    public String toString() {
        return "OpcUaConnection{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", qualityConnection=" + qualityConnection +
                ", endpointUrl='" + endpointUrl + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationUri='" + applicationUri + '\'' +
                ", productUri='" + productUri + '\'' +
                '}';
    }
    
    public void updateConnectionStatus(ConnectionStatus newStatus) throws InvalidConnectionStateTransitionException {
        super.updateStatus(newStatus);
    }
}
