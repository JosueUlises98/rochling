package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.kopingenieria.model.enums.ssh.MonitoringMode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDateTime;

@Data
@Builder
public class QualityConnection {

    // Configuración de monitoreo común
    @Column(name = "sampling_interval")
    private Double samplingInterval;

    @Column(name = "queue_size")
    private Integer queueSize;

    @Column(name = "discard_oldest")
    private Boolean discardOldest;

    @Column(name = "monitoring_mode")
    @Enumerated(EnumType.STRING)
    private MonitoringMode monitoringMode;

    // Configuración industrial común
    @Column(name = "industrial_zone")
    private String industrialZone;

    @Column(name = "equipment_id")
    private String equipmentId;

    @Column(name = "area_id")
    private String areaId;

    @Column(name = "process_id")
    private String processId;

    // Estado y diagnóstico común
    @Enumerated(EnumType.STRING)
    private ConnectionStatus status;

    @Column(name = "last_connected")
    private LocalDateTime lastConnected;

    @Column(name = "last_connecting")
    private LocalDateTime lastConnecting;

    @Column(name = "last_disconnected")
    private LocalDateTime lastDisconnected;

    @Column(name = "last_disconnecting")
    private LocalDateTime lastDisconnecting;

    @Column(name = "last_reconnecting")
    private LocalDateTime lastReconnecting;
    
    @Column(name = "last_reconnected")
    private LocalDateTime lastReconnected;

    @Column(name = "last_failed_connection")
    private LocalDateTime lastFailed;

    @Column(name = "connection_count")
    private Integer connectionCount;

    @Column(name = "connecting_count")
    private Integer connectingCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "unknown_status_count")
    private Integer unknownStatusCount;

    @Column(name = "reconnect_count")
    private Integer reconnectCount;

    @Column(name = "reconnecting_count")
    private Integer reconnectingCount;

    @Column(name = "failedcount")
    private Integer failedcount;

    @Column(name = "disconnectedcount")
    private Integer disconnectedcount;

    // Campos de auditoría comunes
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

    // Métricas
    @Embedded
    private OpcUaMetrics metrics;

    // Clase embebida para métricas
    @Embeddable
    @Data
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
    public String toString() {
        return "QualityConnection{" +
                "samplingInterval=" + samplingInterval +
                ", queueSize=" + queueSize +
                ", discardOldest=" + discardOldest +
                ", monitoringMode=" + monitoringMode +
                ", industrialZone='" + industrialZone + '\'' +
                ", equipmentId='" + equipmentId + '\'' +
                ", areaId='" + areaId + '\'' +
                ", processId='" + processId + '\'' +
                ", status=" + status +
                ", lastConnected=" + lastConnected +
                ", lastConnecting=" + lastConnecting +
                ", lastDisconnected=" + lastDisconnected +
                ", lastDisconnecting=" + lastDisconnecting +
                ", lastReconnecting=" + lastReconnecting +
                ", lastReconnected=" + lastReconnected +
                ", lastFailed=" + lastFailed +
                ", connectionCount=" + connectionCount +
                ", connectingCount=" + connectingCount +
                ", errorCount=" + errorCount +
                ", unknownStatusCount=" + unknownStatusCount +
                ", reconnectCount=" + reconnectCount +
                ", reconnectingCount=" + reconnectingCount +
                ", failedcount=" + failedcount +
                ", disconnectedcount=" + disconnectedcount +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", createdBy='" + createdBy + '\'' +
                ", updatedBy='" + updatedBy + '\'' +
                ", metrics=" + metrics +
                '}';
    }
}
