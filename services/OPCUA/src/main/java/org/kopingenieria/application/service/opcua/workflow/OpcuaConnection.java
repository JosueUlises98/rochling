package org.kopingenieria.application.service.opcua.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.api.response.OpcUaConnectionResponse;
import org.kopingenieria.application.service.files.UserConfigFile;
import org.kopingenieria.application.service.opcua.pool.client.user.OpcUaUserPool;
import org.kopingenieria.application.service.opcua.pool.client.user.OpcUaUserPoolManager;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.application.validators.user.UserConnectionValidator;
import org.kopingenieria.exception.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

public class OpcuaConnection implements Connection {

    private static final int MAX_RETRIES = 3;
    private static final int INITIAL_WAIT = 1000;
    private static final double BACKOFF_FACTOR = 2.0;
    private static final int LINEAR_RETRY_INTERVAL = 5000;
    private static final int CONNECTION_TIMEOUT = 10000;

    @Autowired
    private OpcUaUserPoolManager poolManager;
    private OpcUaUserPool.PooledOpcUaClient pooledClient;
    private final UserConnectionValidator validatorConnection;
    private UrlType lastConnectedUrl;
    @Getter
    private volatile ConnectionStatus currentStatus;
    @Getter
    private LocalDateTime lastActivityTime;
    private final UserConfiguration loadConfiguration;

    public OpcuaConnection() throws ConfigurationException {
        this.validatorConnection = new UserConnectionValidator();
        this.currentStatus = ConnectionStatus.UNKNOWN;
        this.lastActivityTime = LocalDateTime.now();
        UserConfigFile configFile = new UserConfigFile(new ObjectMapper(), new Properties(), new UserConfiguration());
        this.loadConfiguration = configFile.loadConfiguration(configFile.extractExistingFilename());
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> connect() throws Exception {
        if (lastConnectedUrl == null) {
            throw new ConnectionException("No hay URL disponible para la conexión");
        }
        return connect(lastConnectedUrl);
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> connect(UrlType url) throws Exception {
        validateUrl(url);
        lastConnectedUrl = url;
        updateConnectionStatus(ConnectionStatus.CONNECTING);
        updateLastActivity();
        try {
            Optional<OpcUaUserPool.PooledOpcUaClient> optionalClient =
                    poolManager.obtenerCliente(loadConfiguration);

            if (optionalClient.isEmpty()) {
                throw new ConnectionException("No se pudo obtener un cliente del pool");
            }

            pooledClient = optionalClient.get();
            OpcUaClient opcUaClient = pooledClient.getClient();

            return CompletableFuture.supplyAsync(() -> {
                        if (!validateConnection(url, opcUaClient)) {
                            throw new CompletionException(
                                    new ConnectionException("Validación de conexión fallida"));
                        }
                        return true;
                    })
                    .thenCompose(valid -> connectClient(opcUaClient))
                    .orTimeout(CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS)
                    .exceptionally(this::handleConnectionException);
        } catch (Exception e) {
            throw new ConnectionException("Error en la conexión OPC UA", e);
        }
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> disconnect() throws Exception {
        if (pooledClient == null) {
            return CompletableFuture.completedFuture(
                    createConnectionResponse(ConnectionStatus.DISCONNECTED));
        }

        return pooledClient.getClient().disconnect()
                .thenApply(result -> {
                    cleanup();
                    return createConnectionResponse(ConnectionStatus.DISCONNECTED);
                })
                .exceptionally(ex -> {
                    cleanup();
                    throw new CompletionException(
                            new DisconnectException("Error durante la desconexión", ex));
                });
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> backoffreconnection() throws Exception {
        if (lastConnectedUrl == null) {
            throw new OpcUaReconnectionException("No hay URL previa disponible para la reconexión");
        }
        return backoffreconnection(lastConnectedUrl);
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> backoffreconnection(UrlType url)
            throws Exception {
        validateUrl(url);
        cleanup();
        return attemptBackoffReconnection(url, 1, INITIAL_WAIT);
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> linearreconnection() throws Exception {
        if (lastConnectedUrl == null) {
            throw new OpcUaReconnectionException("No hay URL previa disponible para la reconexión");
        }
        return linearreconnection(lastConnectedUrl);
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> linearreconnection(UrlType url)
            throws Exception {
        validateUrl(url);
        cleanup();
        return attemptLinearReconnection(url, 1);
    }

    @Override
    public CompletableFuture<OpcUaConnectionResponse> ping() throws Exception {
        if (pooledClient == null || !pooledClient.isConnected()) {
            throw new OpcUaPingException("Cliente OPC UA no conectado");
        }

        NodeId pingNodeId = NodeId.parse("ns=0;i=2259");
        return pooledClient.getClient().readValue(0, TimestampsToReturn.Both, pingNodeId)
                .thenApply(this::handlePingResponse)
                .exceptionally(ex -> createConnectionResponse(ConnectionStatus.ERROR));
    }

    @Override
    public void close() throws Exception {
        cleanup();
    }

    private CompletableFuture<OpcUaConnectionResponse> attemptBackoffReconnection(
            UrlType url, int retryCount, double waitTime) {
        if (retryCount > MAX_RETRIES) {
            updateConnectionStatus(ConnectionStatus.RECONNECTION_FAILED);
            return CompletableFuture.completedFuture(
                    createConnectionResponse(ConnectionStatus.RECONNECTION_FAILED));
        }

        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return connect(url).thenCompose(response -> {
                            if (response.getStatus() == ConnectionStatus.CONNECTED) {
                                return CompletableFuture.completedFuture(response);
                            }
                            return attemptBackoffReconnection(
                                    url, retryCount + 1, waitTime * BACKOFF_FACTOR);
                        });
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                .thenCompose(Function.identity());
    }

    private CompletableFuture<OpcUaConnectionResponse> attemptLinearReconnection(
            UrlType url, int retryCount) {
        if (retryCount > MAX_RETRIES) {
            updateConnectionStatus(ConnectionStatus.RECONNECTION_FAILED);
            return CompletableFuture.completedFuture(
                    createConnectionResponse(ConnectionStatus.RECONNECTION_FAILED));
        }
        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return connect(url).thenCompose(response -> {
                            if (response.getStatus() == ConnectionStatus.CONNECTED) {
                                return CompletableFuture.completedFuture(response);
                            }
                            return attemptLinearReconnection(url, retryCount + 1);
                        });
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, CompletableFuture.delayedExecutor(LINEAR_RETRY_INTERVAL, TimeUnit.MILLISECONDS))
                .thenCompose(Function.identity());
    }

    private boolean validateConnection(UrlType url, OpcUaClient client) {
        return validatorConnection.validateActiveSession(client) &&
                validatorConnection.validateLocalHost(url.getUrl());
    }

    private CompletableFuture<OpcUaConnectionResponse> connectClient(OpcUaClient client) {
        return client.connect()
                .thenApply(connection -> {
                    updateConnectionStatus(ConnectionStatus.CONNECTED);
                    return createConnectionResponse(ConnectionStatus.CONNECTED, connection);
                });
    }

    private OpcUaConnectionResponse handleConnectionException(Throwable ex) {
        cleanup();

        if (ex instanceof TimeoutException) {
            updateConnectionStatus(ConnectionStatus.FAILED);
            throw new CompletionException(
                    new ConnectionException("Tiempo límite de conexión superado", ex));
        }

        updateConnectionStatus(ConnectionStatus.ERROR);
        return createConnectionResponse(ConnectionStatus.ERROR);
    }

    private OpcUaConnectionResponse handlePingResponse(DataValue value) {
        updateLastActivity();
        if (value != null && value.getValue() != null) {
            updateConnectionStatus(ConnectionStatus.CONNECTED);
            return createConnectionResponse(ConnectionStatus.CONNECTED);
        }
        updateConnectionStatus(ConnectionStatus.NO_RESPONSE);
        return createConnectionResponse(ConnectionStatus.NO_RESPONSE);
    }

    private void validateUrl(UrlType url) throws ConnectionException {
        if (url == null || url.getUrl() == null || url.getUrl().trim().isEmpty()) {
            throw new ConnectionException("La URL no puede ser nula o vacía");
        }
    }

    private OpcUaConnectionResponse createConnectionResponse(ConnectionStatus status) {
        return createConnectionResponse(status, null);
    }

    private OpcUaConnectionResponse createConnectionResponse(ConnectionStatus status, UaClient client) {
        updateLastActivity();

        OpcUaClient opcUaClient = pooledClient != null ? pooledClient.getClient() : null;
        return OpcUaConnectionResponse.builder()
                .endpointUrl(opcUaClient != null ?
                        opcUaClient.getConfig().getEndpoint().getEndpointUrl() : null)
                .applicationName(opcUaClient != null ?
                        opcUaClient.getConfig().getApplicationName().getText() : null)
                .applicationUri(opcUaClient != null ?
                        opcUaClient.getConfig().getApplicationUri() : null)
                .productUri(opcUaClient != null ?
                        opcUaClient.getConfig().getProductUri() : null)
                .status(status)
                .lastActivity(lastActivityTime)
                .client(client)
                .build();
    }

    private void updateConnectionStatus(ConnectionStatus status) {
        this.currentStatus = status;
        updateLastActivity();
    }

    private void updateLastActivity() {
        this.lastActivityTime = LocalDateTime.now();
    }

    private void cleanup() {
        if (pooledClient != null) {
            poolManager.liberarCliente(pooledClient);
            pooledClient = null;
        }
        updateConnectionStatus(ConnectionStatus.DISCONNECTED);
    }
}
