package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tcp_sessions")
@EntityListeners(AuditingEntityListener.class)
public final class TCPSession extends Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "destination_ip")
    private String destinationIP;
    @Column(name = "destination_port")
    private int destinationPort;

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
                ", destinationIP='" + destinationIP + '\'' +
                ", destinationPort=" + destinationPort +
                '}';
    }
}
