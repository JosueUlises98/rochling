package org.kopingenieria.model.classes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Builder
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TCPConnection extends Connection<TCPConnection> {

    // Configuración de socket
    @Column(name = "socket_timeout")
    private Integer socketTimeout;

    @Column(name = "connect_timeout")
    private Integer connectTimeout;

    @Column(name = "send_buffer_size")
    private Integer sendBufferSize;

    @Column(name = "receive_buffer_size")
    private Integer receiveBufferSize;

    @Column(name = "keep_alive")
    private Boolean keepAlive;

    @Column(name = "tcp_no_delay")
    private Boolean tcpNoDelay;

    @Column(name = "reuse_address")
    private Boolean reuseAddress;

    @Column(name = "linger_time")
    private Integer lingerTime;

    // Control de tráfico
    @Column(name = "traffic_class")
    private Integer trafficClass;

    @Column(name = "type_of_service")
    private Integer typeOfService;

    // Configuración de reintentos
    @Column(name = "max_retries")
    private Integer maxRetries;

    @Column(name = "retry_interval")
    private Integer retryInterval;

    // Estado y monitoreo
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

    @Column(name = "bytes_sent")
    private Long bytesSent;

    @Column(name = "bytes_received")
    private Long bytesReceived;

    // Configuración industrial
    @Column(name = "industrial_zone")
    private String industrialZone;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "priority")
    private Integer priority;

    // Campos de auditoría
    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    protected TCPConnection self() {
        return this;
    }

    @Override
    protected TCPConnection build() {
        TCPConnection sc = new TCPConnection();
        sc.name=this.name;
        sc.hostname=this.hostname;
        sc.port=this.port;
        sc.username=this.username;
        sc.password=this.password;
        sc.method=this.method;
        sc.type=this.type;
        sc.socketTimeout=this.socketTimeout;
        sc.connectTimeout=this.connectTimeout;
        sc.sendBufferSize=this.sendBufferSize;
        sc.receiveBufferSize=this.receiveBufferSize;
        sc.keepAlive=this.keepAlive;
        sc.tcpNoDelay=this.tcpNoDelay;
        sc.reuseAddress=this.reuseAddress;
        sc.lingerTime=this.lingerTime;
        sc.trafficClass=this.trafficClass;
        sc.typeOfService=this.typeOfService;
        sc.maxRetries=this.maxRetries;
        sc.retryInterval=this.retryInterval;
        sc.status=this.status;
        sc.lastConnected=this.lastConnected;
        sc.lastDisconnected=this.lastDisconnected;
        sc.connectionCount=this.connectionCount;
        sc.errorCount=this.errorCount;
        sc.bytesSent=this.bytesSent;
        sc.bytesReceived=this.bytesReceived;
        sc.industrialZone=this.industrialZone;
        sc.equipmentId=this.equipmentId;
        sc.priority=this.priority;
        sc.createdAt=this.createdAt;
        sc.updatedAt=this.updatedAt;
        return sc;
    }

    @Override
    public String toString() {
        return "TCPConnection{" +
                "socketTimeout=" + socketTimeout +
                ", connectTimeout=" + connectTimeout +
                ", sendBufferSize=" + sendBufferSize +
                ", receiveBufferSize=" + receiveBufferSize +
                ", keepAlive=" + keepAlive +
                ", tcpNoDelay=" + tcpNoDelay +
                ", reuseAddress=" + reuseAddress +
                ", lingerTime=" + lingerTime +
                ", trafficClass=" + trafficClass +
                ", typeOfService=" + typeOfService +
                ", maxRetries=" + maxRetries +
                ", retryInterval=" + retryInterval +
                ", status=" + status +
                ", lastConnected=" + lastConnected +
                ", lastDisconnected=" + lastDisconnected +
                ", connectionCount=" + connectionCount +
                ", errorCount=" + errorCount +
                ", bytesSent=" + bytesSent +
                ", bytesReceived=" + bytesReceived +
                ", industrialZone='" + industrialZone + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", priority=" + priority +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", method='" + method + '\'' +
                ", type='" + type + '\'' +
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
