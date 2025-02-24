package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaSession;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.kopingenieria.model.enums.ssh.RedundancyMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_connections")
@EntityListeners(AuditingEntityListener.class)
public class OpcUaConnection extends Connection<OpcUaConnection> {

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

    //Atributos de autenticacion
    @Column(name = "authentication_policy")
    private OpcUaAuthentication authentication;


    @Override
    protected OpcUaConnection self() {
        return this;
    }

    @Override
    public OpcUaConnection build() {
        OpcUaConnection opcUaConnection = new OpcUaConnection();
        opcUaConnection.name = this.name;
        opcUaConnection.hostname = this.hostname;
        opcUaConnection.port = this.port;
        opcUaConnection.method = this.method;
        opcUaConnection.type = this.type;
        opcUaConnection.status = this.status;
        opcUaConnection.qualityConnection = this.qualityConnection;
        opcUaConnection.endpointUrl = this.endpointUrl;
        opcUaConnection.applicationName = this.applicationName;
        opcUaConnection.applicationUri = this.applicationUri;
        opcUaConnection.productUri = this.productUri;
        opcUaConnection.authentication = this.authentication;
        return opcUaConnection;
    }

    @Override
    public String toString() {
        return "OpcUaConnection{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
                ", endpointUrl='" + endpointUrl + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationUri='" + applicationUri + '\'' +
                ", productUri='" + productUri + '\'' +
                '}';
    }

    public void updateConnectionStatus(ConnectionStatus newStatus){
        super.updateStatus(newStatus);
    }

}
