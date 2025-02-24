package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.kopingenieria.model.enums.ssh.SshAuthenticationType;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_connections")
@EntityListeners(AuditingEntityListener.class)
public class SSHConnection extends Connection<SSHConnection> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de autenticación
    @Enumerated(EnumType.STRING)
    private SshAuthenticationType authenticationType;

    @Column(length = 1024)
    private String password;

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "private_key_passphrase")
    private String privateKeyPassphrase;

    @Column(name = "known_hosts_path")
    private String knownHostsPath;

    // Configuración de sesión
    @Column(name = "session_timeout")
    private Integer sessionTimeout;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout;

    @Column(name = "channel_timeout")
    private Integer channelTimeout;

    // Configuración de seguridad
    @Column(name = "strict_host_checking")
    private Boolean strictHostKeyChecking;

    @ElementCollection
    @CollectionTable(
            name = "ssh_allowed_algorithms",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private Set<String> allowedAlgorithms;

    // Configuración de canal
    @Column(name = "max_packet_size")
    private Integer maxPacketSize;

    @Column(name = "window_size")
    private Integer windowSize;

    @Column(name = "compression_enabled")
    private Boolean compressionEnabled;

    @Column(name = "compression_level")
    private Integer compressionLevel;

    // Configuración de reintentos
    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "retry_delay")
    private Integer retryDelay;

    // Configuración de keepalive
    @Column(name = "keep_alive_interval")
    private Integer keepAliveInterval;

    @Column(name = "keep_alive_count_max")
    private Integer keepAliveCountMax;

    // Estados y banderas
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ConnectionStatus status;

    @Column(nullable = false)
    private Boolean enabled;

    // Métricas y monitoreo
    @Column(name = "last_connected_at")
    private LocalDateTime lastConnectedAt;

    @Column(name = "last_disconnected_at")
    private LocalDateTime lastDisconnectedAt;

    @Column(name = "connection_attempts")
    private Integer connectionAttempts;

    @Column(name = "failed_attempts")
    private Integer failedAttempts;

    // Auditoría
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
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

    // Configuración industrial específica
    @Column(name = "industrial_zone")
    private String industrialZone;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "priority_level")
    private Integer priorityLevel;

    @Column(name = "maintenance_window")
    private String maintenanceWindow;

    @Override
    protected SSHConnection self() {
        return this;
    }

    @Override
    public SSHConnection build() {
        SSHConnection sc = new SSHConnection();
        sc.name=this.name;
        sc.hostname=this.hostname;
        sc.port=this.port;
        sc.username=this.username;
        sc.password=this.password;
        sc.privateKeyPath=this.privateKeyPath;
        sc.privateKeyPassphrase=this.privateKeyPassphrase;
        sc.knownHostsPath=this.knownHostsPath;
        sc.sessionTimeout=this.sessionTimeout;
        sc.connectionTimeout=this.connectionTimeout;
        sc.channelTimeout=this.channelTimeout;
        sc.strictHostKeyChecking=this.strictHostKeyChecking;
        sc.allowedAlgorithms=this.allowedAlgorithms;
        sc.maxPacketSize=this.maxPacketSize;
        sc.windowSize=this.windowSize;
        sc.compressionEnabled=this.compressionEnabled;
        sc.compressionLevel=this.compressionLevel;
        sc.maxRetries=this.maxRetries;
        sc.retryDelay=this.retryDelay;
        sc.keepAliveInterval=this.keepAliveInterval;
        sc.keepAliveCountMax=this.keepAliveCountMax;
        sc.status=this.status;
        sc.enabled=this.enabled;
        sc.lastConnectedAt=this.lastConnectedAt;
        sc.lastDisconnectedAt=this.lastDisconnectedAt;
        sc.connectionAttempts=this.connectionAttempts;
        sc.failedAttempts=this.failedAttempts;
        sc.createdAt=this.createdAt;
        sc.updatedAt=this.updatedAt;
        sc.createdBy=this.createdBy;
        sc.updatedBy=this.updatedBy;
        sc.industrialZone=this.industrialZone;
        sc.equipmentId=this.equipmentId;
        sc.priorityLevel=this.priorityLevel;
        sc.maintenanceWindow=this.maintenanceWindow;
        return sc;
    }

    @Override
    public String toString() {
        return "SSHConnection{" +
                "name=" + name +
                ", hostname=" + hostname +
                ", port=" + port +
                ", username=" + username +
                ", authenticationType=" + authenticationType +
                ", password='" + password + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", privateKeyPassphrase='" + privateKeyPassphrase + '\'' +
                ", knownHostsPath='" + knownHostsPath + '\'' +
                ", sessionTimeout=" + sessionTimeout +
                ", connectionTimeout=" + connectionTimeout +
                ", channelTimeout=" + channelTimeout +
                ", strictHostKeyChecking=" + strictHostKeyChecking +
                ", allowedAlgorithms=" + allowedAlgorithms +
                ", maxPacketSize=" + maxPacketSize +
                ", windowSize=" + windowSize +
                ", compressionEnabled=" + compressionEnabled +
                ", compressionLevel=" + compressionLevel +
                ", maxRetries=" + maxRetries +
                ", retryDelay=" + retryDelay +
                ", keepAliveInterval=" + keepAliveInterval +
                ", keepAliveCountMax=" + keepAliveCountMax +
                ", status=" + status +
                ", enabled=" + enabled +
                ", lastConnectedAt=" + lastConnectedAt +
                ", lastDisconnectedAt=" + lastDisconnectedAt +
                ", connectionAttempts=" + connectionAttempts +
                ", failedAttempts=" + failedAttempts +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", industrialZone='" + industrialZone + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", priorityLevel=" + priorityLevel +
                ", maintenanceWindow='" + maintenanceWindow + '\'' +
                '}';
    }
}
