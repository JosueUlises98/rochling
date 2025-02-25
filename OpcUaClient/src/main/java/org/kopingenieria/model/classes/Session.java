package org.kopingenieria.model.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.model.enums.network.ProtocolType;
import org.kopingenieria.model.enums.network.SessionStatus;
import java.time.LocalDateTime;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Session permits TCPSession,TLSSession,SSHSession,OpcUaSession {

    protected String sessionId; // A unique identifier for the session
    protected String clientAddress; // The client's network address
    protected int port; // The port used for the session
    protected Boolean isSecure; // Indicates if the session is secure
    protected ProtocolType protocolType; // The type of protocol (opcua, tcp, ssh, tls)
    protected SessionStatus status;
    protected Connection connectionDetails;
    protected LocalDateTime lastActivity;
    protected LocalDateTime creationTime;
    protected LocalDateTime expirationTime;
    protected Integer timeout;
}
