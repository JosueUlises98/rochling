package org.kopingenieria.application.service.opcua;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.config.OpcUaConfiguration;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.exception.*;
import org.kopingenieria.util.ConfigurationLoader;
import org.kopingenieria.validators.OpcUaConnectionValidator;

import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OpcuaConnection implements Connection {

    private static final int MAX_RETRIES;

    private static final int INITIAL_WAIT; // Milisegundos

    private static final double BACKOFF_FACTOR;

    private static final double WAIT_TIME;

    private static final int INITIAL_RETRY;

    private OpcUaClient opcUaClient;

    private final OpcUaConnectionValidator validatorConection;

    static {

            MAX_RETRIES = 3;
            INITIAL_WAIT = 1000;
            BACKOFF_FACTOR = 2.0;
            WAIT_TIME = 5000.0;
            INITIAL_RETRY = 1;
    }

    public OpcuaConnection() throws OpcUaConfigurationException {
        this.opcUaClient = new org.kopingenieria.application.service.opcua.OpcUaConfiguration().createUserOpcUaClient();
        this.validatorConection = new OpcUaConnectionValidator();
    }

    public CompletableFuture<Boolean> connect(UrlType url) throws ConnectionException {
        try {
            // Usamos CompletableFuture y desempaquetamos posibles excepciones
            return CompletableFuture.supplyAsync(() -> {
                        // Validar que la sesión está activa y que la URL es válida
                        if (!(validatorConection.validateActiveSession(opcUaClient) && validatorConection.validateLocalHost(url.getUrl()))) {
                            throw new CompletionException(new ConnectionException("La sesión OPC UA no está activa o la URL es inválida."));
                        }
                        return true;
                    })
                    .thenCompose(valid -> {
                        // Intentar conectar al cliente OPC UA
                        return opcUaClient.connect()
                                .thenApply(connection -> true);
                    })
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        // Manejar excepciones y convertirlas en `ConnectionException`
                        if (ex.getCause() instanceof TimeoutException) {
                            throw new CompletionException(new ConnectionException("Tiempo límite de conexión superado al servidor OPC UA.", ex));
                        } else if (ex.getCause() instanceof ConnectionException) {
                            throw new CompletionException(ex.getCause());
                        } else {
                            throw new CompletionException(new ConnectionException("Error desconocido al conectar al servidor OPC UA.", ex));
                        }
                    });
        } catch (CompletionException e) {
            // Desempaqueta y propaga ConnectionException
            if (e.getCause() instanceof ConnectionException) {
                throw (ConnectionException) e.getCause();
            }
            throw new ConnectionException("Error inesperado en la conexión OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> connect() throws ConnectionException {
        try {
            // Usamos CompletableFuture y desempaquetamos posibles excepciones
            return CompletableFuture.supplyAsync(() -> {
                        // Validar que la sesión está activa y que la URL es válida
                        if (!(validatorConection.validateActiveSession(opcUaClient) && validatorConection.validateLocalHost(opcUaClient.getConfig().getEndpoint().getEndpointUrl()))) {
                            throw new CompletionException(new ConnectionException("La sesión OPC UA no está activa o la URL es inválida."));
                        }
                        return true;
                    })
                    .thenCompose(valid -> {
                        // Intentar conectar al cliente OPC UA
                        return opcUaClient.connect()
                                .thenApply(connection -> true);
                    })
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        // Manejar excepciones y convertirlas en `ConnectionException`
                        if (ex.getCause() instanceof TimeoutException) {
                            throw new CompletionException(new ConnectionException("Tiempo límite de conexión superado al servidor OPC UA.", ex));
                        } else if (ex.getCause() instanceof ConnectionException) {
                            throw new CompletionException(ex.getCause());
                        } else {
                            throw new CompletionException(new ConnectionException("Error desconocido al conectar al servidor OPC UA.", ex));
                        }
                    });
        } catch (CompletionException e) {
            // Desempaqueta y propaga ConnectionException
            if (e.getCause() instanceof ConnectionException) {
                throw (ConnectionException) e.getCause();
            }
            throw new ConnectionException("Error inesperado en la conexión OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> disconnect() throws DisconnectException {
        if (opcUaClient == null) {
            throw new DisconnectException("El cliente OPC UA ya está desconectado o no existe."); // Lanza la excepción de desconexión
        }
        try {
            // Procesar la desconexión de forma completamente asíncrona
            return opcUaClient.disconnect() // Llamada asíncrona para iniciar la desconexión
                    .thenApply(result -> {
                        opcUaClient = null; // Libera el cliente tras la desconexión
                        return true;
                    })
                    .exceptionally(ex -> { // Manejar excepciones y arrojar una DisconnectException
                        throw new CompletionException(new DisconnectException("Error durante la desconexión del cliente OPC UA.", ex));
                    });
        } catch (CompletionException e) {
            // Desempaqueta y propaga DisconnectException
            if (e.getCause() instanceof DisconnectException) {
                throw (DisconnectException) e.getCause();
            }
            throw new DisconnectException("Error en la desconexion de un cliente OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> backoffreconnection(UrlType url) throws OpcUaReconnectionException {
        return attemptBackoffReconnectionWithUrl(url, INITIAL_RETRY, INITIAL_WAIT);
    }

    private CompletableFuture<Boolean> attemptBackoffReconnectionWithUrl(UrlType url, int initialretry, double waittime) throws OpcUaReconnectionException {
        if (initialretry >= MAX_RETRIES) {
            return CompletableFuture.completedFuture(false); // Devuelve un futuro fallido después del límite máximo de intentos
        }
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    return CompletableFuture.completedFuture(true); // Reconexión exitosa
                } else {
                    return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waittime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                            .thenCompose(unused -> {
                                try {
                                    return attemptBackoffReconnectionWithUrl(url, initialretry + 1, waittime * BACKOFF_FACTOR);
                                } catch (OpcUaReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            }); // Incrementar el tiempo de espera y reintentar
                }
            });
        } catch (ConnectionException | CompletionException e) {
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof OpcUaReconnectionException) {
                throw (OpcUaReconnectionException) e.getCause();
            }
            throw new OpcUaReconnectionException("Error en la reconexion de un cliente OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> backoffreconnection() throws OpcUaReconnectionException {
        return attemptBackoffReconnectionWithoutUrl(UrlType.valueOf(opcUaClient.getConfig().getEndpoint().getEndpointUrl()), INITIAL_RETRY, INITIAL_WAIT);
    }

    private CompletableFuture<Boolean> attemptBackoffReconnectionWithoutUrl(UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        if (retries >= MAX_RETRIES) {
            return CompletableFuture.completedFuture(false); // Devuelve un futuro fallido después del límite máximo de intentos
        }
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    return CompletableFuture.completedFuture(true); // Reconexión exitosa
                } else {
                    return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                            .thenCompose(unused -> {
                                try {
                                    return attemptBackoffReconnectionWithUrl(url, retries + 1, waitTime * BACKOFF_FACTOR);
                                } catch (OpcUaReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            }); // Incrementar el tiempo de espera y reintentar
                }
            });
        } catch (ConnectionException | CompletionException e) {
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof OpcUaReconnectionException) {
                throw (OpcUaReconnectionException) e.getCause();
            }
            throw new OpcUaReconnectionException("Error en la reconexion de un cliente OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> linearreconnection(UrlType url) throws OpcUaReconnectionException {
        return attemptlinearReconnectionWithUrl(url, INITIAL_RETRY, WAIT_TIME);
    }

    private CompletableFuture<Boolean> attemptlinearReconnectionWithUrl(UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        final int[] retry = {retries};
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    // Reconexión exitosa
                    return CompletableFuture.completedFuture(true);
                } else {
                    retry[0]--; // Reducir el número de reintentos restantes
                    if (retry[0] <= 0) {
                        // Si no quedan más reintentos, lanzar la excepción
                        try {
                            throw new OpcUaReconnectionException("Maximo de intentos excedidos.");
                        } catch (OpcUaReconnectionException e) {
                            throw new CompletionException(e);
                        }
                    }
                    // Si aún quedan intentos, esperar y volver a intentarlo
                    return CompletableFuture.supplyAsync(() -> null,
                                    CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                            .thenCompose(unused -> {
                                try {
                                    return attemptlinearReconnectionWithUrl(url, retry[0], waitTime);
                                } catch (OpcUaReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            });
                }
            }).exceptionally(ex -> {
                // Manejamos cualquier excepción no controlada previamente
                try {
                    throw new OpcUaReconnectionException("Error inesperado durante el proceso de reconexión", ex);
                } catch (OpcUaReconnectionException e) {
                    throw new CompletionException(e);
                }
            });
        } catch (CompletionException | ConnectionException e) {
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof OpcUaReconnectionException) {
                throw (OpcUaReconnectionException) e.getCause();
            }
            throw new OpcUaReconnectionException("Error en la reconexion de un cliente OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> linearreconnection() throws OpcUaReconnectionException {
        return attemptlinearReconnectionWithoutUrl(UrlType.valueOf(opcUaClient.getConfig().getEndpoint().getEndpointUrl()), INITIAL_RETRY, WAIT_TIME);
    }

    private CompletableFuture<Boolean> attemptlinearReconnectionWithoutUrl(UrlType url, int retries, double waitTime) throws OpcUaReconnectionException {
        final int[] retry = {retries};
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    // Reconexión exitosa
                    return CompletableFuture.completedFuture(true);
                } else {
                    retry[0]--; // Reducir el número de reintentos restantes
                    if (retry[0] <= 0) {
                        // Si no quedan más reintentos, lanzar la excepción
                        try {
                            throw new OpcUaReconnectionException("Maximo de intentos excedidos.");
                        } catch (OpcUaReconnectionException e) {
                            throw new CompletionException(e);
                        }
                    }
                    // Si aún quedan intentos, esperar y volver a intentarlo
                    return CompletableFuture.supplyAsync(() -> null,
                                    CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                            .thenCompose(unused -> {
                                try {
                                    return attemptlinearReconnectionWithUrl(url, retry[0], waitTime);
                                } catch (OpcUaReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            });
                }
            }).exceptionally(ex -> {
                // Manejamos cualquier excepción no controlada previamente
                try {
                    throw new OpcUaReconnectionException("Error inesperado durante el proceso de reconexión", ex);
                } catch (OpcUaReconnectionException e) {
                    throw new CompletionException(e);
                }
            });
        } catch (CompletionException | ConnectionException e) {
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof OpcUaReconnectionException) {
                throw (OpcUaReconnectionException) e.getCause();
            }
            throw new OpcUaReconnectionException("Error en la reconexion de un cliente OPC UA.", e);
        }
    }

    public CompletableFuture<Boolean> ping() throws OpcUaPingException {
        if (opcUaClient == null) {
            // Fallo inmediato si el cliente no está conectado.
            throw new OpcUaPingException("Cliente OPC UA desconectado");
        }
        // Nodo estándar en OPC UA para verificar el estado del servidor
        NodeId pingNodeId = NodeId.parse("ns=0;i=2259");
        // Realiza la lectura del valor del nodo en el servidor de forma asíncrona
        CompletableFuture<Boolean> pingFuture = new CompletableFuture<>();
        opcUaClient.readValue(0, TimestampsToReturn.Both, pingNodeId)
                .thenAccept(value -> {
                    if (value != null && value.getValue() != null) {
                        // El valor devuelto es válido, el ping es exitoso
                        pingFuture.complete(true); // Completar el futuro con éxito
                    } else {
                        // Si el valor devuelto es nulo, lanzar directamente una excepción personalizada
                        pingFuture.complete(false);
                        try {
                            throw new OpcUaPingException("PingException: Respuesta enviada nula");
                        } catch (OpcUaPingException e) {
                            throw new CompletionException(e);
                        }
                    }
                }).exceptionally(ex -> {
                    // Manejo de cualquier error que ocurra durante el proceso de ping
                    String errorMessage = "Error durante el ping al servidor OPC UA.";
                    pingFuture.completeExceptionally(new OpcUaPingException(errorMessage, ex));
                    return null;
                });
        return pingFuture;
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
                opcUaClient.disconnect()
                        .get(10, TimeUnit.SECONDS); // Espera hasta 10 segundos para desconectarse de forma limpia
            } catch (TimeoutException e) {
                throw new Exception("Timeout al intentar desconectar el cliente OPC UA.", e);
            } catch (Exception e) {
                throw new Exception("Error durante la desconexión del cliente OPC UA.", e);
            } finally {
                opcUaClient = null; // Asegura que el recurso sea liberado.
            }
        }
    }
}
