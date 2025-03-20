package org.kopingenieria.application.db.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.domain.enums.communication.SessionStatus;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Session implements Serializable permits OpcUaSession {

    @Serial
    private static final long serialVersionUID = 500L;

    //Atributos base
    protected String sessionId;
    protected String clientAddress;
    protected Integer port;
    protected Boolean isSecure;
    protected ConnectionType protocolType;
    protected SessionStatus status;
    protected Connection connectionDetails;
    protected LocalDateTime lastActivity;
    protected LocalDateTime creationTime;
    protected LocalDateTime expirationTime;
    protected Integer timeout;
}
