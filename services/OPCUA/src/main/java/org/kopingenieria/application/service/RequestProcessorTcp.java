package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.types.builtin.DataValue;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;

import java.util.concurrent.CompletableFuture;

public class RequestProcessorTcp {

    private OpcUaClient opcUaClient;

    /**
     * Procesa solicitudes recibidas del cliente y se comunica con el servidor OPC UA.
     */
    private String processRequest(String request) {
        try {
            if (request.startsWith("READ:")) {
                // Leer un nodo del servidor OPC UA
                String nodeIdStr = request.split(":")[1];
                NodeId nodeId = new NodeId(2, nodeIdStr);
                CompletableFuture<DataValue> futureValue = opcUaClient.readValue(0, TimestampsToReturn.Both, nodeId);
                DataValue value = futureValue.get();
                return "Valor leído: " + value.getValue().getValue();
            } else {
                return "Comando no reconocido: " + request;
            }
        } catch (Exception e) {
            return "Error al procesar la solicitud: " + e.getMessage();
        }
    }
}
