package org.kopingenieria.domain.classes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.exceptions.InvalidConnectionStateTransitionException;
import org.kopingenieria.domain.enums.client.network.connection.ConnectionStatus;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_connections")
@EntityListeners(AuditingEntityListener.class)
public final class SSHConnection extends Connection {

    @Serial
    private static final long serialVersionUID = 963L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de canal
    @Column(name = "max_packet_size")
    private Integer maxPacketSize = ChannelDefaults.DEFAULT_MAX_PACKET_SIZE;

    @Column(name = "window_size")
    private Integer windowSize = ChannelDefaults.DEFAULT_WINDOW_SIZE;

    @Column(name = "compression_enabled")
    private Boolean compressionEnabled = ChannelDefaults.DEFAULT_COMPRESSION_ENABLED;

    @Column(name = "compression_level")
    private Integer compressionLevel = ChannelDefaults.DEFAULT_COMPRESSION_LEVEL;

    static class ChannelDefaults{
        public static final int DEFAULT_MAX_PACKET_SIZE = 32768;
        public static final int DEFAULT_WINDOW_SIZE = 2097152;
        public static final boolean DEFAULT_COMPRESSION_ENABLED = true;
        public static final int DEFAULT_COMPRESSION_LEVEL = 6;
    }

    @Override
    public String toString() {
        return "SSHConnection{" +
                "name='" + name + '\'' +
                ", hostname='" + hostname + '\'' +
                ", port=" + port +
                ", method='" + method + '\'' +
                ", type=" + type +
                ", status=" + status +
                ", qualityConnection=" + qualityConnection +
                ", maxPacketSize=" + maxPacketSize +
                ", windowSize=" + windowSize +
                ", compressionEnabled=" + compressionEnabled +
                ", compressionLevel=" + compressionLevel +
                "} ";
    }

    public void updateConnectionStatus(ConnectionStatus newStatus) throws InvalidConnectionStateTransitionException {
        super.updateStatus(newStatus);
    }
}
