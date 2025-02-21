package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaSession;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.kopingenieria.model.enums.tcp.ConnectionStatus;
import org.kopingenieria.model.enums.opcua.MessageSecurityMode;
import org.kopingenieria.model.enums.ssh.RedundancyMode;
import org.kopingenieria.model.enums.opcua.SecurityPolicy;
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

    // Configuración de seguridad
    @Enumerated(EnumType.STRING)
    @Column(name = "security_policy")
    private SecurityPolicy securityPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_security_mode")
    private MessageSecurityMode messageSecurityMode;

    @Column(name = "certificate_path")
    private String certificatePath;

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "trust_list_path")
    private String trustListPath;

    @Column(name = "issuer_list_path")
    private String issuerListPath;

    @Column(name = "revocation_list_path")
    private String revocationListPath;

    // Configuración de sesión
    @Column(name = "session_timeout")
    private Double sessionTimeout;

    @Column(name = "session_name")
    private String sessionName;

    @ElementCollection
    @CollectionTable(
            name = "opcua_locale_ids",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private List<String> localeIds;

    @Column(name = "max_response_message_size")
    private Integer maxResponseMessageSize;

    @Column(name = "max_chunk_count")
    private Integer maxChunkCount;

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

    // Configuración de monitoreo
    @Column(name = "sampling_interval")
    private Double samplingInterval;

    @Column(name = "queue_size")
    private Integer queueSize;

    @Column(name = "discard_oldest")
    private Boolean discardOldest;

    @Column(name = "monitoring_mode")
    @Enumerated(EnumType.STRING)
    private MonitoringMode monitoringMode;

    // Configuración de redundancia
    @Column(name = "redundancy_mode")
    @Enumerated(EnumType.STRING)
    private RedundancyMode redundancyMode;

    @ElementCollection
    @CollectionTable(
            name = "opcua_redundant_servers",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private List<String> redundantServerUrls;

    // Configuración industrial
    @Column(name = "industrial_zone")
    private String industrialZone;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "area_id")
    private String areaId;

    @Column(name = "process_id")
    private String processId;

    // Estado y diagnóstico
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    @Column(name = "last_connected")
    private LocalDateTime lastConnected;

    @Column(name = "last_disconnected")
    private LocalDateTime lastDisconnected;

    @Column(name = "connection_count")
    private Integer connectionCount;

    @Column(name = "error_count")
    private Integer errorCount;

    // Métricas
    @Embedded
    private OpcUaMetrics metrics;

    // Campos de auditoría
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by")
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    // Referencias transientes
    @Transient
    private OpcUaClient client;

    @Transient
    private UaSession session;

    // Clase embebida para métricas
    @Embeddable
    @Getter
    @Setter
    public static class OpcUaMetrics {
        private Long goodCount;
        private Long badCount;
        private Long dataChangeCount;
        private Long eventCount;
        private Double averagePublishTime;
        private Double averageDataChangeTime;
        private Long totalBytesReceived;
        private Long totalBytesSent;
    }

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
        opcUaConnection.username = this.username;
        opcUaConnection.password = this.password;
        opcUaConnection.method = this.method;
        opcUaConnection.type = this.type;
        opcUaConnection.endpointUrl = this.endpointUrl;
        opcUaConnection.applicationName = this.applicationName;
        opcUaConnection.applicationUri = this.applicationUri;
        opcUaConnection.productUri = this.productUri;
        opcUaConnection.securityPolicy = this.securityPolicy;
        opcUaConnection.messageSecurityMode = this.messageSecurityMode;
        opcUaConnection.certificatePath = this.certificatePath;
        opcUaConnection.privateKeyPath = this.privateKeyPath;
        opcUaConnection.trustListPath = this.trustListPath;
        opcUaConnection.issuerListPath = this.issuerListPath;
        opcUaConnection.revocationListPath = this.revocationListPath;
        opcUaConnection.sessionTimeout = this.sessionTimeout;
        opcUaConnection.sessionName = this.sessionName;
        opcUaConnection.localeIds = this.localeIds;
        opcUaConnection.maxResponseMessageSize = this.maxResponseMessageSize;
        opcUaConnection.maxChunkCount = this.maxChunkCount;
        opcUaConnection.publishingInterval = this.publishingInterval;
        opcUaConnection.lifetimeCount = this.lifetimeCount;
        opcUaConnection.maxKeepAliveCount = this.maxKeepAliveCount;
        opcUaConnection.maxNotificationsPerPublish = this.maxNotificationsPerPublish;
        opcUaConnection.priority = this.priority;
        opcUaConnection.publishingEnabled = this.publishingEnabled;
        opcUaConnection.samplingInterval = this.samplingInterval;
        opcUaConnection.queueSize = this.queueSize;
        opcUaConnection.discardOldest = this.discardOldest;
        opcUaConnection.monitoringMode = this.monitoringMode;
        opcUaConnection.redundancyMode = this.redundancyMode;
        opcUaConnection.redundantServerUrls = this.redundantServerUrls;
        opcUaConnection.industrialZone = this.industrialZone;
        opcUaConnection.equipmentId = this.equipmentId;
        opcUaConnection.areaId = this.areaId;
        opcUaConnection.processId = this.processId;
        opcUaConnection.status = this.status;
        opcUaConnection.lastConnected = this.lastConnected;
        opcUaConnection.lastDisconnected = this.lastDisconnected;
        opcUaConnection.connectionCount = this.connectionCount;
        opcUaConnection.errorCount = this.errorCount;
        opcUaConnection.metrics = this.metrics;
        opcUaConnection.createdAt = this.createdAt;
        opcUaConnection.updatedAt = this.updatedAt;
        opcUaConnection.createdBy = this.createdBy;
        opcUaConnection.updatedBy = this.updatedBy;
        return opcUaConnection;
    }

    @Override
    public String toString() {
        return "OpcUaConnection{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
                ", endpointUrl='" + endpointUrl + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", applicationUri='" + applicationUri + '\'' +
                ", productUri='" + productUri + '\'' +
                ", securityPolicy=" + securityPolicy +
                ", messageSecurityMode=" + messageSecurityMode +
                ", certificatePath='" + certificatePath + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", trustListPath='" + trustListPath + '\'' +
                ", issuerListPath='" + issuerListPath + '\'' +
                ", revocationListPath='" + revocationListPath + '\'' +
                ", sessionTimeout=" + sessionTimeout +
                ", sessionName='" + sessionName + '\'' +
                ", localeIds=" + localeIds +
                ", maxResponseMessageSize=" + maxResponseMessageSize +
                ", maxChunkCount=" + maxChunkCount +
                ", publishingInterval=" + publishingInterval +
                ", lifetimeCount=" + lifetimeCount +
                ", maxKeepAliveCount=" + maxKeepAliveCount +
                ", maxNotificationsPerPublish=" + maxNotificationsPerPublish +
                ", priority=" + priority +
                ", publishingEnabled=" + publishingEnabled +
                ", samplingInterval=" + samplingInterval +
                ", queueSize=" + queueSize +
                ", discardOldest=" + discardOldest +
                ", monitoringMode=" + monitoringMode +
                ", redundancyMode=" + redundancyMode +
                ", redundantServerUrls=" + redundantServerUrls +
                ", industrialZone='" + industrialZone + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", areaId='" + areaId + '\'' +
                ", processId='" + processId + '\'' +
                ", status=" + status +
                ", lastConnected=" + lastConnected +
                ", lastDisconnected=" + lastDisconnected +
                ", connectionCount=" + connectionCount +
                ", errorCount=" + errorCount +
                ", metrics=" + metrics +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                '}';
    }

    //Metodo auxiliar
    private void updateStatus(ConnectionStatus newStatus) {
        this.status = newStatus;
        if (newStatus == ConnectionStatus.CONNECTED) {
            this.lastConnected = LocalDateTime.now();
            this.connectionCount++;
        } else if (newStatus == ConnectionStatus.DISCONNECTED) {
            this.lastDisconnected = LocalDateTime.now();
        }
    }

}
