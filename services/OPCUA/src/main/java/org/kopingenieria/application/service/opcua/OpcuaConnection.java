package org.kopingenieria.application.service.opcua;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.api.response.OpcUaConnectionResponse;
import org.kopingenieria.application.monitoring.quality.QualityNetwork;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.exception.*;
import org.kopingenieria.application.validators.OpcUaConnectionValidator;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OpcuaConnection implements Connection {

    private static final int MAX_RETRIES;
    private static final int INITIAL_WAIT;
    private static final double BACKOFF_FACTOR;
    private static final double WAIT_TIME;
    private static final int INITIAL_RETRY;

    private OpcUaClient opcUaClient;
    private final OpcUaConnectionValidator validatorConection;
    private QualityNetwork qualityNetwork;

    static {
        MAX_RETRIES = 3;
        INITIAL_WAIT = 1000;
        BACKOFF_FACTOR = 2.0;
        WAIT_TIME = 5000.0;
        INITIAL_RETRY = 1;
    }

    public OpcuaConnection() throws OpcUaConfigurationException {
        this.opcUaClient = new OpcUaConfiguration().createUserOpcUaClient();
        this.validatorConection = new OpcUaConnectionValidator();
    }

    public CompletableFuture<OpcUaConnectionResponse> connect(UrlType url) throws ConnectionException {
        try {
            return CompletableFuture.supplyAsync(() -> {
                        if (!(validatorConection.validateActiveSession(opcUaClient) &&
                                validatorConection.validateLocalHost(url.getUrl()))) {
                            throw new CompletionException(
                                    new ConnectionException("La sesión OPC UA no está activa o la URL es inválida."));
                        }
                        return true;
                    })
                    .thenCompose(valid -> opcUaClient.connect()
                            .thenApply(connection -> createConnectionResponse(ConnectionStatus.CONNECTED)))
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        if (ex.getCause() instanceof TimeoutException) {
                            throw new CompletionException(
                                    new ConnectionException("Tiempo límite de conexión superado.", ex));
                        }
                        return createConnectionResponse(ConnectionStatus.ERROR);
                    });
        } catch (CompletionException e) {
            throw new ConnectionException("Error en la conexión OPC UA.", e);
        }
    }

    public CompletableFuture<OpcUaConnectionResponse> connect() throws ConnectionException {
        try {
            return CompletableFuture.supplyAsync(() -> {
                        if (!(validatorConection.validateActiveSession(opcUaClient) &&
                                validatorConection.validateLocalHost(opcUaClient.getConfig().getEndpoint().getEndpointUrl()))) {
                            throw new CompletionException(
                                    new ConnectionException("La sesión OPC UA no está activa o la URL es inválida."));
                        }
                        return true;
                    })
                    .thenCompose(valid -> opcUaClient.connect()
                            .thenApply(connection -> createConnectionResponse(ConnectionStatus.CONNECTED)))
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        if (ex.getCause() instanceof TimeoutException) {
                            throw new CompletionException(
                                    new ConnectionException("Tiempo límite de conexión superado.", ex));
                        }
                        return createConnectionResponse(ConnectionStatus.ERROR);
                    });
        } catch (CompletionException e) {
            throw new ConnectionException("Error en la conexión OPC UA.", e);
        }
    }

    public CompletableFuture<OpcUaConnectionResponse> disconnect() throws DisconnectException {
        if (opcUaClient == null) {
            throw new DisconnectException("El cliente OPC UA ya está desconectado o no existe.");
        }
        try {
            return opcUaClient.disconnect()
                    .thenApply(result -> {
                        opcUaClient = null;
                        return createConnectionResponse(ConnectionStatus.DISCONNECTED);
                    })
                    .exceptionally(ex -> {
                        throw new CompletionException(
                                new DisconnectException("Error durante la desconexión del cliente OPC UA.", ex));
                    });
        } catch (CompletionException e) {
            if (e.getCause() instanceof DisconnectException) {
                throw (DisconnectException) e.getCause();
            }
            throw new DisconnectException("Error en la desconexión del cliente OPC UA.", e);
        }
    }

    public CompletableFuture<OpcUaConnectionResponse> backoffreconnection(UrlType url)
            throws OpcUaReconnectionException {
        return attemptBackoffReconnectionWithUrl(url, INITIAL_RETRY, INITIAL_WAIT);
    }

    private CompletableFuture<OpcUaConnectionResponse> attemptBackoffReconnectionWithUrl(
            UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        if (retries >= MAX_RETRIES) {
            return CompletableFuture.completedFuture(
                    createConnectionResponse(ConnectionStatus.RECONNECTION_FAILED));
        }
        try {
            return connect(url).thenCompose(response -> {
                if (response.getStatus() == ConnectionStatus.CONNECTED) {
                    return CompletableFuture.completedFuture(response);
                } else {
                    return CompletableFuture.supplyAsync(() -> null,
                                    CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                            .thenCompose(unused -> {
                                try {
                                    return attemptBackoffReconnectionWithUrl(
                                            url, retries + 1, waitTime * BACKOFF_FACTOR);
                                } catch (OpcUaReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            });
                }
            });
        } catch (ConnectionException | CompletionException e) {
            throw new OpcUaReconnectionException("Error en la reconexión del cliente OPC UA.", e);
        }
    }

    public CompletableFuture<OpcUaConnectionResponse> backoffreconnection()
            throws OpcUaReconnectionException {
        return attemptBackoffReconnectionWithoutUrl(
                UrlType.valueOf(opcUaClient.getConfig().getEndpoint().getEndpointUrl()),
                INITIAL_RETRY, INITIAL_WAIT);
    }

    private CompletableFuture<OpcUaConnectionResponse> attemptBackoffReconnectionWithoutUrl(
            UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        return attemptBackoffReconnectionWithUrl(url, retries, waitTime);
    }

    public CompletableFuture<OpcUaConnectionResponse> linearreconnection(UrlType url)
            throws OpcUaReconnectionException {
        return attemptLinearReconnectionWithUrl(url, INITIAL_RETRY, WAIT_TIME);
    }

    private CompletableFuture<OpcUaConnectionResponse> attemptLinearReconnectionWithUrl(
            UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        if (retries <= 0) {
            return CompletableFuture.completedFuture(
                    createConnectionResponse(ConnectionStatus.RECONNECTION_FAILED));
        }
        try {
            return connect(url).thenCompose(response -> {
                if (response.getStatus() == ConnectionStatus.CONNECTED) {
                    return CompletableFuture.completedFuture(response);
                }
                return CompletableFuture.supplyAsync(() -> null,
                                CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                        .thenCompose(unused -> {
                            try {
                                return attemptLinearReconnectionWithUrl(url, retries - 1, waitTime);
                            } catch (OpcUaReconnectionException e) {
                                throw new CompletionException(e);
                            }
                        });
            });
        } catch (ConnectionException | CompletionException e) {
            throw new OpcUaReconnectionException("Error en la reconexión del cliente OPC UA.", e);
        }
    }

    public CompletableFuture<OpcUaConnectionResponse> linearreconnection()
            throws OpcUaReconnectionException {
        return attemptLinearReconnectionWithoutUrl(
                UrlType.valueOf(opcUaClient.getConfig().getEndpoint().getEndpointUrl()),
                INITIAL_RETRY, WAIT_TIME);
    }

    private CompletableFuture<OpcUaConnectionResponse> attemptLinearReconnectionWithoutUrl(
            UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        return attemptLinearReconnectionWithUrl(url, retries, waitTime);
    }

    public CompletableFuture<OpcUaConnectionResponse> ping() throws OpcUaPingException {
        if (opcUaClient == null) {
            throw new OpcUaPingException("Cliente OPC UA desconectado");
        }

        NodeId pingNodeId = NodeId.parse("ns=0;i=2259");
        return opcUaClient.readValue(0, TimestampsToReturn.Both, pingNodeId)
                .thenApply(value -> {
                    if (value != null && value.getValue() != null) {
                        return createConnectionResponse(ConnectionStatus.CONNECTED);
                    }
                    return createConnectionResponse(ConnectionStatus.NO_RESPONSE);
                })
                .exceptionally(ex -> createConnectionResponse(ConnectionStatus.ERROR));
    }

    private OpcUaConnectionResponse createConnectionResponse(ConnectionStatus status) {
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
                .quality(qualityNetwork.getQualityLevel())
                .lastActivity(LocalDateTime.now())
                .build();
    }

    public OpcUaClient opcUaClient() {
        return opcUaClient;
    }

    public OpcuaConnection setOpcUaClient(OpcUaClient opcUaClient) {
        this.opcUaClient = opcUaClient;
        return this;
    }

    public UrlType targeturl() {
        if (opcUaClient == null) {
            return null;
        }
        return UrlType.valueOf(opcUaClient.getConfig().getEndpoint().getEndpointUrl());
    }

    public OpcuaConnection castClass(Object object) {
        if (object != null) {
            return (OpcuaConnection) object;
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        if (opcUaClient != null) {
            try {
                opcUaClient.disconnect().get(10, TimeUnit.SECONDS);
            } catch (TimeoutException e) {
                throw new Exception("Timeout al intentar desconectar el cliente OPC UA.", e);
            } catch (Exception e) {
                throw new Exception("Error durante la desconexión del cliente OPC UA.", e);
            } finally {
                opcUaClient = null;
            }
        }
    }
}
