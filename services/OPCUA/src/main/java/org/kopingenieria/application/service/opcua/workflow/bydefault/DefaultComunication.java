package org.kopingenieria.application.service.opcua.workflow.bydefault;

import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;

import java.util.Map;

public interface DefaultComunication {
    /**
     * Lectura dinámica desde el servidor OPC UA.
     *
     * @param nodeId  El identificador del nodo a leer.
     * @param options Opciones de configuración para la lectura (e.g., tipo de datos, formato).
     */
    void lectura(NodeId nodeId, Map<String, Object> options)throws Exception;
    /**
     * Escritura dinámica en el servidor OPC UA.
     *
     * @param nodeId  El identificador del nodo donde escribir.
     * @param value   El valor que se desea escribir.
     * @param options Opciones de configuración para la escritura (e.g., modo, prioridad, etc.).
     */
    void escritura(NodeId nodeId, Object value, Map<String, Object> options)throws Exception;
    /**
     * Modificación de un valor o nodo de forma dinámica.
     *
     * @param nodeId        El identificador del nodo a modificar.
     * @param modifications Mapa de cambios o ajustes por aplicar.
     */
    void modificacion(String nodename,Object newValue)throws Exception;
    /**
     * Eliminación dinámica de un nodo o suscripción en el servidor OPC UA.
     *
     * @param nodeId  El identificador del nodo o recurso a eliminar.
     * @param options Opciones adicionales para configurar la operación de eliminación.
     */
    void eliminacion(NodeId nodeId, Map<String, Object> options)throws Exception;
    /**
     * Suscripción dinámica a un nodo del servidor OPC UA.
     *
     * @param nodeId  El identificador del nodo al cual suscribirse para recibir actualizaciones.
     * @param options Opciones de configuración para la suscripción (e.g., intervalo de monitoreo, modo de notificación).
     */
    void suscripcion(NodeId nodeId, Map<String, Object> options)throws Exception;
}
