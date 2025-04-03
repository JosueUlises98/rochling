package org.kopingenieria.application.service.opcua.pool.client.bydefault;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.kopingenieria.application.service.opcua.workflow.bydefault.DefaultConfigurationImpl;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.model.bydefault.DefaultConfigurationOpcUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;

@Component
@AllArgsConstructor
public class OpcUaDefaultPool {

    @Autowired
    private DefaultConfigurationImpl opcUaConfiguration;

    private final Map<ClientKey, PooledOpcUaClient> activeClients;
    private final Map<ClientKey, BlockingQueue<PooledOpcUaClient>> availableClients;
    private final ScheduledExecutorService maintenanceExecutor;

    public static class ClientKey {
        private final String endpointUrl;
        private final String name;
        private final String securityPolicy;
        private final String messageSecurityMode;

        public ClientKey(DefaultConfigurationOpcUa defaultConfig) {
            this.endpointUrl = defaultConfig.getConnection().getEndpointUrl();
            this.name = defaultConfig.getConnection().getName();
            this.securityPolicy = defaultConfig.getAuthentication().getSecurityPolicyUri().name();
            this.messageSecurityMode = defaultConfig.getAuthentication().getMessageSecurityMode().name();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ClientKey clientKey = (ClientKey) o;
            return Objects.equals(endpointUrl, clientKey.endpointUrl) &&
                    Objects.equals(name, clientKey.name) &&
                    Objects.equals(securityPolicy, clientKey.securityPolicy) &&
                    Objects.equals(messageSecurityMode, clientKey.messageSecurityMode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(endpointUrl, name, securityPolicy, messageSecurityMode);
        }
    }

    @Data
    public static class PooledOpcUaClient {
        private final OpcUaClient client;
        private final ClientKey key;
        private final DefaultConfigurationOpcUa defaultConfig;
        private volatile long lastUsed;
        private volatile boolean isValid;

        public PooledOpcUaClient(OpcUaClient client,
                                 DefaultConfigurationOpcUa defaultConfig) {
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

    public Optional<PooledOpcUaClient> obtenerCliente(DefaultConfigurationOpcUa userConfig) {
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

    private Optional<PooledOpcUaClient> crearNuevoCliente(DefaultConfigurationOpcUa defaultConfig) {
        try {
            OpcUaClient client = opcUaConfiguration.createDefaultOpcUaClient(defaultConfig);

            PooledOpcUaClient pooledClient = new PooledOpcUaClient(client, defaultConfig);
            ClientKey key = new ClientKey(defaultConfig);

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
