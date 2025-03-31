package org.kopingenieria.application.service.opcua.pool.connection;

import jakarta.annotation.PreDestroy;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.response.OpcUaConnectionResponse;
import org.kopingenieria.application.service.opcua.workflow.OpcuaConnection;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.exception.exceptions.ConnectionPoolException;
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

@Service
@RequiredArgsConstructor
public class OpcUaConnectionPool implements AutoCloseable {

    private static final int DEFAULT_POOL_SIZE = 10;
    private static final Duration HEALTH_CHECK_INTERVAL = Duration.ofMinutes(1);
    private static final Duration CONNECTION_TIMEOUT = Duration.ofSeconds(10);

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
    private class PooledConnection {
        private final String connectionId;
        private final OpcuaConnection connection;
        private volatile LocalDateTime lastUsed;
        private volatile ConnectionStatus status;
        private AtomicInteger failureCount;
        private UrlType currentUrl;

        public PooledConnection(OpcuaConnection connection) {
            this.connectionId = UUID.randomUUID().toString();
            this.connection = connection;
            this.lastUsed = LocalDateTime.now();
            this.status = ConnectionStatus.DISCONNECTED;
            this.failureCount = new AtomicInteger(0);
        }

        public CompletableFuture<Boolean> validateConnection() throws Exception {
            return connection.ping()
                    .thenApply(response -> {
                        if (response.getStatus().equals(ConnectionStatus.CONNECTED)) {
                            failureCount.set(0);
                            status = ConnectionStatus.CONNECTED;
                            return true;
                        }
                        handleFailure();
                        return false;
                    })
                    .exceptionally(ex -> {
                        handleFailure();
                        return false;
                    });
        }

        private void handleFailure() {
            int failures = failureCount.incrementAndGet();
            status = ConnectionStatus.ERROR;
            if (failures >= config.getMaxRetryAttempts()) {
                status = ConnectionStatus.FAILED;
            }
        }

        public CompletableFuture<OpcUaConnectionResponse> reconnect() throws Exception {
            if (config.isEnableBackoffRetry()) {
                return connection.backoffreconnection(currentUrl);
            }
            return connection.linearreconnection(currentUrl);
        }
    }

    public OpcUaConnectionPool(PoolConfig config) throws ConnectionPoolException {
        this.config = config;
        this.availableConnections = new LinkedBlockingQueue<>(config.getMaxPoolSize());
        this.activeConnections = new ConcurrentHashMap<>();
        this.poolLock = new ReentrantReadWriteLock();
        this.healthCheckExecutor = Executors.newSingleThreadScheduledExecutor();
        this.isShutdown = false;
        initializePool();
        startHealthCheck();
    }

    private void initializePool() throws ConnectionPoolException {
        poolLock.writeLock().lock();
        try {
            for (int i = 0; i < config.getMinPoolSize(); i++) {
                createAndAddConnection();
            }
        } finally {
            poolLock.writeLock().unlock();
        }
    }

    private void createAndAddConnection() throws ConnectionPoolException {
        try {
            OpcuaConnection connection = new OpcuaConnection();
            PooledConnection pooledConnection = new PooledConnection(connection);
            availableConnections.offer(pooledConnection);
        } catch (Exception e) {
            throw new ConnectionPoolException("Failed to create connection", e);
        }
    }

    public CompletableFuture<PooledConnection> acquireConnection(UrlType url) {
        validatePoolState();
        return CompletableFuture.supplyAsync(() -> {
            try {
                PooledConnection connection = availableConnections.poll(
                        config.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS);

                if (connection == null) {
                    if (getTotalConnections() < config.getMaxPoolSize()) {
                        connection = availableConnections.poll();
                        if (connection == null) {
                            createAndAddConnection();
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
                    if (!valid) {
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

    public void releaseConnection(PooledConnection connection) throws Exception {
        if (connection == null) return;

        poolLock.writeLock().lock();
        try {
            activeConnections.remove(connection.getConnectionId());
            if (connection.getStatus() != ConnectionStatus.FAILED) {
                availableConnections.offer(connection);
            } else {
                replaceFailedConnection(connection);
            }
        } finally {
            poolLock.writeLock().unlock();
        }
    }

    private void replaceFailedConnection(PooledConnection failedConnection) throws Exception {
            failedConnection.getConnection().close();
            if (!isShutdown && getTotalConnections() < config.getMinPoolSize()) {
                createAndAddConnection();
            }
    }

    private void startHealthCheck() {
        healthCheckExecutor.scheduleAtFixedRate(
                this::performHealthCheck,
                config.getHealthCheckInterval().toMillis(),
                config.getHealthCheckInterval().toMillis(),
                TimeUnit.MILLISECONDS
        );
    }

    private void performHealthCheck() {
        poolLock.readLock().lock();
        try {
            List<CompletableFuture<Void>> healthChecks = new ArrayList<>();

            availableConnections.forEach(conn ->
                    {
                        try {
                            healthChecks.add(conn.validateConnection()
                                    .thenAccept(valid -> {
                                        if (!valid) {
                                            try {
                                                handleUnhealthyConnection(conn);
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

    private void handleUnhealthyConnection(PooledConnection connection) throws Exception {
        poolLock.writeLock().lock();
        try {
            availableConnections.remove(connection);
            replaceFailedConnection(connection);
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
