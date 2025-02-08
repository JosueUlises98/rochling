package org.kopingenieria.services;

import org.kopingenieria.model.Url;
import java.util.concurrent.CompletableFuture;

/**
 * The Conexion interface defines the contract for managing connections
 * to external systems such as Programmable Logic Controllers (PLCs)
 * or OPC-UA compatible devices.
 *
 * This interface provides methods for establishing, closing, re-establishing connections,
 * and verifying connectivity to the desired endpoint. Implementations of this interface
 * are expected to handle connection-specific logic, ensuring secure and reliable operations.
 *
 * Methods:
 *
 * - conexion(Url url): Establishes a connection to the specified URL. Throws an exception
 *   if the connection cannot be established.
 *
 * - desconexion(): Closes an existing connection. Throws an exception if the disconnection fails.
 *
 * - reconexion(Url url): Re-establishes a connection to the specified URL. Combines the logic
 *   of disconnecting and then establishing a new connection. Throws an exception if the operation fails.
 *
 * - ping(): Verifies the health and status of the current connection to ensure connectivity.
 *   Throws an exception if connectivity cannot be verified.
 */
public interface Connection {
    /**
     * Establishes a connection to an external system or resource asynchronously.
     *
     * This method attempts to create a stable connection and may throw an exception
     * if an error occurs during the connection process.
     *
     * @return A CompletableFuture that, when completed, returns true if the connection
     *        */
    CompletableFuture<Boolean>connect()throws Exception;
    /**
     * Establishes a connection to an external system or resource asynchronously
     * using the specified URL.
     *
     * @param url The {@link Url} representing the endpoint to connect to.
     * @return A CompletableFuture that, when completed,*/
    CompletableFuture<Boolean> connect(Url url)throws Exception;
    /**
     * Terminates the connection to an external system or resource asynchronously.
     *
     * @return A CompletableFuture that, when completed, returns true if the disconnection
     *         was successful, or false if the disconnection attempt failed.
     * @throws Exception if an error occurs during the disconnection process.
     */
    CompletableFuture<Boolean> disconnect()throws Exception;
    /**
     * Attempts to reconnect to an external system or resource asynchronously using a backoff strategy.
     * This method retries the connection with incremental delays, aiming to achieve a stable connection
     * while managing resource usage efficiently during repeated connection attempts.
     *
     * @param url The {@link Url} representing the target endpoint for the recon*/
    CompletableFuture<Boolean> backoffreconnection(Url url)throws Exception;
    /**
     * Attempts to reconnect to an external system or resource asynchronously
     * using a backoff strategy. This approach ensures a systematic retry mechanism
     * with delays to manage*/
    CompletableFuture<Boolean> backoffreconnection()throws Exception;
    /**
     * Attempts to reconnect to an external system or resource asynchronously using a linear retry strategy.
     * This method tries to establish a connection to the specified URL with consistent intervals between retries.
     *
     * @param url The {@link Url} representing the endpoint to reconnect to.
     * @return A CompletableFuture that, when completed, returns true if the reconnection
     *         attempt was successful, or false if the reconnection failed.
     * @throws Exception if an error occurs during the reconnection process.
     */
    CompletableFuture<Boolean>linearreconnection(Url url)throws Exception;
    /**
     * Attempts to reconnect to an external system or resource asynchronously
     * using a linear retry strategy. This method retries the reconnection process
     * with consistent intervals between attempts until a stable connection is achieved
     * or the process is terminated due to an error or timeout.
     *
     * @return A CompletableFuture that, when completed, returns true if the reconnection
     *         attempt was successful, or false if the reconnection failed.
     * @throws Exception if an error occurs during the reconnection process.
     */
    CompletableFuture<Boolean>linearreconnection()throws Exception;
    /**
     * Sends a ping message to check the availability of the external system or resource
     * to which the connection has been established.
     *
     * @return A CompletableFuture that, when completed, returns true if the ping was successful
     *         and the system is reachable, or false if the ping failed or the system is unavailable.
     * @throws Exception if an error occurs while attempting to ping the external system or resource.
     */
    CompletableFuture<Boolean> ping()throws Exception;
    /**
     * Checks the connectivity status for a specific predefined URL.
     * This method is used to verify the health and accessibility of the external system
     * or resource associated with the specified URL.
     *
     * @param url The predefined {@link Url} object representing the endpoint to ping.
     * @return A CompletableFuture that, when completed, returns true if the connectivity
     *         to the URL is active and functioning correctly, or false otherwise.
     * @throws Exception if an error occurs while attempting to ping the URL.
     */
    CompletableFuture<Boolean>ping(Url url)throws Exception;
}
