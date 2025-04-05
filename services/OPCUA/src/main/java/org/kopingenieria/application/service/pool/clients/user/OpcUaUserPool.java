package org.kopingenieria.application.service.pool.clients.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.application.service.configuration.components.UserConfigurationComp;
import org.kopingenieria.domain.model.user.UserOpcUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

@Component
@AllArgsConstructor
public class OpcUaUserPool {

    @Autowired
    private UserConfigurationComp opcUaConfiguration;

    private final Map<ClientKey, PooledOpcUaClient> activeClients;
    private final Map<ClientKey, BlockingQueue<PooledOpcUaClient>> availableClients;
    private final ScheduledExecutorService maintenanceExecutor;

    @Data
    public static class ClientKey {
        private static final String id = UUID.randomUUID().toString();
        private String name;
        private static final LocalDateTime timestamp = LocalDateTime.now();

        public ClientKey(UserOpcUa userConfig) {
            this.name = userConfig.getName();
        }
    }

    @Data
    public static class PooledOpcUaClient {
        private final OpcUaClient client;
        private final ClientKey key;
        private final UserOpcUa userConfig;
        private volatile long lastUsed;
        private volatile boolean isValid;

        public PooledOpcUaClient(OpcUaClient client,
                                 UserOpcUa userConfig) {
            this.client = client;
            this.key = new ClientKey(userConfig);
            this.userConfig = userConfig;
            this.lastUsed = System.currentTimeMillis();
            this.isValid = true;
        }

        public boolean isConnected(){
            return userConfig.getConnection().getStatus().name().equals("CONNECTED");
        }
    }

    public Optional<PooledOpcUaClient> obtenerCliente(UserOpcUa userConfig) {
        ClientKey key = new ClientKey(userConfig);
        // Intentar obtener un cliente existente
        Optional<PooledOpcUaClient> existingClient = obtenerClienteExistente(key);
        if (existingClient.isPresent()) {
            return existingClient;
        }
        // Crear nuevo cliente si no existe
        return crearNuevoCliente(userConfig);
    }

    private Optional<PooledOpcUaClient> obtenerClienteExistente(ClientKey key) {
        BlockingQueue<PooledOpcUaClient> queue = availableClients.get(key);
        if (queue != null) {
            PooledOpcUaClient client = queue.poll();
            if (client != null && client.isValid) {
                client.lastUsed = System.currentTimeMillis();
                activeClients.put(key, client);
                return Optional.of(client);
            }
        }
        return Optional.empty();
    }

    private Optional<PooledOpcUaClient> crearNuevoCliente(UserOpcUa userConfig) {
        try {
            OpcUaClient client = opcUaConfiguration.createUserOpcUaClient(userConfig);

            PooledOpcUaClient pooledClient = new PooledOpcUaClient(client, userConfig);
            ClientKey key = new ClientKey(userConfig);

            activeClients.put(key, pooledClient);
            return Optional.of(pooledClient);

        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public void liberarCliente(PooledOpcUaClient client) {
        if (client != null && client.isValid) {
            client.lastUsed = System.currentTimeMillis();
            BlockingQueue<PooledOpcUaClient> queue = availableClients.computeIfAbsent(
                    client.key,
                    k -> new LinkedBlockingQueue<>()
            );
            queue.offer(client);
            activeClients.remove(client.key);
        }
    }

}
