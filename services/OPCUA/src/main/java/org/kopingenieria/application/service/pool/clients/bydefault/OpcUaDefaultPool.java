package org.kopingenieria.application.service.pool.clients.bydefault;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.response.configuration.bydefault.DefaultConfigResponse;
import org.kopingenieria.application.service.configuration.bydefault.component.DefaultConfigComp;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

@Component("opcUaDefaultPool")
@AllArgsConstructor
public class OpcUaDefaultPool {

    @Autowired
    private DefaultConfigComp opcUaConfiguration;

    private final Map<ClientKey, PooledOpcUaClient> activeClients;
    private final Map<ClientKey, BlockingQueue<PooledOpcUaClient>> availableClients;
    private final ScheduledExecutorService maintenanceExecutor;


    @Data
    public static class ClientKey {
        private static final String id = UUID.randomUUID().toString();
        private String name;
        private static final LocalDateTime timestamp = LocalDateTime.now();

        public ClientKey(DefaultOpcUa defaultOpcUa) {
            String fullName = defaultOpcUa.getName();
            this.name = fullName != null && fullName.contains(".")
                    ? fullName.substring(0, fullName.lastIndexOf('.'))
                    : fullName;
        }
    }

    @Data
    public static class PooledOpcUaClient {
        private final OpcUaClient client;
        private final ClientKey key;
        private final DefaultOpcUa defaultConfig;
        private volatile long lastUsed;
        private volatile boolean isValid;

        public PooledOpcUaClient(OpcUaClient client,
                                 DefaultOpcUa defaultConfig) {
            this.client = client;
            this.key = new ClientKey(defaultConfig);
            this.defaultConfig = defaultConfig;
            this.lastUsed = System.currentTimeMillis();
            this.isValid = true;
        }

        public boolean isConnected(){
            return defaultConfig.getConnection().getStatus().name().equals("CONNECTED");
        }
    }

    public Optional<PooledOpcUaClient> obtenerCliente(String id) {
        DefaultOpcUa defaultclient = opcUaConfiguration.readDefaultConfiguration(id).getClient();
        ClientKey key = new ClientKey(defaultclient);
        // Intentar obtener un cliente existente
        Optional<PooledOpcUaClient> existingClient = obtenerClienteExistente(key);
        if (existingClient.isPresent()) {
            return existingClient;
        }
        // Crear nuevo cliente si no existe
        return crearNuevoCliente(id);
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

    private Optional<PooledOpcUaClient> crearNuevoCliente(String id) {
        try {
            DefaultConfigResponse response = opcUaConfiguration.readDefaultConfiguration(id);
            OpcUaClient miloClient = response.getMiloClient();
            DefaultOpcUa defaultClient = response.getClient();
            PooledOpcUaClient pooledClient = new PooledOpcUaClient(miloClient, defaultClient);
            ClientKey key = new ClientKey(defaultClient);
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
