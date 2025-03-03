package org.kopingenieria.domain.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kopingenieria.exceptions.InvalidConnectionStateTransitionException;
import org.kopingenieria.domain.enums.client.network.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.client.network.connection.ConnectionType;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;


@SuperBuilder(toBuilder = true)
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public abstract sealed class Connection implements Serializable permits TCPConnection, TLSConnection, SSHConnection, OpcUaConnection{

    @Serial
    private static final long serialVersionUID = 194L;

    private static final Logger logger = LogManager.getLogger(Connection.class);

    private static final String INVALID_TRANSITION_MESSAGE = "Invalid transition from %s to %s";

    protected String name;
    protected String hostname;
    protected Integer port;
    protected String method;
    protected ConnectionType type;
    protected ConnectionStatus status;
    protected QualityNetwork qualityConnection;

    {
        status = ConnectionStatus.UNKNOWN;
    }

    private static final Map<ConnectionStatus, ConnectionStateHandler> STATE_HANDLERS = Map.of(
            ConnectionStatus.CONNECTED, quality -> {
                quality.setLastConnected(LocalDateTime.now());
                quality.setConnectionCount(Optional.ofNullable(quality.getConnectionCount()).map(count -> count + 1).orElse(1));
            },
            ConnectionStatus.DISCONNECTED, quality ->
                    quality.setLastDisconnected(LocalDateTime.now()),
            ConnectionStatus.ERROR, quality -> {
                quality.setErrorCount(Optional.ofNullable(quality.getErrorCount()).map(count -> count + 1).orElse(1));
            },
            ConnectionStatus.RECONNECTING, quality ->
                    quality.setLastReconnecting(LocalDateTime.now()),
            ConnectionStatus.UNKNOWN, quality -> {
                quality.setUnknownStatusCount(Optional.ofNullable(quality.getUnknownStatusCount()).map(count -> count + 1).orElse(1));
            },
            ConnectionStatus.CONNECTING, quality -> {
                quality.setLastConnecting(LocalDateTime.now());
                quality.setConnectingCount(Optional.ofNullable(quality.getConnectingCount()).map(count -> count + 1).orElse(1));
            },
            ConnectionStatus.DISCONNECTING, quality ->
                    quality.setLastDisconnecting(LocalDateTime.now()),
            ConnectionStatus.FAILED, quality -> {
                quality.setLastFailed(LocalDateTime.now());
                quality.setFailedcount(Optional.ofNullable(quality.getFailedcount()).map(count -> count + 1).orElse(1));
            }
    );

    private static final Map<ConnectionStatus, Set<ConnectionStatus>> VALID_TRANSITIONS = Map.of(
            ConnectionStatus.CONNECTED, Set.of(ConnectionStatus.DISCONNECTING, ConnectionStatus.ERROR, ConnectionStatus.RECONNECTING),
            ConnectionStatus.DISCONNECTED, Set.of(ConnectionStatus.CONNECTING, ConnectionStatus.ERROR),
            ConnectionStatus.ERROR, Set.of(ConnectionStatus.RECONNECTING, ConnectionStatus.DISCONNECTED),
            ConnectionStatus.RECONNECTING, Set.of(ConnectionStatus.CONNECTED, ConnectionStatus.ERROR),
            ConnectionStatus.UNKNOWN, Set.of(ConnectionStatus.CONNECTING),
            ConnectionStatus.CONNECTING, Set.of(ConnectionStatus.CONNECTED, ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR),
            ConnectionStatus.DISCONNECTING, Set.of(ConnectionStatus.DISCONNECTED, ConnectionStatus.ERROR),
            ConnectionStatus.FAILED, Set.of(ConnectionStatus.RECONNECTING, ConnectionStatus.ERROR)
    );

    private void validateTransition(ConnectionStatus currentStatus, ConnectionStatus newStatus) throws InvalidConnectionStateTransitionException {
        if (!VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of()).contains(newStatus)) {
            String message = String.format(INVALID_TRANSITION_MESSAGE, currentStatus, newStatus);
            logger.error(message);
            throw new InvalidConnectionStateTransitionException(message);
        }
    }

    protected void updateStatus(ConnectionStatus newStatus) throws InvalidConnectionStateTransitionException {
        Objects.requireNonNull(newStatus, "New status cannot be null");
        Objects.requireNonNull(qualityConnection, "QualityConnection cannot be null");
        logger.debug("Attempting status transition from {} to {}", this.status, newStatus);
        validateTransition(this.status, newStatus);
        updateConnectionStatus(newStatus);
        updateQualityMetrics(newStatus);
        logger.info("Status successfully updated from {} to {}", this.status, newStatus);
    }

    private void updateConnectionStatus(ConnectionStatus newStatus) {
        this.status = newStatus;
    }

    private void updateQualityMetrics(ConnectionStatus newStatus) {
        ConnectionStateHandler handler = STATE_HANDLERS.getOrDefault(
                newStatus,
                (quality) -> logger.warn("No specific handler for status: {}", newStatus)
        );
        handler.handle(qualityConnection);
    }

}



