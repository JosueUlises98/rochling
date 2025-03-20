package org.kopingenieria.domain.model.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.core.AccessLevel;
import org.eclipse.milo.opcua.sdk.core.Reference;
import org.eclipse.milo.opcua.sdk.server.OpcUaServer;
import org.eclipse.milo.opcua.sdk.server.api.DataItem;
import org.eclipse.milo.opcua.sdk.server.api.ManagedNamespace;
import org.eclipse.milo.opcua.sdk.server.api.MonitoredItem;
import org.eclipse.milo.opcua.sdk.server.nodes.UaFolderNode;
import org.eclipse.milo.opcua.sdk.server.nodes.UaVariableNode;
import org.eclipse.milo.opcua.sdk.server.util.SubscriptionModel;
import org.eclipse.milo.opcua.stack.core.Identifiers;
import org.eclipse.milo.opcua.stack.core.types.builtin.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Represents a custom OPC-UA namespace server that extends the functionality
 * of a ManagedNamespace. The NameSpaceServer class is responsible for managing
 * nodes, subscriptions, and interactions within the namespace.
 *
 * This class facilitates the creation, initialization, modification, and deletion
 * of OPC-UA nodes. It also includes a subscription model for efficiently handling
 * client interactions and monitored items.
 *
 * The NameSpaceServer integrates folder and variable nodes into the OPC-UA address
 * space, supporting functionality for managing PLC-specific data and other custom
 * variables. It also provides thread-safe mechanisms for node modification and deletion.
 */
public class NameSpaceServer extends ManagedNamespace {
    /**
     * Represents the subscription model used for managing OPC-UA subscriptions.
     * This variable is responsible for configuring, handling, and tracking
     * client-subscription interactions within the server namespace.
     *
     * It plays a central role in observing the monitored items,
     * sending updates to clients, and ensuring efficient data delivery.
     *
     * This instance is immutable and ensured thread-safe to avoid
     * concurrent access issues in the namespace server.
     */
    private final SubscriptionModel subscriptionModel;
    /**
     * A registry that maintains a mapping of node names to their corresponding UaVariableNode instances.
     *
     * This registry is used to store and manage OPC-UA variable nodes within the system. Each entry in the map
     * associates a unique node name (as a string key) with a UaVariableNode instance, facilitating efficient
     * access, modification, and deletion of nodes.
     *
     * The map is initialized as an empty HashMap and is immutable in reference (declared final),
     * ensuring thread safety when accessed through synchronized methods in the containing class.
     */
    private final Map<String, UaVariableNode> nodeRegistry = new HashMap<>();
    /**
     * The logger variable is a static final instance of Logger used for managing
     * logging within the NameSpaceServer class.
     *
     * This logger is initialized through the LogManager and is specifically tied
     * to the NameSpaceServer class, allowing the logging of important events,
     * warnings, errors, and debug messages throughout the lifecycle of the class.
     *
     * Utilized for:
     * - Debugging application behavior within NameSpaceServer.
     * - Reporting errors and exceptions encountered during the methods' execution.
     * - Providing runtime information for effective monitoring and diagnostics.
     *
     * Logging follows the configuration and patterns defined in the application's
     * logging framework, ensuring consistent and centralized log output.
     */
    private static final Logger logger = LogManager.getLogger(NameSpaceServer.class);
    /**
     * Constructs a new instance of the `NameSpaceServer` class, extending the functionality
     * of an OPC-UA ManagedNamespace. This class is responsible for creating and managing
     * the namespace of an OPC-UA server, including defining nodes and handling subscription models.
     *
     * Upon initialization, the server's namespace is set up using a predefined URI, and node structures
     * are created via the `initializeNodes` method.
     *
     * @param server The instance of the `OpcUaServer` to which this namespace will be attached.
     *               It provides the functionality and context necessary for creating the namespace.
     */
    public NameSpaceServer(OpcUaServer server) {
        super(server, "urn:professional:opcua:server:namespace");
        subscriptionModel = new SubscriptionModel(server, this);
        initializeNodes();
    }
    /**
     * Initializes the OPC-UA nodes within the namespace.
     * This method creates a parent folder node, named "PLC_Data," and adds it
     * under the Objects folder in the OPC-UA node hierarchy. Furthermore, it
     * initializes and registers specific OPC-UA variable nodes to represent
     * PLC data, such as Temperature, Pressure, and Status.
     *
     * Functionality:
     * - Creates a folder node for PLC data with the name "PLC_Data".
     * - Registers the folder node within the OPC-UA namespace.
     * - Creates and registers variable nodes as children of the "PLC_Data" folder:
     *   - Temperature: Represents a double value initialized with 25.0.
     *   - Pressure: Represents a double value initialized with 1013.25.
     *   - Status: Represents a string value initialized with "Running".
     */
    private void initializeNodes() {
        // Crear carpeta principal
        NodeId folderNodeId = new NodeId(getNamespaceIndex(), "DataFolder");
        UaFolderNode plcFolder = new UaFolderNode(
                getNodeContext(),
                folderNodeId,
                new QualifiedName(getNamespaceIndex(), "DataFolder"),
                LocalizedText.english("DataFolder")
        );
        getNodeManager().addNode(plcFolder);
        plcFolder.addReference(new Reference(
                plcFolder.getNodeId(),
                Identifiers.Organizes,
                Identifiers.ObjectsFolder.expanded(),
                true
        ));
        // Log para confirmar la creación de la carpeta
        logger.info("Carpeta PLC_Data creada con NodeId {}", folderNodeId);
        // Crear variables y registrarlas
        registerNode("Temperature", createVariable(plcFolder, "Temperature", Identifiers.Double, 25.0));
        registerNode("Pressure", createVariable(plcFolder, "Pressure", Identifiers.Double, 1013.25));
        registerNode("Status", createVariable(plcFolder, "Status", Identifiers.String, "Running"));
        // Logs verificando los nodos creados
        logger.info("Nodos Temperature, Pressure y Status registrados correctamente en PLC_Data");
    }
    /**
     * Creates a new variable node in the OPC-UA address space and organizes it under the given folder.
     * The created variable node is initialized with the specified data type and initial value.
     *
     * @param folder The parent folder node under which the new variable node will be organized.
     * @param name The name of the new variable node.
     * @param dataType The data type of the variable node, specified as a NodeId.
     * @param initialValue The initial value to be set for the variable node.
     * @return The created UaVariableNode instance.
     */
    private UaVariableNode createVariable(UaFolderNode folder, String name, NodeId dataType, Object initialValue) {
        NodeId nodeId = new NodeId(getNamespaceIndex(), "PLC_Data/" + name);
        @SuppressWarnings("deprecation")
        UaVariableNode node = UaVariableNode.builder(getNodeContext())
                .setNodeId(nodeId)
                .setBrowseName(new QualifiedName(getNamespaceIndex(), name))
                .setDisplayName(LocalizedText.english(name))
                .setDataType(dataType)
                .setAccessLevel(AccessLevel.READ_WRITE)
                .setUserAccessLevel(AccessLevel.READ_WRITE)
                .build();
        node.setValue(new DataValue(new Variant(initialValue)));
        getNodeManager().addNode(node);
        folder.addOrganizes(node);
        return node;
    }
    /**
     * Registers a node with the specified name in the node registry.
     *
     * @param name The unique name of the node to be registered.
     * @param node The UaVariableNode instance to be associated with the given name.
     */
    private void registerNode(String name, UaVariableNode node) {
        nodeRegistry.put(name, node);
    }
    /**
     * Modifies the value of an existing OPC-UA node identified by its name.
     * This method finds the node in the registry by its name, updates its value,
     * and logs the operation's success or failure. If the node does not exist or an
     * error occurs during the modification, the operation will fail.
     *
     * @param name The unique identifier name of the node to be modified.
     * @param newValue The new value to set for the specified node.
     * @return true if the node was successfully modified; false otherwise.
     */
    public synchronized boolean modifyExistingNode(String name, Object newValue) {
        try {
            UaVariableNode node = nodeRegistry.get(name);
            if (node != null) {
                node.setValue(new DataValue(new Variant(newValue)));
                logger.info("Nodo '{}' modificado con éxito. Nuevo valor: {}", name, newValue);
                return true;
            } else {
                logger.warn("El nodo '{}' no existe.", name);
            }
        } catch (Exception e) {
            logger.error("Error al modificar el nodo '{}': {}", name, e.getMessage());
        }
        return false;
    }
    /**
     * Deletes a node from the namespace by its name.
     * This method attempts to locate the node in the registry using the provided name,
     * removes it from the node manager if it exists, and logs the action.
     * If the node does not exist or an error occurs during deletion,
     * the method logs appropriate warnings or errors.
     *
     * @param name The name of the node to be deleted.
     * @return true if the node was successfully deleted; false otherwise.
     */
    public synchronized boolean deleteNode(String name) {
        try {
            UaVariableNode node = nodeRegistry.remove(name);
            if (node != null) {
                getNodeManager().removeNode(node.getNodeId());
                logger.info("Nodo '{}' eliminado con éxito.", name);
                return true;
            } else {
                logger.warn("El nodo '{}' no existe en el espacio de nombres.", name);
            }
        } catch (Exception e) {
            logger.error("Error al eliminar el nodo '{}': {}", name, e.getMessage());
        }
        return false;
    }
    /**
     * Updates the simulated values of nodes within the namespace.
     * This method simulates environmental state changes by modifying node values
     * for "Temperature", "Pressure", and "Status".
     *
     * Functionality:
     * - Generates random values for temperature (between 20.0 and 30.0) and pressure (between 1013.0 and 1018.0).
     * - Updates the respective nodes in the namespace with these simulated values using the `modifyExistingNode` method.
     * - Sets the "Status" node to "Warning: High Temperature" if the simulated temperature exceeds 25.0,
     *   otherwise it sets the node to "Normal Operation".
     *
     * Exception Handling:
     * - Logs an error if any exception occurs while updating the simulated values.
     *
     * Dependencies:
     * - Relies on the `modifyExistingNode` method to update nodes in the namespace.
     * - Assumes existence of nodes named "Temperature", "Pressure", and "Status" in the node registry.
     */
    public void updateSimulatedValues() {
        try {
            double temp = 20.0 + (Math.random() * 10.0);
            double pressure = 1013.0 + (Math.random() * 5.0);
            modifyExistingNode("Temperature", temp);
            modifyExistingNode("Pressure", pressure);
            // Actualizar estado
            String status = temp > 25.0 ? "Warning: High Temperature" : "Normal Operation";
            modifyExistingNode("Status", status);
        } catch (Exception e) {
            logger.error("Error actualizando valores simulados", e);
        }
    }
    /**
     * Handles the creation of new data items in the OPC UA namespace.
     * This method is triggered when new data items are added to the system
     * and performs necessary operations on the provided data items.
     *
     * @param items A list of {@code DataItem} objects representing the newly created data items.
     */
    @Override
    public void onDataItemsCreated(List<DataItem> items) {
        subscriptionModel.onDataItemsCreated(items);
    }
    /**
     * Handles modifications to a list of data items. This method is invoked
     * whenever changes are detected in the provided data items, allowing the
     * system to react and update its internal state accordingly.
     *
     * @param items The list of {@code DataItem} objects that have been modified.
     */
    @Override
    public void onDataItemsModified(List<DataItem> items) {
        subscriptionModel.onDataItemsModified(items);
    }
    /**
     * Handles the deletion of data items.
     * This method is triggered when a collection of {@link DataItem} objects is deleted
     * and performs cleanup or necessary updates in the subscription model.
     *
     * @param items A list of {@link DataItem} objects that have been deleted.
     */
    @Override
    public void onDataItemsDeleted(List<DataItem> items) {
        subscriptionModel.onDataItemsDeleted(items);
    }
    /**
     * Handles changes in the monitoring mode of a list of monitored items.
     * This method is invoked when the monitoring mode of one or more monitored items
     * in the subscription changes.
     *
     * @param items A list of {@link MonitoredItem} instances whose monitoring mode has changed.
     */
    @Override
    public void onMonitoringModeChanged(List<MonitoredItem> items) {
        subscriptionModel.onMonitoringModeChanged(items);
    }
}
