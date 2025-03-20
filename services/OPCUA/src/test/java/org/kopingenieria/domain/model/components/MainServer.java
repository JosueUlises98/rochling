package org.kopingenieria.domain.model.components;

import org.kopingenieria.domain.model.server.OpcUaServerClass;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainServer {

    public static void main(String[] args) throws Exception {
        // Crear una instancia del servidor personalizado
        OpcUaServerClass opcUaServer = OpcUaServerClass.getInstance();
        // Iniciar el servidor con un gancho para cerrar ordenadamente en caso de interrupción
        opcUaServer.start();
        // Crear un `ScheduledExecutorService` para programar tareas periódicas
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        // Programar la visualización periódica de la información del cliente OPC UA
        scheduler.scheduleAtFixedRate(opcUaServer::visualizarClientesConectados, 0, 1, TimeUnit.MINUTES);
        // Agregar un gancho para detener el planificador junto con el servidor
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            opcUaServer.stop();
            scheduler.shutdown();
        }));
        // Mantener el servidor corriendo
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
}