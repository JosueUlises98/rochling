package org.kopingenieria.model.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.model.enums.network.connection.ProtocolType;
import org.kopingenieria.model.enums.network.communication.SessionStatus;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Session implements Serializable permits TCPSession,TLSSession,SSHSession,OpcUaSession {

    protected String sessionId;
    protected String clientAddress;
    protected Integer port;
    protected Boolean isSecure;
    protected ProtocolType protocolType;
    protected SessionStatus status;
    protected Connection connectionDetails;
    protected LocalDateTime lastActivity;
    protected LocalDateTime creationTime;
    protected LocalDateTime expirationTime;
    protected Integer timeout;
}
