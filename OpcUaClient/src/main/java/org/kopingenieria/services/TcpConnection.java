package org.kopingenieria.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.exceptions.DisconnectException;
import org.kopingenieria.exceptions.PingException;
import org.kopingenieria.exceptions.ReconnectionException;
import org.kopingenieria.model.TCPConnection;
import org.kopingenieria.model.Url;
import org.kopingenieria.tools.ConfigurationLoader;
import org.kopingenieria.validators.ValidatorConexion;

import java.util.Properties;
import java.util.concurrent.*;

public class TcpConnection extends ConnectionService {
    /**
     * Logger instance used for logging informational, warning, and error messages
     * related to the operations of the PlcConnect class.
     * <p>
     * This logger is configured to track critical events such as connection requests,
     * connection status updates, error handling, and other operational details within the class.
     * It facilitates debugging and monitoring by providing a detailed audit trail of
     * activities and errors encountered during runtime.
     */
    private static final Logger logger = LogManager.getLogger(TcpConnection.class);
    /**
     * The MAX_RETRIES constant represents the maximum number of retry attempts
     * allowed when trying to establish or re-establish a connection with a
     * Programmable Logic Controller (PLC) or other external systems.
     * <p>
     * This constant is used to control the number of reconnection attempts before
     * considering the operation as failed, ensuring that the connection handling
     * logic adheres to a defined retry policy.
     * <p>
     * Value: 10
     */
    private static final int MAX_RETRIES;
    /**
     * The INITIAL_WAIT constant specifies the initial waiting time in milliseconds
     * before performing retry attempts when establishing or re-establishing a connection in the PlcConnect class.
     * <p>
     * This value is used as the base delay for implementing connection retry logic.
     * It helps in spacing out retry attempts to avoid overwhelming the system or remote server.
     * The determination of this value depends on the typical responsiveness of the system being connected to.
     */
    private static final int INITIAL_WAIT; // Milisegundos
    /**
     * The BACKOFF_FACTOR is a constant multiplier used to increase the wait time between
     * connection retry attempts in case of failures when establishing or re-establishing
     * connections to a PLC or other external systems.
     * <p>
     * This factor implements an exponential backoff strategy where the delay between
     * retry attempts grows exponentially by multiplying the initial wait time by this
     * factor after each failure.
     * <p>
     * For example, with a BACKOFF_FACTOR of 2.0, the wait time for retries would double
     * after each unsuccessful connection attempt.
     * <p>
     * It helps mitigate potential system overloads or throttling issues caused by frequent
     * retry attempts and improves system stability and reliability.
     */
    private static final double BACKOFF_FACTOR;
    /**
     * Represents the predetermined wait time in seconds between connection attempts,
     * especially used in backoff or linear reconnection strategies.
     * <p>
     * This value is utilized to control the delay before retrying a connection attempt
     * in scenarios where previous attempts have failed. It assists in maintaining a
     * controlled load on the server and avoiding excessive immediate retries.
     * <p>
     * The {@code WAIT_TIME} is defined as a constant to ensure uniformity of usage
     * and prevent accidental alteration during runtime.
     */
    private static final double WAIT_TIME;
    /**
     * Represents the initial number of retry attempts for reconnection in the {@code TcpConnection} class.
     * This constant defines the starting point for retry mechanisms when attempting
     * to re-establish a connection, particularly in backoff or linear reconnection strategies.
     *
     * The value of {@code INITIAL_RETRY} serves as a fundamental configuration to control the behavior
     * of the retry logic and provides a baseline for subsequent retries.
     */
    private static final int INITIAL_RETRY;
    /**
     * Represents the instance of an OPC UA client used for communicating with OPC UA servers.
     *
     * This variable is responsible for managing the connection to the server, facilitating
     * operations such as data exchange, server browsing, and monitoring. It acts as the primary
     * interface for handling OPC UA communication in the {@code ConexionClienteService} class.
     *
     * The {@code uaclient} is initialized and maintained internally within the service, ensuring
     * that all interactions with the OPC UA server are encapsulated and managed in a synchronized
     * and thread-safe manner.
     */
    private OpcUaClient opcUaClient;
    /**
     * Represents an instance of {@link ValidatorConexion} used to perform validation operations related
     * to OPC UA client configurations and server connectivity within the {@code ConexionClienteService}.
     *
     * This field acts as a utility for ensuring the integrity and preconditions of the operations
     * performed, covering aspects such as validating server endpoints, checking client availability,
     * and facilitating preparatory validation prior to establishing a connection.
     */
    private ValidatorConexion validatorConection;
    /**
     * Represents the server endpoint URL for establishing TCP connections.
     *
     * This field is used to initialize, manage, and maintain communication
     * with a predefined OPC-UA server. The {@code url} is of type {@link Url},
     * which defines strongly typed references to specific server addresses.
     *
     * Typically configured during the initialization of the {@code TcpConnection} class
     * and may be used in connection management tasks such as establishing, reconnecting,
     * or verifying server connections.
     */
    private Url targeturl;

    static {
        Properties properties = ConfigurationLoader.loadProperties("reconnection.properties");
        INITIAL_RETRY = Integer.parseInt(properties.getProperty("initial_retry", "0"));
        MAX_RETRIES = Integer.parseInt(properties.getProperty("max_retries", "10"));
        INITIAL_WAIT = Integer.parseInt(properties.getProperty("initial_wait", "1000"));
        BACKOFF_FACTOR = Double.parseDouble(properties.getProperty("backoff_factor", "2.0"));
        WAIT_TIME = Double.parseDouble(properties.getProperty("wait_time", "3000"));
    }
    /**
     * Private constructor for the {@code ConexionClienteService} class.
     * <p>
     * This constructor initializes the server endpoint URL to a predefined value.
     * It restricts direct object creation and is utilized internally for controlled initialization.
     *
     * @param url the {@link Url} object providing the endpoint address to initialize the connection service.
     */
    public TcpConnection(OpcUaClient opcUaClient) {
        super();
        this.opcUaClient=opcUaClient;
        this.validatorConection=new ValidatorConexion();
    }
    /**
     * Default private constructor for the {@code ConexionClienteService} class.
     * <p>
     * This constructor prevents the instantiation of the {@code ConexionClienteService} class directly.
     * It is used internally to enforce the singleton pattern and ensure controlled access
     * to the instance of the class. External instantiation is not allowed to maintain
     * the integrity and centralized management of connections.
     */
    public TcpConnection() {
        super();
        this.validatorConection=new ValidatorConexion();
    }

    public CompletableFuture<Boolean> connect(Url url)throws ConnectionException {
        logger.info("Intentando conectar al servidor OPC UA en URL: {}", url.getUrl());
        // Validar en una cadena de ejecución asíncrona
        return CompletableFuture.supplyAsync(() -> {
                    if (!(validatorConection.sesionActiva(opcUaClient) && validatorConection.validateLocalHost(url.getUrl()))) {
                        try {
                            throw new ConnectionException("Error en la conexion");
                        } catch (ConnectionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                }).thenCompose(valid -> {
                    // Intentar conectar al cliente OPC UA
                    return opcUaClient.connect()
                            .thenApply(connection -> {
                                logger.info("Conectado exitosamente al servidor OPC UA en URL: {}", url.getUrl());
                                return true;
                            });
                }).orTimeout(10, TimeUnit.SECONDS) // Establece un timeout de 10 segundos
                .exceptionally(ex -> {
                    // Manejo de excepciones y del timeout
                    if (ex instanceof TimeoutException) {
                        logger.error("Tiempo limite de conexion superado al servidor OPC UA");
                    } else {
                        logger.error("Error al conectar al servidor OPC UA en URL: {}", url.getUrl(), ex);
                    }
                    throw new CompletionException(ex);
                });
    }

    public CompletableFuture<Boolean> connect()throws ConnectionException{
        logger.info("Intentando conectar al servidor OPC UA con default URL: {}", targeturl.getUrl());
        // Validar en una cadena de ejecución asíncrona
        return CompletableFuture.supplyAsync(() -> {
                    if (!(validatorConection.sesionActiva(opcUaClient) && validatorConection.validateLocalHost(targeturl.getUrl()))) {
                        try {
                            throw new ConnectionException("Error en la conexion");
                        } catch (ConnectionException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    return true;
                }).thenCompose(valid -> {
                    // Intentar conectar al cliente OPC UA
                    return opcUaClient.connect()
                            .thenApply(connection -> {
                                logger.info("Conectado exitosamente al servidor OPC UA con default URL: {}",targeturl.getUrl());
                                return true;
                            });
                }).orTimeout(10, TimeUnit.SECONDS) // Establece un timeout de 10 segundos
                .exceptionally(ex -> {
                    // Manejo de excepciones y del timeout
                    if (ex instanceof TimeoutException) {
                        logger.error("La conexión al servidor OPC UA superó el tiempo límite");
                    } else {
                        logger.error("Error al conectar al servidor OPC UA en URL: {}",targeturl.getUrl(), ex);
                    }
                    throw new CompletionException(ex);
                });
    }

    /**
     * Attempts to disconnect the OPC UA client from the server in a thread-safe manner.
     * <p>
     * If the client is currently connected to the server, it will disconnect and clean up resources.
     * If the client is already disconnected or not initialized, a warning will be logged.
     * In case of errors during the disconnection process, an appropriate error message will be logged.
     *
     * @return true if the disconnection operation is successful, false otherwise.
     */
    public CompletableFuture<Boolean> disconnect() throws DisconnectException {
        if (opcUaClient == null) {
            logger.warn("El cliente OPC UA ya está desconectado o no existe.");
            throw new DisconnectException("El cliente OPC UA ya está desconectado o no existe."); // Lanza la excepción de desconexión
        }
        // Procesar la desconexión de forma completamente asíncrona
        return opcUaClient.disconnect() // Llamada asíncrona para iniciar la desconexión
                .thenApply(result -> {
                    logger.info("Desconectado exitosamente del servidor OPC UA.");
                    opcUaClient = null; // Libera el cliente tras la desconexión
                    return true;
                })
                .exceptionally(ex -> { // Manejar excepciones y arrojar una DisconnectException
                    logger.error("Error al desconectar el cliente OPC UA: {}", ex.getMessage(), ex);
                    throw new CompletionException(new DisconnectException("Error durante la desconexión del cliente OPC UA.", ex));
                });
    }
    /**
     * Attempts to re-establish a connection to the specified server URL.
     * This method performs a retry mechanism with incremental backoff
     * until the maximum number of retries is reached or the connection is successful.
     *
     * @param url The server URL to which the reconnection attempt will be made.
     *            The URL is represented using the {@code Url} enum.
     * @return {@code true} if the reconnection attempt is successful,
     * {@code false} if all retry attempts fail.
     */

    public CompletableFuture<Boolean> backoffreconnection(Url url) throws ConnectionException, ReconnectionException {
        return attemptBackoffReconnectionWithUrl(url,INITIAL_RETRY,INITIAL_WAIT);
    }

    private CompletableFuture<Boolean> attemptBackoffReconnectionWithUrl(Url url,int initialretry,double waittime) throws ConnectionException, ReconnectionException {
        if (initialretry >= MAX_RETRIES) {
            logger.error("No se pudo reconectar al servidor OPC UA después de {} intentos.",initialretry);
            return CompletableFuture.completedFuture(false); // Devuelve un futuro fallido después del límite máximo de intentos
        }
        return connect(url).thenCompose(success -> {
            if (success) {
                logger.info("Reconexión exitosa en el intento #{}", initialretry + 1);
                return CompletableFuture.completedFuture(true); // Reconexión exitosa
            } else {
                logger.info("Intento fallido de reconexión #{}. Esperando {} ms antes de reintentar...",initialretry + 1,waittime);
                return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waittime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                        .thenCompose(unused -> {
                            try {
                                return attemptBackoffReconnectionWithUrl(url, initialretry + 1, waittime * BACKOFF_FACTOR);
                            } catch (ConnectionException | ReconnectionException e) {
                                try {
                                    throw new ReconnectionException("Error de reconexion exponencial",e.getCause());
                                } catch (ReconnectionException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }); // Incrementar el tiempo de espera y reintentar
            }
        });
    }

    public CompletableFuture<Boolean> backoffreconnection() throws ConnectionException, ReconnectionException {
        return attemptBackoffReconnectionWithoutUrl(targeturl,INITIAL_RETRY,INITIAL_WAIT);
    }

    private CompletableFuture<Boolean> attemptBackoffReconnectionWithoutUrl(Url url, int retries, double waitTime) throws ConnectionException, ReconnectionException {
        if (retries >= MAX_RETRIES) {
            logger.error("Numero de intentos excedidos {} intentos.", retries);
            return CompletableFuture.completedFuture(false); // Devuelve un futuro fallido después del límite máximo de intentos
        }
        return connect(url).thenCompose(success -> {
            if (success) {
                logger.info("Reconexión en el intento #{}", retries + 1);
                return CompletableFuture.completedFuture(true); // Reconexión exitosa
            } else {
                logger.info("Intento de reconexion fallido #{}. Esperando {} ms antes de reintentar...", retries + 1, waitTime);
                return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                        .thenCompose(unused -> {
                            try {
                                return attemptBackoffReconnectionWithoutUrl(url, retries + 1, waitTime * BACKOFF_FACTOR);
                            } catch (ConnectionException | ReconnectionException e) {
                                logger.error("Error al intentar reconectar al servidor OPC UA: {}", e.getMessage(), e);
                                try {
                                    throw new ReconnectionException("Error de reconexion exponencial:",e.getCause());
                                } catch (ReconnectionException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }); // Incrementar el tiempo de espera y reintentar
            }
        });
    }

    public CompletableFuture<Boolean> linearreconnection(Url url){
        return attemptlinearReconnectionWithUrl(url,INITIAL_RETRY,WAIT_TIME);
    }

    private CompletableFuture<Boolean>attemptlinearReconnectionWithUrl(Url url,int retries,double waitTime) throws ConnectionException {
        if (url == null || url.toString().isEmpty()) {
            logger.error("La URL proporcionada es nula o vacía. Se cancela la reconexión.");
            return CompletableFuture.failedFuture(new NullPointerException("Url vacia o nula."));
        }
        final int[] retry = {retries};
        // Define la tarea de reconexión
        return connect(url).thenCompose(success -> {
            if (success) {
                logger.info("Reconexión linear exitosa en el intento #{}", retry[0] + 1);
                return CompletableFuture.completedFuture(true); // Reconexión exitosa
            } else {
                logger.info("Reconexion fallida #{}. Esperando {} ms antes de reintentar...",retry[0] + 1,waitTime);
                return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                        .thenCompose(unused -> {
                            try {
                                return attemptlinearReconnectionWithUrl(url, retry[0] + 1,waitTime);
                            } catch (ConnectionException | ReconnectionException e) {
                                try {
                                    throw new ReconnectionException("Error de reconexion exponencial",e.getCause());
                                } catch (ReconnectionException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        }); // Incrementar el tiempo de espera y reintentar
            }
        });
    }

    public CompletableFuture<Boolean> linearreconnection() throws Exception {
        return null;
    }

    /**
     * Verifies the health and connectivity status of the OPC UA server by performing a "ping" operation.
     * The method attempts to read a standard OPC UA node's value on the server to ensure it is reachable.
     * If the server is not connected, it logs a warning and returns false.
     * If the operation is successful and a valid value is returned from the server, the method logs the response
     * and returns true. Otherwise, it logs an error or warning based on the situation.
     *
     * @return true if the server responds successfully with a valid value; false otherwise
     */
    public CompletableFuture<Boolean> ping() throws PingException {
        if (opcUaClient == null) {
            logger.warn("Realice una conexión antes de intentar un ping.");
            return CompletableFuture.failedFuture(new PingException("El cliente OPC UA no está conectado."));
        }
        // Nodo estándar en OPC UA para verificar el estado del servidor
        NodeId pingNodeId = NodeId.parse("ns=0;i=2259");
        logger.debug("Ping se ejecutará en el nodo estándar con NodeId: {}", pingNodeId);
        // Leer el valor del nodo en el servidor de forma asíncrona
        try{
            return opcUaClient.readValue(0, TimestampsToReturn.Both, pingNodeId)
                    .thenApply(value -> {
                        if (value.getValue() != null) {
                            logger.info("Ping exitoso");
                            return true;
                        } else {
                            logger.warn("Ping fallido");
                            try {
                                throw new PingException("Ping fallido: el valor devuelto fue nulo.");
                            } catch (PingException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .exceptionally(ex -> {
                        logger.error("Error durante el ping al servidor OPC UA: {}", ex.getMessage(), ex);
                        try {
                            throw new PingException("Error durante el ping al servidor OPC UA.", ex);
                        } catch (PingException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }catch (Exception e){
            logger.error("Error durante el ping al servidor OPC UA: {}", e.getMessage(), e);
        }
        return null;
    }

    /**
     * Retrieves the current instance of the OPC UA client associated with this service.
     *
     * @return the instance of {@link OpcUaClient} used for managing OPC UA server communication.
     */
    public OpcUaClient getOpcUaClient() {
        return opcUaClient;
    }

    public TCPConnection castClass(Object object){
        if (object != null){
            return (TCPConnection) object;
        }
        return null;
    }

    public TcpConnection setOpcUaClient(OpcUaClient opcUaClient) {
        this.opcUaClient = opcUaClient;
        return this;
    }

    public TcpConnection setUrl(Url url) {
        this.targeturl = url;
        return this;
    }

    public Url url() {
        return targeturl;
    }

    public OpcUaClient opcUaClient() {
        return opcUaClient;
    }

}
