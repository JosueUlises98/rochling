package org.kopingenieria.model.classes;

import org.kopingenieria.model.enums.network.ProtocolType;
import org.kopingenieria.model.enums.network.SessionStatus;
import java.time.LocalDateTime;


public abstract sealed class Session permits TLSSession{

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
