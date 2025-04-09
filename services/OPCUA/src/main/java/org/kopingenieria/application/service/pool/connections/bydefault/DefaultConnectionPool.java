package org.kopingenieria.application.service.pool.connections.bydefault;

import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.response.connection.ConnectionResponse;
import org.kopingenieria.application.service.connection.bydefault.DefaultConnectionImpl;
import org.kopingenieria.application.service.pool.clients.bydefault.OpcUaDefaultPool;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;
import org.kopingenieria.exception.exceptions.ConnectionPoolException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

@Component( "defaultConnectionPool")
@RequiredArgsConstructor
public class DefaultConnectionPool implements AutoCloseable {


    public static final int DEFAULT_POOL_SIZE = 10;
    public static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);
    public static final Duration HEALTH_CHECK_INTERVAL = Duration.ofMinutes(1);
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final boolean ENABLE_BACKOFF_RETRY = true;

    @Data
    @Builder
    public static class PoolConfig {
        private final int maxPoolSize;
        private final int minPoolSize;
        private final Duration connectionTimeout;
        private final Duration healthCheckInterval;
        private final int maxRetryAttempts;
        private final boolean enableBackoffRetry;
    }

    private final BlockingQueue<PooledConnection> availableConnections;
    private final Map<String, PooledConnection> activeConnections;
    private final ReentrantReadWriteLock poolLock;
    private final ScheduledExecutorService healthCheckExecutor;
    private final PoolConfig config;
    private volatile boolean isShutdown;

    @Data
    public class PooledConnection {
        private final String connectionId;
        private final DefaultConnectionImpl connection;
        private volatile LocalDateTime lastUsed;
        private volatile ConnectionStatus status;
        private AtomicInteger failureCount;
        private UrlType currentUrl;

        public PooledConnection(DefaultConnectionImpl connection) {
            this.connectionId = UUID.randomUUID().toString();
            this.connection = connection;
            this.lastUsed = LocalDateTime.now();
            this.status = ConnectionStatus.DISCONNECTED;
            this.failureCount = new AtomicInteger(0);
        }

        public CompletableFuture<ConnectionResponse> validateConnection() throws Exception {
            return connection.ping()
                    .thenApply(response -> {
                        if (response.getStatus().equals(ConnectionStatus.CONNECTED)) {
                            failureCount.set(0);
                            status = ConnectionStatus.CONNECTED;
                            return ConnectionResponse.builder()
                                    .status(ConnectionStatus.CONNECTED)
                                    .build();
                        }
                        handleFailure();
                        return ConnectionResponse.builder()
                                .status(ConnectionStatus.ERROR)
                                .build();
                    })
                    .exceptionally(ex -> {
                        handleFailure();
                        return ConnectionResponse.builder()
                                .status(ConnectionStatus.ERROR)
                                .build();
                    });
        }

        private void handleFailure() {
            int failures = failureCount.incrementAndGet();
            status = ConnectionStatus.ERROR;
            if (failures >= config.getMaxRetryAttempts()) {
                status = ConnectionStatus.FAILED;
            }
        }

        public CompletableFuture<ConnectionResponse> reconnect() throws Exception {
            if (config.isEnableBackoffRetry()) {
                return connection.backoffreconnection(currentUrl);
            }
            return connection.linearreconnection(currentUrl);
        }
    }

    public DefaultConnectionPool(PoolConfig config, List<String>clients) throws ConnectionPoolException {
        this.config = config;
        this.availableConnections = new LinkedBlockingQueue<>(config.getMaxPoolSize());
        this.activeConnections = new ConcurrentHashMap<>();
        this.poolLock = new ReentrantReadWriteLock();
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        this.isShutdown = false;
        initializePool(clients);
        startHealthCheck(clients.getFirst());
    }

    private void initializePool(List<String>clientIds) throws ConnectionPoolException {
        poolLock.writeLock().lock();
        try {
            for (int i = 0; i < config.getMinPoolSize(); i++) {
                createAndAddConnection(clientIds.get(i));
            }
        } finally {
            poolLock.writeLock().unlock();
        }
    }

    private void createAndAddConnection(String clientId) throws ConnectionPoolException {
        try {
            DefaultConnectionImpl connection = new DefaultConnectionImpl(clientId);
            PooledConnection pooledConnection = new PooledConnection(connection);
            availableConnections.offer(pooledConnection);
        } catch (Exception e) {
            throw new ConnectionPoolException("Failed to create connection", e);
        }
    }

    public CompletableFuture<PooledConnection> acquireConnection(UrlType url, String clientId) {
        validatePoolState();
        return CompletableFuture.supplyAsync(() -> {
            try {
                PooledConnection connection = availableConnections.poll(
                        config.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS);

                if (connection == null) {
                    if (getTotalConnections() < config.getMaxPoolSize()) {
                        connection = availableConnections.poll();
                        if (connection == null) {
                            createAndAddConnection(clientId);
                        }
                    } else {
                        throw new ConnectionPoolException("Connection pool exhausted");
                    }
                }
                assert connection != null;
                return prepareConnection(connection, url);
            } catch (Exception e) {
                try {
                    throw new ConnectionPoolException("Failed to acquire connection", e);
                } catch (ConnectionPoolException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }

    private PooledConnection prepareConnection(PooledConnection connection, UrlType url) throws Exception {
        return connection.validateConnection()
                .thenCompose(valid -> {
                    if (valid.getStatus().equals(ConnectionStatus.ERROR)) {
                        try {
                            return connection.reconnect()
                                    .thenApply(response -> {
                                        if (!response.getStatus().equals(ConnectionStatus.CONNECTED)) {
                                            try {
                                                throw new ConnectionPoolException("Failed to reconnect");
                                            } catch (ConnectionPoolException e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                        return connection;
                                    });
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return CompletableFuture.completedFuture(connection);
                })
                .thenApply(conn -> {
                    conn.setCurrentUrl(url);
                    conn.setLastUsed(LocalDateTime.now());
                    activeConnections.put(conn.getConnectionId(), conn);
                    return conn;
                })
                .join();
    }

    public void releaseConnection(PooledConnection connection, String clientId) throws Exception {
        if (connection == null) return;

        poolLock.writeLock().lock();
        try {
            activeConnections.remove(connection.getConnectionId());
            if (connection.getStatus() != ConnectionStatus.FAILED) {
                availableConnections.offer(connection);
            } else {
                replaceFailedConnection(connection,clientId);
            }
        } finally {
            poolLock.writeLock().unlock();
        }
    }

    private void replaceFailedConnection(PooledConnection failedConnection, String clientId) throws Exception {
            failedConnection.getConnection().close();
            if (!isShutdown && getTotalConnections() < config.getMinPoolSize()) {
                createAndAddConnection(clientId);
            }
    }

    private void startHealthCheck(String clientId) {
        healthCheckExecutor.scheduleAtFixedRate(
                () -> performHealthCheck(clientId),
                config.getHealthCheckInterval().toMillis(),
                config.getHealthCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    private void performHealthCheck(String clientId) {
        poolLock.readLock().lock();
        try {
            List<CompletableFuture<Void>> healthChecks = new ArrayList<>();

            availableConnections.forEach(conn ->
                    {
                        try {
                            healthChecks.add(conn.validateConnection()
                                    .thenAccept(valid -> {
                                        if (valid.getStatus().equals(ConnectionStatus.ERROR)) {
                                            try {
                                                handleUnhealthyConnection(conn,clientId);
                                            } catch (Exception e) {
                                                throw new RuntimeException(e);
                                            }
                                        }
                                    }));
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
            );

            CompletableFuture.allOf(healthChecks.toArray(new CompletableFuture[0]))
                    .exceptionally(ex -> null);
        } finally {
            poolLock.readLock().unlock();
        }
    }

    private void handleUnhealthyConnection(PooledConnection connection, String clientId) throws Exception {
        poolLock.writeLock().lock();
        try {
            availableConnections.remove(connection);
            replaceFailedConnection(connection,clientId);
        } finally {
            poolLock.writeLock().unlock();
        }
    }

    private int getTotalConnections() {
        return availableConnections.size() + activeConnections.size();
    }

    private void validatePoolState() {
        if (isShutdown) {
            throw new IllegalStateException("Connection pool is shutdown");
        }
    }

    @PreDestroy
    @Override
    public void close() {
        isShutdown = true;
        healthCheckExecutor.shutdown();

        poolLock.writeLock().lock();
        try {
            Stream.concat(
                    availableConnections.stream(),
                    activeConnections.values().stream()
            ).forEach(conn -> {
                try {
                    conn.getConnection().close();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            availableConnections.clear();
            activeConnections.clear();
        } finally {
            poolLock.writeLock().unlock();
        }
    }

}
