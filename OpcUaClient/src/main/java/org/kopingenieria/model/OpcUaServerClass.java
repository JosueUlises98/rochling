package org.kopingenieria.model;

import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.config.OpcUaServerConfig;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import org.eclipse.milo.opcua.stack.core.types.structured.BuildInfo;
import org.eclipse.milo.opcua.stack.server.EndpointConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Represents an OPC UA server implementation, providing functionality for configuration,
 * management, and lifecycle operations. This final class follows the singleton pattern,
 * ensuring only one server instance is instantiated and used throughout the application.
 * <p>
 * The class provides methods for server startup, shutdown, configuration, and namespace
 * management, leveraging ExecutorServices to handle asynchronous tasks efficiently.
 * <p>
 * Key functionalities include:
 * - OPC UA server instance creation and configuration.
 * - Management of namespaces, including node creation and value simulation.
 * - Asynchronous handling of server lifecycle operations.
 * <p>
 * Thread Safety:
 * - The singleton design enforces a single instance of this class.
 * - ExecutorServices handle concurrency and task scheduling safely.
 * <p>
 * Core Dependencies:
 * - Utilizes the Eclipse Milo OPC UA library to manage the server.
 * - Relies on SLF4J for logging capabilities.
 * <p>
 * Note: Proper resource cleanup (e.g., shutting down executors) is necessary to prevent
 * memory leaks or hanging threads when the server is stopped.
 */
public final class OpcUaServerClass {

    /**
     * Logger instance used for logging messages and events related to the OpcUaServerClass operations.
     * <p>
     * This static final logger captures runtime information, errors, and other logging details
     * that assist in debugging and monitoring the behavior of the OpcUaServerClass.
     * <p>
     * The logger is configured using SLF4J (Simple Logging Facade for Java) and provides a flexible
     * and extensible logging mechanism, enabling developers to integrate with various logging frameworks.
     */
    private static final Logger logger = LoggerFactory.getLogger(OpcUaServerClass.class);
    /**
     * Represents an instance of the OPC UA server within the application.
     * <p>
     * This field is declared as `final`, ensuring that the reference to the server
     * instance cannot be changed once initialized. It is responsible for managing
     * all functionalities related to the OPC UA server, such as establishing connections,
     * handling data exchanges, and maintaining the server's lifecycle.
     * <p>
     * The server is crucial for facilitating OPC UA communication, which is widely
     * used in industrial domains for connecting and interoperating with devices
     * or systems in a standardized manner.
     */
    private final OpcUaServer server;
    /**
     * A thread pool executor service used to manage and execute asynchronous tasks
     * within the OpcUaServerClass. This instance allows concurrent task execution
     * and scheduling, ensuring non-blocking operations for server processes.
     * <p>
     * Utilization:
     * - Executes tasks for starting, stopping, and simulating server operations.
     * - Handles long-running and background tasks related to the server.
     * - Ensures proper shutdown during the server's stop process.
     * <p>
     * Responsibilities:
     * - Provides a scalable and manageable thread pool for server operations.
     * - Ensures efficient use of system resources by managing concurrent threads.
     * - Supports asynchronous task execution for responsive server behavior.
     * <p>
     * Note:
     * - This field is initialized only once during the lifecycle of the server.
     * - Proper shutdown of the executor is critical for releasing resources and
     * avoiding memory leaks.
     */
    private final ExecutorService executorService;
    /**
     * A ScheduledExecutorService instance responsible for managing and scheduling
     * asynchronous tasks within the OpcUaServerClass.
     * <p>
     * This executor is used to coordinate and execute tasks such as starting the server,
     * stopping the server, or handling other periodic or delayed operations. It ensures
     * tasks are executed in a controlled and efficient manner, leveraging multi-threading
     * capabilities.
     * <p>
     * Characteristics:
     * - It provides methods for scheduling tasks with specific delays or at fixed rates.
     * - Tasks submitted through this executor are managed according to a scheduled thread pool.
     * - Utilized to facilitate asynchronous, non-blocking operations within the application.
     * <p>
     * Thread Safety:
     * - Thread-safe operations are ensured by the ScheduledExecutorService implementation.
     * - Proper shutdown procedures must be followed to release resources and prevent memory leaks.
     */
    private final ScheduledExecutorService executor;
    /**
     * Represents the namespace in the OPC UA server being developed, responsible for managing
     * nodes, variables, and the subscription model within the server.
     * <p>
     * The `NameSpace` class extends the `ManagedNamespace` class, inheriting its functionality
     * while providing specialized behavior for the application's specific namespace implementation.
     * <p>
     * The `nameSpace` field serves as a reference to this namespace instance within the
     * `OpcUaServerClass`. It encapsulates all functionality related to the namespace, including:
     * <p>
     * - Node creation and management: Handles the initialization of folders and variables within
     * the namespace. For example, variables like temperature, pressure, and status are created,
     * added to the node manager, and organized into folders.
     * <p>
     * - Subscription management: Maintains a subscription model to manage data items and monitor
     * changes, supporting the OPC UA publishing and subscription mechanism.
     * <p>
     * - Simulation of variable values: Updates data variables for simulation purposes, providing
     * randomized values and dynamic behaviors to support testing and demonstration.
     * <p>
     * Integration:
     * - Tightly integrated with the `OpcUaServer` instance to ensure seamless operation within
     * the server context.
     * - Utilized by the `OpcUaServerClass` for node and namespace management as part of the
     * OPC UA server's lifecycle (e.g., start, stop).
     * <p>
     * This field is declared `final`, indicating that its reference cannot be changed once
     * assigned, ensuring the integrity of the `NameSpace` instance during the server's runtime.
     */
    private final NameSpaceServer namespace;
    /**
     * A singleton instance of the OpcUaServerClass that provides centralized access
     * to the server operations and configurations.
     * <p>
     * This static field ensures that only one instance of OpcUaServerClass exists
     * throughout the application, following the singleton design pattern.
     * <p>
     * The instance is initialized and managed using the {@code getInstance} method,
     * which ensures thread-safe, lazy initialization.
     */
    private static OpcUaServerClass instance;

    /**
     * Provides a singleton instance of the OpcUaServerClass. Ensures that only
     * one instance is created and reused across the application.
     *
     * @return The singleton instance of OpcUaServerClass.
     * @throws Exception If an error occurs during initialization.
     */
    public static synchronized OpcUaServerClass getInstance() throws Exception {
        if (instance == null) {
            instance = new OpcUaServerClass();
        }
        return instance;
    }

    private OpcUaServerClass() throws Exception {
        this.server = new OpcUaServer(config());
        this.namespace = new NameSpaceServer(server);
        executor = Executors.newScheduledThreadPool(2);
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setName("OpcUaServerThread");
            thread.setDaemon(false); // Asegurarse de que el hilo no es system-thread
            return thread;
        });
    }

    /**
     * Configures and initializes the OpcUaServerConfig object for the application.
     * The configuration includes application name, endpoint configuration, and server build information.
     *
     * @return OpcUaServerConfig instance containing the configured OPC UA server settings.
     */
    private OpcUaServerConfig config() {
        // Definir la información de construcción
        BuildInfo buildInfo = new BuildInfo(
                "Kop Ingenieria",   // Nombre de la organización
                "MyProfessionalOpcUaServer", // Nombre del producto
                "1.0.0",                     // Versión del producto
                "1.0.0",                     // Número de compilación
                "https://kopingenieria.com", // URL del producto
                buildTimestamp()             // Timestamp de compilación
        );
        // Configurar el servidor OPC UA
        return OpcUaServerConfig.builder()
                .setApplicationName(LocalizedText.english("My Professional OPC UA Server")) // Nombre de la aplicación
                .setEndpoints(Collections.singleton(EndpointConfiguration.newBuilder()
                        .setBindAddress("127.0.0.1")
                        .setHostname("localserver")
                        .setBindPort(4840)
                        .setSecurityPolicy(SecurityPolicy.None)
                        .build()))
                .setBuildInfo(buildInfo)
                .build();
    }

    /**
     * Builds a timestamp representing the current date and time.
     *
     * @return A DateTime object representing the current timestamp.
     */
    private static DateTime buildTimestamp() {
        return DateTime.now();
    }

    /**
     * Starts the OPC UA server asynchronously using an executor service.
     * This method initiates the startup process for the server and logs
     * the status of the operation. If an exception occurs during the
     * startup process, it is logged.
     * <p>
     * Behavior:
     * - The server start operation is submitted to an executor service for asynchronous execution.
     * - Logs a message indicating the server startup attempt.
     * - On successful startup, logs a message confirming that the server is running.
     * - Logs any exceptions encountered during the startup process for troubleshooting purposes.
     */
    public void start() {
        executorService.submit(() -> {
            try {
                logger.info("Starting OPC UA Server...");
                server.startup().get();
                logger.info("OPC UA Server started successfully.");
                mostrarBannerInicio();
            } catch (Exception e) {
                logger.error("Error starting OPC UA Server: ", e);
            }
        });
    }

    /**
     * Stops the OPC UA server and releases associated resources.
     * <p>
     * This method attempts to gracefully shut down the server and related
     * executor services. If the shutdown does not complete within a specified
     * timeout, it forces the terminal state of the executor service. It logs
     * the progress and any errors that occur during the process.
     * <p>
     * Behavior:
     * - Initiates the shutdown of the OPC UA server.
     * - Attempts to terminate the executor service gracefully.
     * - Falls back to a forced termination of the executor service if it fails to
     * shut down within the configured timeout.
     * - Logs the outcomes, including successful completion or encountered errors.
     * <p>
     * Exceptions:
     * - Catches and logs any exceptions that might occur during the shutdown process.
     */
    public void stop() {
        try {
            logger.info("Stopping OPC UA Server...");
            server.shutdown().get();
            executorService.shutdown();
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            logger.info("OPC UA Server stopped successfully.");
        } catch (Exception e) {
            logger.error("Error stopping the OPC UA Server: ", e);
        }
    }

    /**
     * Retrieves the current configuration of the OPC UA server.
     *
     * @return An instance of {@code OpcUaServerConfig} representing the server's configuration.
     */
    private OpcUaServerConfig getServerConfig() {
        return server.getConfig();
    }

    /**
     * Initiates the data simulation process by scheduling regular updates
     * to simulate changes in node values for the OPC UA server.
     * <p>
     * This method uses a scheduled executor to run the `updateSimulatedValues`
     * method of the `namespace` object at a fixed interval of one second.
     * The simulation includes updating values such as temperature, pressure,
     * and system status to emulate realistic behavior in an OPC UA environment.
     * <p>
     * Behavior:
     * - Immediately schedules `namespace::updateSimulatedValues` with an initial delay of 0 seconds.
     * - Repeats the update task at a fixed interval of 1 second.
     * - Executes tasks on a separate thread to ensure responsive server operations.
     * <p>
     * Exception Handling:
     * - Any exceptions encountered during the execution of `updateSimulatedValues`
     * are caught and logged within the method implementation to avoid disrupting simulation.
     * <p>
     * Note:
     * - Ensure that the executor service is properly initialized before calling this method.
     * - This method should be called during server setup to begin the simulation process.
     */
    private void startDataSimulation() {
        executor.scheduleAtFixedRate(namespace::updateSimulatedValues, 0, 1, TimeUnit.SECONDS);
    }

    /**
     * Modifies the value of an existing node in the namespace.
     * Attempts to locate a node by its name and update its value with the specified new value.
     * Logs the operation status, including success, warnings if the node doesn't exist,
     * or errors encountered during the modification process.
     *
     * @param nodeName Name of the node to be modified.
     * @param newValue New value to be assigned to the specified node.
     * @return {@code true} if the node is successfully modified; {@code false} otherwise.
     */
    public boolean modifyNode(String nodeName, Object newValue) {
        return namespace.modifyExistingNode(nodeName, newValue);
    }

    /**
     * Deletes a node from the namespace based on the specified name.
     * This operation removes the node from the node registry and its associated manager.
     *
     * @param nodeName The name of the node to be deleted.
     * @return {@code true} if the node was successfully deleted, otherwise {@code false}.
     */
    public boolean deleteNode(String nodeName) {
        return namespace.deleteNode(nodeName);
    }

    /**
     * Retrieves the instance of the OPC UA Server.
     *
     * @return The current instance of {@code OpcUaServer}.
     */
    public OpcUaServer getServer() {
        return server;
    }

    /**
     * Displays the application's startup banner in the log output.
     * <p>
     * This method logs an ASCII representation of the company's banner
     * and provides branding information. The banner includes the company name
     * and is intended to be shown whenever the application starts to signal the
     * beginning of the server's operation.
     * <p>
     * Behavior:
     * - Constructs a multi-line ASCII banner string.
     * - Logs the banner using the configured logger instance.
     * <p>
     * Logging:
     * - Uses the logger to output the banner to the log at the INFO level.
     */
    private void mostrarBannerInicio() {
        String banner = """
                  _   _  ___  _  _  ____  ____  _  _   
                 / \\_/ )/ __)/ )( \\(  _ \\(  __)( \\/ )  
                ( ( o ))\\__ \\(__) )) __/ ) _) /)  /   
                 \\_\\/ (___/ \\____/(__)  (____)(__/    
                --------------------------------------
                KOP Ingeniería S.A. C.V. ®
                --------------------------------------
                """;
        logger.info("\n{}", banner);
    }

    /**
     * Logs the information of all currently connected OPC UA clients on the server.
     *
     * This method checks for active OPC UA sessions managed by the server
     * and retrieves connection details for each session. It logs the session ID,
     * application name, endpoint URL, remote address, and connection timestamp
     * of each connected client. If no clients are connected, it logs a message indicating
     * the absence of active connections.
     *
     * Behavior:
     * - If there are no active sessions, logs a message stating that no clients are connected.
     * - If active sessions are present, iterates through each session and constructs
     *   a formatted log entry containing client session details.
     *
     * Exception Handling:
     * - Catches and logs any exceptions that may occur during the execution of the
     *   session retrieval or logging process, without disrupting the overall functionality.
     */
    public void visualizarClientesConectados() {
        try {
            if (server.getSessionManager().getAllSessions().isEmpty()) {
                logger.info("No hay clientes OPC UA conectados actualmente al servidor.");
                return;
            }
            // Log de las sesiones activas
            logger.info("Clientes activos en el servidor OPC UA:");
            server.getSessionManager().getAllSessions().forEach(session -> {
                String clientInfo = """
                        Cliente OPC UA Conectado:
                            - Session ID: %s
                            - Application Name: %s
                        """.formatted(
                        session.getSessionId(),
                        session.getSessionName()
                );
                logger.info(clientInfo);
            });
        } catch (Exception e) {
            logger.error("Ocurrió un error al intentar obtener la información de los clientes conectados.", e);
        }
    }
}