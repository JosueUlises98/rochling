package org.kopingenieria.domain.classes;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_sessions")
@EntityListeners(AuditingEntityListener.class)
public final class SSHSession extends Session{

    @Serial
    private static final long serialVersionUID = 4L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de sesión
    @Column(name = "session_cache_size")
    private Integer sessionCacheSize;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout;

    @Column(name = "channel_timeout")
    private Integer channelTimeout;

    @Column(name = "destination_ip")
    private String destinationIP;

    @Column(name = "destination_port")
    private Integer destinationPort;

    @Override
    public String toString() {
        return "TCPSession{" +
                ", sessionId='" + sessionId + '\'' +
                ", clientAddress='" + clientAddress + '\'' +
                ",port " + port +
                ",protocolType='" + protocolType + '\'' +
                ", status=" + status +
                ",connection details='" + connectionDetails + '\'' +
                ",lastActivity=" + lastActivity +
                ",creationTime=" + creationTime +
                ",expirationTime=" + expirationTime +
                ",timeout=" + timeout +
                ", sessionCacheSize=" + sessionCacheSize +
                ", connectionTimeout=" + connectionTimeout +
                ", channelTimeout=" + channelTimeout +
                ", destinationIP='" + destinationIP + '\'' +
                ", destinationPort=" + destinationPort +
                '}';
    }

}
