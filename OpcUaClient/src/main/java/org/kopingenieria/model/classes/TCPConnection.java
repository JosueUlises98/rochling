package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.kopingenieria.exceptions.InvalidConnectionStateTransitionException;
import org.kopingenieria.model.enums.network.ConnectionStatus;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tcp_connections")
@EntityListeners(AuditingEntityListener.class)
public final class TCPConnection extends Connection {

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

    @Override
    public String toString() {
        return "TCPConnection{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", qualityConnection=" + qualityConnection +
                ", socketTimeout=" + socketTimeout +
                ", connectTimeout=" + connectTimeout +
                ", sendBufferSize=" + sendBufferSize +
                ", receiveBufferSize=" + receiveBufferSize +
                ", keepAlive=" + keepAlive +
                ", tcpNoDelay=" + tcpNoDelay +
                ", reuseAddress=" + reuseAddress +
                ", lingerTime=" + lingerTime +
                ", trafficClass=" + trafficClass +
                ", typeOfService=" + typeOfService +
                "} ";
    }

    public void updateConnectionStatus(ConnectionStatus newStatus) throws InvalidConnectionStateTransitionException {
        super.updateStatus(newStatus);
    }

}
