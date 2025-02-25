package org.kopingenieria.model.classes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_sessions")
@EntityListeners(AuditingEntityListener.class)
public final class TLSSession extends Session {

    // Configuración de sesión
    @Column(name = "session_timeout")
    private Integer sessionTimeout;

    @Column(name = "session_cache_size")
    private Integer sessionCacheSize;

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
                ", sessionTimeout=" + sessionTimeout +
                ", sessionCacheSize=" + sessionCacheSize +
                '}';
    }





}
