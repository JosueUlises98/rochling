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

/**
 * Provides functionalities to create and manage a TCP connection using an OPC UA client. The class includes methods for
 * connecting, disconnecting, and reconnecting to a server with various strategies, along with utility methods to manage
 * and validate the connection. The connection can be backed by retry mechanisms and ping operations to ensure connectivity.
 *
 * This class restricts direct instantiation through private constructors to enforce controlled and centralized management
 * of the connection-related tasks.
 *
 * The methods allow for synchronous and asynchronous handling of connection operations and include handling of connection
 * exceptions during failures.
 *
 * Fields:
 * - `logger`: The logger instance used for logging events and errors.
 * - `MAX_RETRIES`: Maximum retry count allowed for reconnections.
 * - `INITIAL_WAIT`: Initial delay in milliseconds before retrying a failed connection.
 * - `BACKOFF_FACTOR`: Factor used to calculate the delay for exponential backoff strategy.
 * - `WAIT_TIME`: Common wait time for linear reconnections.
 * - `INITIAL_RETRY`: Initial number of retries for backoff processes.
 * - `opcUaClient`: The {@link OpcUaClient} instance used for communication.
 * - `validatorConection`: A utility to validate the connection state.
 * - `targeturl`: The target {@link Url} associated with the connection.
 */
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
    /**
     * Establishes a connection to an OPC UA server using the provided URL.
     * This method validates the connection parameters, ensures the session is active,
     * and attempts to connect to the server. It also handles timeouts and unexpected errors.
     *
     * @param url the URL object containing the endpoint address of the OPC UA server.
     * @return a CompletableFuture that resolves to true if the connection is successfully established.
     * @throws ConnectionException if an error occurs during the connection process, such as an invalid session,
     *         timeout, or other unexpected issues.
     */
    public CompletableFuture<Boolean> connect(Url url) throws ConnectionException {
        logger.info("Conectando a un cliente TCP en URL: {}", url.getUrl());
        try {
            // Usamos CompletableFuture y desempaquetamos posibles excepciones
            return CompletableFuture.supplyAsync(() -> {
                        // Validar que la sesión está activa y que la URL es válida
                        if (!(validatorConection.sesionActiva(opcUaClient) && validatorConection.validateLocalHost(url.getUrl()))) {
                            logger.warn("La sesión OPC UA no está activa.");
                            throw new CompletionException(new ConnectionException("La sesión OPC UA no está activa o la URL es inválida."));
                        }
                        return true;
                    })
                    .thenCompose(valid -> {
                        // Intentar conectar al cliente OPC UA
                        return opcUaClient.connect()
                                .thenApply(connection -> {
                                    logger.info("Conectado al servidor OPC UA en URL: {}", url.getUrl());
                                    return true;
                                });
                    })
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        // Manejar excepciones y convertirlas en `ConnectionException`
                        if (ex.getCause() instanceof TimeoutException) {
                            logger.error("Tiempo límite de conexión superado al servidor OPC UA");
                            throw new CompletionException(new ConnectionException("Tiempo límite de conexión superado al servidor OPC UA.", ex));
                        } else if (ex.getCause() instanceof ConnectionException) {
                            throw new CompletionException(ex.getCause());
                        } else {
                            logger.error("Error desconocido al conectar al servidor OPC UA en URL: {}", url.getUrl(), ex);
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
    /**
     * Establishes a connection to an OPC UA server using the provided client and validates the connection.
     * The connection is asynchronous and returns a CompletableFuture that resolves once the connection
     * has been successfully established.
     *
     * The following operations are performed:
     * - Validates if the session is active and the target URL is valid.
     * - Attempts to connect to the OPC UA server.
     * - Handles exceptions such as timeout, invalid session, or unknown errors, and wraps them in
     *   a {@link ConnectionException}.
     *
     * @return a CompletableFuture that completes with a Boolean value:
     *         - {@code true} if the connection was successfully established,
     *         - {@code false} otherwise.
     * @throws ConnectionException if the session is inactive, the URL is invalid, or if an error
     *         occurs during the connection process, including timeouts or unexpected conditions.
     */
    public CompletableFuture<Boolean> connect() throws ConnectionException {
        logger.info("Conectando a un cliente TCP en URL: {}.", targeturl.getUrl());
        try {
            // Usamos CompletableFuture y desempaquetamos posibles excepciones
            return CompletableFuture.supplyAsync(() -> {
                        // Validar que la sesión está activa y que la URL es válida
                        if (!(validatorConection.sesionActiva(opcUaClient) && validatorConection.validateLocalHost(targeturl.getUrl()))) {
                            logger.warn("La sesión OPC UA no está activa");
                            throw new CompletionException(new ConnectionException("La sesión OPC UA no está activa o la URL es inválida."));
                        }
                        return true;
                    })
                    .thenCompose(valid -> {
                        // Intentar conectar al cliente OPC UA
                        return opcUaClient.connect()
                                .thenApply(connection -> {
                                    logger.info("Conectado al servidor OPC UA en URL: {}.", targeturl.getUrl());
                                    return true;
                                });
                    })
                    .orTimeout(10, TimeUnit.SECONDS)
                    .exceptionally(ex -> {
                        // Manejar excepciones y convertirlas en `ConnectionException`
                        if (ex.getCause() instanceof TimeoutException) {
                            logger.error("Tiempo límite de conexión superado al servidor OPC UA.");
                            throw new CompletionException(new ConnectionException("Tiempo límite de conexión superado al servidor OPC UA.", ex));
                        } else if (ex.getCause() instanceof ConnectionException) {
                            throw new CompletionException(ex.getCause());
                        } else {
                            logger.error("Error desconocido al conectar al servidor OPC UA en URL: {}", targeturl.getUrl(), ex);
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
    /**
     * Disconnects the existing OPC UA client asynchronously, releasing any associated resources.
     * If the client is already disconnected or non-existent, a {@code DisconnectException} is thrown.
     * This operation is handled asynchronously and any exceptions occurring during the disconnection
     * process are captured and rethrown as a {@code DisconnectException}.
     *
     * @return A {@code CompletableFuture<Boolean>} that completes with {@code true} if the disconnection
     *         was successful or rethrows a {@code DisconnectException} on failure.
     * @throws DisconnectException If the client is already disconnected, non-existent, or an error occurs
     *         during the disconnection process.
     */
    public CompletableFuture<Boolean> disconnect() throws DisconnectException {
        if (opcUaClient == null) {
            logger.warn("El cliente TCP ya está desconectado o no existe.");
            throw new DisconnectException("El cliente TCP ya está desconectado o no existe."); // Lanza la excepción de desconexión
        }
        try{
            // Procesar la desconexión de forma completamente asíncrona
            return opcUaClient.disconnect() // Llamada asíncrona para iniciar la desconexión
                    .thenApply(result -> {
                        logger.info("Desconectado exitosamente del servidor OPC UA.");
                        opcUaClient = null; // Libera el cliente tras la desconexión
                        return true;
                    })
                    .exceptionally(ex -> { // Manejar excepciones y arrojar una DisconnectException
                        logger.error("Error al desconectar el cliente TCP: {}", ex.getMessage(), ex);
                        throw new CompletionException(new DisconnectException("Error durante la desconexión del cliente TCP.", ex));
                    });
        }catch (CompletionException e){
            // Desempaqueta y propaga DisconnectException
            if (e.getCause() instanceof DisconnectException) {
                logger.error("Error al desconectar el cliente TCP: {}", e.getCause().getMessage(), e.getCause());
                throw (DisconnectException) e.getCause();
            }
            throw new DisconnectException("Error en la desconexion de un cliente TCP.", e);
        }
    }
    /**
     * Attempts a backoff reconnection to the specified URL. The method applies
     * an incremental retry mechanism with an initial retry count and wait period.
     *
     * @param url the URL to which the reconnection attempt is made
     * @return a CompletableFuture containing a Boolean value indicating
     *         whether the reconnection attempt was successful
     * @throws ReconnectionException if the reconnection attempt fails
     */
    public CompletableFuture<Boolean> backoffreconnection(Url url) throws ReconnectionException {
        return attemptBackoffReconnectionWithUrl(url,INITIAL_RETRY,INITIAL_WAIT);
    }
    /**
     * Attempts to reconnect to a given URL using a backoff strategy, retrying the connection
     * with an increased wait time after each failed attempt up to a maximum retry limit.
     *
     * @param url the URL to which the connection attempt should be made
     * @param initialretry the current retry count, starting from 0
     * @param waittime the initial wait time in milliseconds before attempting reconnection
     * @return a CompletableFuture that completes with {@code true} if the reconnection is successful,
     *         or {@code false} if the maximum retry limit is reached without success
     * @throws ReconnectionException if the reconnection process fails due to an unrecoverable error
     */
    private CompletableFuture<Boolean> attemptBackoffReconnectionWithUrl(Url url,int initialretry,double waittime) throws ReconnectionException {
        if (initialretry >= MAX_RETRIES) {
            logger.error("Numero de intentos excedidos {} intentos",initialretry);
            return CompletableFuture.completedFuture(false); // Devuelve un futuro fallido después del límite máximo de intentos
        }
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    logger.info("Reconexión en el intento. #{}", initialretry + 1);
                    return CompletableFuture.completedFuture(true); // Reconexión exitosa
                } else {
                    logger.info("Intento de reconexion fallido #{}. Esperando {} ms....", initialretry + 1, waittime);
                    return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waittime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                            .thenCompose(unused -> {
                                try {
                                    return attemptBackoffReconnectionWithUrl(url, initialretry + 1, waittime * BACKOFF_FACTOR);
                                } catch (ReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            }); // Incrementar el tiempo de espera y reintentar
                }
            });
        }catch (ConnectionException | CompletionException e){
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof ReconnectionException) {
                logger.error("Error al reconectar el cliente TCP: {}", e.getCause().getMessage(), e.getCause());
                throw (ReconnectionException) e.getCause();
            }
            throw new ReconnectionException("Error en la reconexion de un cliente TCP.", e);
        }
    }
    /**
     * Attempts to re-establish a connection using an exponential backoff strategy.
     * This method aims to handle transient connectivity issues by retrying the
     * connection attempt after increasing wait intervals on each failure, up to
     * a maximum number of retries.
     *
     * @return a CompletableFuture that completes with a Boolean value indicating
     *         whether the reconnection was successful (true) or not (false).
     * @throws ReconnectionException if the reconnection process encounters a
     *         fatal error or exceeds the allowed retries.
     */
    public CompletableFuture<Boolean> backoffreconnection() throws ReconnectionException {
        return attemptBackoffReconnectionWithoutUrl(targeturl,INITIAL_RETRY,INITIAL_WAIT);
    }
    /**
     * Attempts to reconnect to a given URL with a backoff strategy. The method retries the connection
     * a specified number of times, increasing the wait time based on a defined backoff factor for
     * each unsuccessful attempt. If the maximum number of retries is reached, the method returns
     * a failed future.
     *
     * @param url the target URL to reconnect to.
     * @param retries the current attempt count for reconnection.
     * @param waitTime the delay in milliseconds before the next connection attempt.
     * @return a CompletableFuture that resolves to {@code true} if reconnection is successful,
     *         or {@code false} if the maximum number of retries is exceeded.
     * @throws ReconnectionException if an error occurs during reconnection.
     */
    private CompletableFuture<Boolean> attemptBackoffReconnectionWithoutUrl(Url url, int retries, double waitTime) throws ReconnectionException {
        if (retries >= MAX_RETRIES) {
            logger.error("Numero de intentos excedidos {} intentos.", retries);
            return CompletableFuture.completedFuture(false); // Devuelve un futuro fallido después del límite máximo de intentos
        }
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    logger.info("Reconexión en el intento #{}", retries + 1);
                    return CompletableFuture.completedFuture(true); // Reconexión exitosa
                } else {
                    logger.info("Intento de reconexion fallido #{}. Esperando {} ms...", retries + 1, waitTime);
                    return CompletableFuture.supplyAsync(() -> null, CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS)) // Devuelve un futuro después del retraso
                            .thenCompose(unused -> {
                                try {
                                    return attemptBackoffReconnectionWithUrl(url, retries + 1, waitTime * BACKOFF_FACTOR);
                                } catch (ReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            }); // Incrementar el tiempo de espera y reintentar
                }
            });
        }catch (ConnectionException | CompletionException e){
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof ReconnectionException) {
                logger.error("Error al reconectar el cliente TCP: {}", e.getCause().getMessage(), e.getCause());
                throw (ReconnectionException) e.getCause();
            }
            throw new ReconnectionException("Error en la reconexion de un cliente TCP.", e);
        }
    }
    /**
     * Attempts to re-establish a connection to the specified URL using a linear reconnection strategy.
     *
     * @param url the URL to which the reconnection attempt should be made
     * @return a CompletableFuture that resolves to true if the reconnection is successful,
     *         or false if the reconnection fails
     * @throws ReconnectionException if an error occurs during the reconnection attempt
     */
    public CompletableFuture<Boolean> linearreconnection(Url url) throws ReconnectionException {
        return attemptlinearReconnectionWithUrl(url,INITIAL_RETRY,WAIT_TIME);
    }
    /**
     * Attempts a linear reconnection to the specified URL with a defined number of retries and wait time between attempts.
     * If the connection fails after exhausting all retries, a {@link ReconnectionException} is thrown.
     *
     * @param url the URL to connect to
     * @param retries the number of retry attempts allowed
     * @param waitTime the wait time in milliseconds before each retry
     * @return a CompletableFuture indicating whether the reconnection was successful or not
     * @throws ReconnectionException if the reconnection process fails after exhausting retries
     * or if an unexpected error occurs during the process
     */
    private CompletableFuture<Boolean>attemptlinearReconnectionWithUrl(Url url, int retries, double waitTime) throws ReconnectionException{
        final int[] retry = {retries};
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    // Reconexión exitosa
                    logger.info("Reconexión lineal en el intento. #{}", retry[0] + 1);
                    return CompletableFuture.completedFuture(true);
                } else {
                    retry[0]--; // Reducir el número de reintentos restantes
                    if (retry[0] <= 0) {
                        // Si no quedan más reintentos, lanzar la excepción
                        logger.error("ReconnectionException: Maximo de intentos excedidos");
                        try {
                            throw new ReconnectionException("Maximo de intentos excedidos.");
                        } catch (ReconnectionException e) {
                            throw new CompletionException(e);
                        }
                    }
                    // Si aún quedan intentos, esperar y volver a intentarlo
                    logger.info("Reintento fallido #{}. Esperando {} ms....",
                            retries - retry[0], waitTime);
                    return CompletableFuture.supplyAsync(() -> null,
                                    CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                            .thenCompose(unused -> {
                                try {
                                    return attemptlinearReconnectionWithUrl(url, retry[0], waitTime);
                                } catch (ReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            });
                }
            }).exceptionally(ex -> {
                // Manejamos cualquier excepción no controlada previamente
                logger.error("Ocurrió un error durante la reconexión: {}", ex.getMessage());
                try {
                    throw new ReconnectionException("Error inesperado durante el proceso de reconexión", ex);
                } catch (ReconnectionException e) {
                    throw new CompletionException(e);
                }
            });
        }catch (CompletionException | ConnectionException e){
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof ReconnectionException) {
                logger.error("Error al reconectar el cliente TCP: {}", e.getCause().getMessage(), e.getCause());
                throw (ReconnectionException) e.getCause();
            }
            throw new ReconnectionException("Error en la reconexion de un cliente TCP.", e);
        }
    }
    /**
     * Attempts to establish a linear reconnection to the target while observing predefined retry and wait configurations.
     *
     * This method performs reconnection attempts in a linear sequence using a fixed number of retries and wait time between attempts.
     * The target URL for the reconnection is defined by the internal configuration.
     *
     * @return a CompletableFuture containing a Boolean result indicating whether the reconnection was successful (true) or not (false).
     * @throws ReconnectionException if an error occurs during the reconnection process.
     */
    public CompletableFuture<Boolean> linearreconnection() throws ReconnectionException {
        return attemptlinearReconnectionWithoutUrl(targeturl,INITIAL_RETRY,WAIT_TIME);
    }
    /**
     * Attempts to perform a linear reconnection to the given URL without creating a new URL object.
     * The method retries the connection for a specified number of attempts, waiting for a specified
     * amount of time between each attempt. If all retries are exhausted without success, a
     * {@link ReconnectionException} is thrown.
     *
     * @param url the target URL to reconnect to
     * @param retries the maximum number of retries allowed
     * @param waitTime the wait time in milliseconds between retries
     * @return a {@link CompletableFuture} containing {@code true} if the reconnection is successful,
     *         otherwise it completes exceptionally with a {@link ReconnectionException}
     * @throws ReconnectionException if the maximum retries are exceeded or an unexpected error occurs
     *         during the reconnection process
     */
    private CompletableFuture<Boolean>attemptlinearReconnectionWithoutUrl(Url url, int retries,double waitTime)throws ReconnectionException{
        final int[] retry = {retries};
        try {
            return connect(url).thenCompose(success -> {
                if (success) {
                    // Reconexión exitosa
                    logger.info("Reconexión lineal en el intento. #{}.", retry[0] + 1);
                    return CompletableFuture.completedFuture(true);
                } else {
                    retry[0]--; // Reducir el número de reintentos restantes
                    if (retry[0] <= 0) {
                        // Si no quedan más reintentos, lanzar la excepción
                        logger.error("ReconnectionException: Maximo de intentos excedidos.");
                        try {
                            throw new ReconnectionException("Maximo de intentos excedidos.");
                        } catch (ReconnectionException e) {
                            throw new CompletionException(e);
                        }
                    }
                    // Si aún quedan intentos, esperar y volver a intentarlo
                    logger.info("Reintento fallido #{}. Esperando {} ms...",retries - retry[0], waitTime);
                    return CompletableFuture.supplyAsync(() -> null,
                                    CompletableFuture.delayedExecutor((long) waitTime, TimeUnit.MILLISECONDS))
                            .thenCompose(unused -> {
                                try {
                                    return attemptlinearReconnectionWithUrl(url, retry[0], waitTime);
                                } catch (ReconnectionException e) {
                                    throw new CompletionException(e);
                                }
                            });
                }
            }).exceptionally(ex -> {
                // Manejamos cualquier excepción no controlada previamente
                logger.error("Ocurrió un error durante la reconexión: {}.", ex.getMessage());
                try {
                    throw new ReconnectionException("Error inesperado durante el proceso de reconexión", ex);
                } catch (ReconnectionException e) {
                    throw new CompletionException(e);
                }
            });
        }catch (CompletionException | ConnectionException e){
            // Desempaqueta y propaga ReconnectionException
            if (e.getCause() instanceof ReconnectionException) {
                logger.error("Error al reconectar el cliente TCP: {}", e.getCause().getMessage(), e.getCause());
                throw (ReconnectionException) e.getCause();
            }
            throw new ReconnectionException("Error en la reconexion de un cliente TCP.", e);
        }
    }
    /**
     * Sends a ping request to the OPC UA server to validate the connection state.
     * The method performs an asynchronous read operation on a standard OPC UA node.
     * If the server responds with valid data, the ping is considered successful.
     * In case of an error or null response, an appropriate exception will be thrown.
     *
     * @return a {@link CompletableFuture} that resolves to {@code true} if the ping is successful,
     *         or {@code false} if the response is invalid. In case of an error, the future completes
     *         exceptionally with a {@link PingException}.
     * @throws PingException if the OPC UA client is not connected or if an error occurs during the ping operation.
     */
    public CompletableFuture<Boolean> ping() throws PingException {
        if (opcUaClient == null) {
            logger.warn("Realice una conexión antes de intentar un ping.");
            // Fallo inmediato si el cliente no está conectado.
            throw new PingException("Cliente Tcp desconectado");
        }
        // Nodo estándar en OPC UA para verificar el estado del servidor
        NodeId pingNodeId = NodeId.parse("ns=0;i=2259");
        logger.debug("Ping se ejecutará en el nodo estándar con NodeId: {}", pingNodeId);
        // Realiza la lectura del valor del nodo en el servidor de forma asíncrona
        CompletableFuture<Boolean> pingFuture = new CompletableFuture<>();
        opcUaClient.readValue(0, TimestampsToReturn.Both, pingNodeId)
                .thenAccept(value -> {
                    if (value != null && value.getValue() != null) {
                        // El valor devuelto es válido, el ping es exitoso
                        logger.info("Ping exitoso.");
                        pingFuture.complete(true); // Completar el futuro con éxito
                    } else {
                        // Si el valor devuelto es nulo, lanzar directamente una excepción personalizada
                        logger.warn("Ping fallido: Repuesta enviada nula");
                        pingFuture.complete(false);
                        try {
                            throw new PingException("PingException: Respuesta enviada nula");
                        } catch (PingException e) {
                            throw new CompletionException(e);
                        }
                    }
                }).exceptionally(ex -> {
                    // Manejo de cualquier error que ocurra durante el proceso de ping
                    String errorMessage = "Error durante el ping al servidor OPC UA.";
                    logger.error("{}: {}", errorMessage, ex.getMessage(), ex);
                    pingFuture.completeExceptionally(new PingException(errorMessage, ex));
                    return null;
                });
        return pingFuture;
    }
    /**
     * Retrieves the OPC UA client instance associated with this connection.
     *
     * @return the {@link OpcUaClient} instance used to manage communication with the OPC UA server.
     */
    public OpcUaClient getOpcUaClient() {
        return opcUaClient;
    }
    /**
     * Casts the provided object to a {@code TCPConnection} instance if possible.
     * If the object is not null and is assignable to the {@code TCPConnection} type,
     * it performs the cast and returns the result. Otherwise, it returns null.
     *
     * @param object the object to be cast to {@code TCPConnection}
     * @return the casted {@code TCPConnection} object if successful, null otherwise
     */
    public TCPConnection castClass(Object object){
        if (object != null) {
            return (TCPConnection) object;
        }
        return null;
    }
    /**
     * Sets the OPC UA client instance associated with this connection.
     *
     * @param opcUaClient the {@link OpcUaClient} to be set for managing communication with the OPC UA server
     * @return the current instance of {@code TcpConnection} with the updated OPC UA client
     */
    public TcpConnection setOpcUaClient(OpcUaClient opcUaClient) {
        this.opcUaClient = opcUaClient;
        return this;
    }
    /**
     * Sets the target {@link Url} for this TCP connection.
     *
     * @param url the {@link Url} instance representing the target address to be set for the connection
     * @return the current instance of {@code TcpConnection} with the updated target URL
     */
    public TcpConnection setUrl(Url url) {
        this.targeturl = url;
        return this;
    }
    /**
     * Retrieves the target {@link Url} associated with this TCP connection.
     *
     * @return the {@link Url} instance representing the target address for the connection.
     */
    public Url url() {
        return targeturl;
    }
    /**
     * Provides access to the OPC UA client associated with this connection.
     *
     * @return the instance of {@link OpcUaClient}, which facilitates communication
     *         with the OPC UA server.
     */
    public OpcUaClient opcUaClient() {
        return opcUaClient;
    }

}
