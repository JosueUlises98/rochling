package org.kopingenieria.application.service.pool.connections;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.response.connection.ConnectionResponse;
import org.kopingenieria.application.service.pool.connections.bydefault.DefaultConnectionPool;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.QualityLevel;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.exception.exceptions.ConnectionPoolException;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class DefaultConnectionPoolService {

    private final DefaultConnectionPool connectionPool;

    public DefaultConnectionPool createDefaultPool(List<String> clients) throws ConnectionPoolException {
        return createCustomPool(
                DefaultConnectionPool.PoolConfig.builder()
                        .maxPoolSize(DefaultConnectionPool.DEFAULT_POOL_SIZE)
                        .minPoolSize(DefaultConnectionPool.DEFAULT_POOL_SIZE / 2)
                        .connectionTimeout(DefaultConnectionPool.CONNECTION_TIMEOUT)
                        .healthCheckInterval(DefaultConnectionPool.HEALTH_CHECK_INTERVAL)
                        .maxRetryAttempts(DefaultConnectionPool.MAX_RETRY_ATTEMPTS)
                        .enableBackoffRetry(DefaultConnectionPool.ENABLE_BACKOFF_RETRY)
                        .build(),
                clients
        );
    }

    public DefaultConnectionPool createCustomPool(DefaultConnectionPool.PoolConfig config, List<String> clients)
            throws ConnectionPoolException {
        try {
            return new DefaultConnectionPool(config, clients);
        } catch (Exception e) {
            throw new ConnectionPoolException("Error al crear pool de conexiones", e);
        }
    }

    public CompletableFuture<ConnectionResponse> getConnection(UrlType urlType, String clientId) {
        return connectionPool.acquireConnection(urlType, clientId)
                .thenApply(this::mapToConnectionResponse);
    }

    public void releaseConnection(DefaultConnectionPool.PooledConnection connection, String clientId)
            throws Exception {
        connectionPool.releaseConnection(connection, clientId);
    }

    public CompletableFuture<ConnectionResponse> validateConnection(
            DefaultConnectionPool.PooledConnection connection) throws Exception {
        return connection.validateConnection()
                .thenApply(response -> mapToConnectionResponse(connection));
    }

    public CompletableFuture<ConnectionResponse> reconnect(
            DefaultConnectionPool.PooledConnection connection) throws Exception {
        return connection.reconnect()
                .thenApply(response -> mapToConnectionResponse(connection));
    }

    private ConnectionResponse mapToConnectionResponse(DefaultConnectionPool.PooledConnection pooledConnection) {
        return ConnectionResponse.builder()
                .id(pooledConnection.getConnectionId())
                .endpointUrl(pooledConnection.getCurrentUrl().toString())
                .applicationName(pooledConnection.getConnectionId())
                .applicationUri(pooledConnection.getConnection().getApplicationUri())
                .productUri(pooledConnection.getConnection().getProductUri())
                .status(pooledConnection.getStatus())
                .quality(determineQualityLevel(pooledConnection))
                .lastActivity(pooledConnection.getLastUsed())
                .build();
    }

    private QualityLevel determineQualityLevel(DefaultConnectionPool.PooledConnection connection) {
        if (connection.getStatus() == ConnectionStatus.CONNECTED) {
            return connection.getFailureCount().get() == 0 ?
                    QualityLevel.GOOD : QualityLevel.POOR;
        }
        return QualityLevel.FAIR;
    }

    public void close() throws Exception {
        connectionPool.close();
    }
}
