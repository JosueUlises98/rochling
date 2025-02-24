package org.kopingenieria.model.classes;

import org.kopingenieria.model.enums.network.ProtocolType;
import org.kopingenieria.model.enums.network.SessionStatus;
import java.io.Serializable;
import java.time.LocalDateTime;

public abstract class Session<T extends Session<T>> implements Serializable {

    protected String sessionId; // A unique identifier for the session
    protected String clientAddress; // The client's network address
    protected int port; // The port used for the session
    protected Boolean isSecure; // Indicates if the session is secure
    protected ProtocolType protocolType; // The type of protocol (opcua, tcp, ssh, tls)
    protected SessionStatus status;
    protected Connection<?> connectionDetails;
    protected LocalDateTime lastActivity;
    protected LocalDateTime creationTime;
    protected LocalDateTime expirationTime;
    protected Integer timeout;


    public T withSessionId(String sessionId) {
        this.sessionId = sessionId;
        return self();
    }

    public T withClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
        return self();
    }

    public T withPort(int port) {
        this.port = port;
        return self();
    }

    public T withVerification(Boolean isSecure) {
        this.isSecure = isSecure;
        return self();
    }

    public T withProtocol(ProtocolType protocolType) {
        this.protocolType = protocolType;
        return self();
    }

    public T withStatus(SessionStatus status) {
        this.status = status;
        return self();
    }

    public T withConnectionDetails(Connection<?> connectionDetails) {
        this.connectionDetails = connectionDetails;
        return self();
    }
    public T withLastActivity(LocalDateTime lastActivity) {
        this.lastActivity = lastActivity;
        return self();
    }
    public T withCreationTime(LocalDateTime creationTime) {
        this.creationTime = creationTime;
        return self();
    }
    public T withExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
        return self();
    }
    public T withTimeout(Integer timeout) {
        this.timeout = timeout;
        return self();
    }

    protected abstract T self();

    protected abstract Session<T> build();
}
