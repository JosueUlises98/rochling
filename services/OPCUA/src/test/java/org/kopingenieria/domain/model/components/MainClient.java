package org.kopingenieria.domain.model.components;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kopingenieria.application.service.OpcuaConnection;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.exception.ConnectionException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


public class MainClient {

    private static final Logger logger = LogManager.getLogger(MainClient.class);

    public static void main(String[] args) {
        try {
            // Crear instancia de OpcuaConnection
            try (OpcuaConnection connection = new OpcuaConnection()) {

                // Establecer URL objetivo
                connection.setTargeturl(UrlType.OPCUA_LOCAL);

                // Intentar conectar
                CompletableFuture<Boolean> connectFuture = connection.connect();

                // Esperar a que se complete la conexión
                Boolean connected = connectFuture.get();

                if (connected) {
                    logger.info("Conexión establecida exitosamente");

                    // Realizar ping para verificar la conexión
                    try {
                        Boolean pingResult = connection.ping().get();
                        if (pingResult) {
                            logger.info("Ping exitoso al servidor");
                        } else {
                            logger.warn("Ping fallido al servidor");
                        }
                    } catch (Exception e) {
                        logger.error("Error durante el ping: {}", e.getMessage());
                    }
                    // Ejemplo de reconexión si es necesario
                    try {
                        Boolean reconnected = connection.backoffreconnection().get();
                        if (reconnected) {
                            logger.info("Reconexión exitosa");
                        } else {
                            logger.warn("Reconexión fallida");
                        }
                    } catch (Exception e) {
                        logger.error("Error durante la reconexión: {}", e.getMessage());
                    }
                    // Desconectar al finalizar
                    connection.disconnect().get();
                    logger.info("Desconexión exitosa");
                } else {
                    logger.error("No se pudo establecer la conexión");
                }
            }
        } catch (ConnectionException e) {
            logger.error("Error de conexión: {}", e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            logger.error("Error durante la ejecución: {}", e.getMessage());
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("Error inesperado: {}", e.getMessage());
        }
    }

}

