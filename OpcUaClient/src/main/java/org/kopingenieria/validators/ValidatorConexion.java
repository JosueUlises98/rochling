package org.kopingenieria.validators;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.model.Url;

import java.net.InetAddress;
import java.net.URI;

/**
 * The ValidatorConexion class provides functionalities to validate and manage
 * OPC UA client connections and host validation. It includes methods for checking
 * the validity of connections, verifying host addresses, and preparing the connection process.
 * <p>
 * This class is part of the `org.kopingenieria.validators` package and is used for
 * connection-level validations within OPC UA communication workflows.
 * <p>
 * Key Features:
 * - Log-based diagnostics for connection states and host validation.
 * - Ensures that clients and hosts are verified before attempting further communication.
 * - Supports validation of host strings to ensure they resolve to loopback or local addresses.
 * <p>
 * Dependencies:
 * - Logger from LogManager for recording logs.
 * - OpcUaClient for client validation.
 * - Url for host and URL information.
 */
public final class ValidatorConexion {
    /**
     * Logger instance for the `ValidatorConexion` class.
     * <p>
     * This logger is used to record logs for operations and validations
     * performed within the class, such as warnings for null connections or
     * errors when resolving hosts. Specifically, it helps in diagnostic
     * operations by tracking unexpected scenarios and key events that occur
     * during execution.
     * <p>
     * The logger facilitates debugging and analysis for methods like:
     * - `nullClient`: Logs warnings if the client is null or not connected.
     * - `validateHost`: Logs errors if the provided host cannot be resolved.
     * - `preConexion`: Logs informational messages for already connected clients.
     * <p>
     * This logging functionality ensures a traceable and informative execution flow.
     */
    private static final Logger logger = LogManager.getLogger(ValidatorConexion.class);
    /**
     * Checks if the provided OPC UA client is null and logs a warning if true.
     *
     * @param client the OpcUaClient instance to check for null.
     * @return true if the client is null, false otherwise.
     */
    public boolean sesionActiva(UaClient client) {
        if (client != null) {
            logger.warn("Sesion activa del cliente opcua: {}", client.getSession().toString());
            return false;
        }
        return true;
    }
    /**
     * Validates the given host by resolving its DNS and checking if it matches a specific IP address.
     * This method attempts to resolve the hostname from the URL and verifies if it corresponds
     * to the predetermined IP address "192.168.1.12".
     *
     * @param host the host URL to be validated
     * @return {@code true} if the host resolves successfully and matches the specific IP address,
     *         otherwise {@code false}
     */
    public boolean validateHost(String host) {
        try {
            URI uri = new URI(host);
            InetAddress name = InetAddress.getByName(uri.getHost());// Intentamos resolver el nombre
            if (name != null) {
                if (name.getHostAddress().equalsIgnoreCase("192.168.1.12")) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Host no resolvible para la URL: {}. Mensaje: {}", host, e.getMessage());
            return false;
        }
        return false;
    }
    /**
     * Validates whether the provided host corresponds to a local loopback address.
     * This method attempts to resolve the host from the given URL and checks if
     * the resolved address is a loopback address (e.g., 127.0.0.1 or localhost).
     *
     * @param host the string representation of the URL to be validated.
     * @return {@code true} if the host resolves successfully and is identified as a local loopback address,
     *         otherwise {@code false}.
     */
    public boolean validateLocalHost(String host) {
        try {
            URI uri = new URI(host);
            InetAddress name = InetAddress.getByName(uri.getHost());// Intentamos resolver el nombre
            if (name != null) {
                if (name.isLoopbackAddress()) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Error de localhost para la URL: {}. Mensaje: {}", host, e.getMessage());
            return false;
        }
        return false;
    }

}
