package org.kopingenieria.application.service.opcua.workflow.user;


import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.OpcUaSession;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.StatusCode;
import org.eclipse.milo.opcua.stack.core.types.builtin.Variant;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteNodesItem;
import org.eclipse.milo.opcua.stack.core.types.structured.DeleteNodesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class UserComunicationImpl implements UserComunication {
    /**
     * Logger instance used for logging operations throughout the ComunicacionClienteService class.
     *
     * This logger provides mechanisms for recording application-level events, debugging information,
     * errors, and other significant runtime data. It is configured via the SLF4J framework and uses
     * the LoggerFactory to retrieve a logger specific to the ComunicacionClienteService class.
     *
     * The logger is declared as a static final constant to ensure a single shared instance
     * throughout the lifecycle of the class, promoting efficient memory usage and consistency
     * across all logging operations.
     */
    private static final Logger logger = LoggerFactory.getLogger(UserComunicationImpl.class);
    /**
     * Represents an instance of the ConexionClienteService used to manage
     * client communication and interactions within the service.
     * This is a dependency required for implementing and handling
     * communication operations such as reading, writing, modifying,
     * and managing subscriptions for OPC UA nodes.
     * As a final field, it cannot be reassigned after being initialized.
     */
    private final OpcUaSession session;
    /**
     * Represents the OPC UA client used for managing communication and interactions
     * with OPC UA servers. This client is responsible for executing client-side
     * operations such as reading, writing, subscribing, and modifying nodes in the
     * OPC UA server's address space. It facilitates connection management, data
     * exchange, and monitoring of server-side events or changes.
     *
     * This field is initialized during the construction of the {@code ComunicationService} class
     * and serves as the primary interface for OPC UA-related functionalities within the system.
     * The client's lifecycle, including its initialization, usage, and termination, should align
     * with the encapsulating service or process requirements.
     */
    private final OpcUaClient opcuaclient;
    /**
     * Constructor for the ComunicacionClienteService class.
     * Initializes the required services and components for client-server communication.
     *
     * @param conexionClienteService the service responsible for managing client connections
     * @param opcuaserver the OPC UA server handling server-side operations
     * @param opcuaclient the OPC UA client used for client-side interactions
     */
    public UserComunicationImpl(OpcUaSession session, OpcUaClient opcuaclient) {
        this.session=session;
        this.opcuaclient = opcuaclient;
    }
    /**
     * Reads the value of the specified OPC-UA node and logs the result.
     * This method attempts to establish a read operation with the provided node identifier (`NodeId`)
     * and logs the value retrieved from the server. It also handles any exceptions
     * related to the connection or read operation failures.
     *
     * @param nodeId The identifier of the OPC-UA node to be read.
     * @param options A map containing additional options for the read operation,
     *                which can be expanded to include parameters such as timeout configuration or custom headers.
     */
    @Override
    public void lectura(NodeId nodeId, Map<String, Object> options) {
        try {
            if (session == null || session.getSessionId() == null) {
                throw new UaException(StatusCodes.Bad_ServerNotConnected);
            }
            // Leer el valor del nodo.
            CompletableFuture<DataValue> readFuture = opcuaclient.readValue(0, TimestampsToReturn.Both, nodeId);
            DataValue dataValue = readFuture.get();
            // Loggear el resultado.
            logger.info("Lectura exitosa del nodo {}: {}", nodeId, dataValue.getValue().getValue());
        } catch (Exception e) {
            logger.error("Error al realizar la lectura del nodo {}: {}", nodeId, e.getMessage());
        }
    }
    /**
     * Writes a value to the specified OPC-UA node.
     * This method attempts to write the provided value to the node identified by the given `NodeId`.
     * If the write operation is successful, a log entry is created indicating the success.
     * If an error occurs during the operation, it logs the error details.
     *
     * @param nodeId The {@code NodeId} of the target OPC-UA node where the value will be written.
     * @param value The value to be written to the node. Expected to be compatible with the OPC-UA node's data type.
     * @param options A map of additional options or settings for the write operation.
     */
    @Override
    public void escritura(NodeId nodeId, Object value, Map<String, Object> options) {
        try {
            if (session == null || session.getSessionId() == null) {
                throw new UaException(StatusCodes.Bad_ServerNotConnected);
            }
            // Crear el valor a escribir
            DataValue dataValue = new DataValue(new Variant(value));
            // Escribir en el nodo
            StatusCode statusCode = opcuaclient.writeValue(nodeId, dataValue).get();
            // Validar el estado de la operación
            if (statusCode.isGood()) {
                logger.info("Escritura exitosa en el nodo {}: {}", nodeId, value);
            } else {
                throw new UaException(statusCode);
            }
        } catch (Exception e) {
            logger.error("Error al realizar la escritura en el nodo {}: {}", nodeId, e.getMessage());
        }
    }
    /**
     * Modifies the value of a specified node within an OPC-UA server.
     *
     * @param nodename The name of the node to be modified.
     * @param newValue The new value to be assigned to the specified node.
     */
    @Override
    public void modificacion(String nodename,Object newValue) {
        try {
            if (session == null || session.getSessionId() == null) {
                throw new UaException(StatusCodes.Bad_ServerNotConnected);
            }
            // Crear el valor a escribir
            DataValue dataValue = new DataValue(new Variant(newValue));
            // Escribir en el nodo
            StatusCode statusCode = opcuaclient.writeValue(NodeId.parse(nodename), dataValue).get();
            // Validar el estado de la operación
            if (statusCode.isGood()) {
                logger.info("Modificacion exitosa en el nodo {}: {}",nodename,dataValue);
            } else {
                throw new UaException(statusCode);
            }
        } catch (Exception e) {
            logger.error("Error al modificar el nodo '{}': {}", nodename, e.getMessage());
        }
    }
    /**
     * Deletes a specified OPC-UA node, identified by its NodeId, from the OPC-UA server.
     * Logs the operation's success or failure and captures any exceptions encountered during the process.
     *
     * @param nodeId The identifier of the OPC-UA node to be removed.
     * @param options A map containing additional options or parameters for the deletion operation.
     */
    @Override
    public void eliminacion(NodeId nodeId, Map<String, Object> options) {
        try {
            if (session == null || session.getSessionId() == null) {
                throw new UaException(StatusCodes.Bad_ServerNotConnected);
            }
            CompletableFuture<DeleteNodesResponse> deleteNodes = opcuaclient.deleteNodes(List.of(new DeleteNodesItem(nodeId, true)));
            DeleteNodesResponse nodesResponse = deleteNodes.get();
            if (nodesResponse.getTypeId().isNull()){
                logger.info("Eliminacion exitosa del nodo {}", nodeId);
            }else {
                throw new UaException(StatusCode.BAD,"Error durante la eliminacion");
            }
        } catch (Exception e) {
            logger.error("Error al eliminar el nodo '{}': {}", nodeId, e.getMessage());
        }
    }

    @Override
    public void suscripcion(NodeId nodeId, Map<String, Object> options) {
    }
}
