package org.kopingenieria.application.service.pool.clients.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.application.service.configuration.components.UserConfigurationComp;
import org.kopingenieria.domain.enums.security.SecurityPolicyUri;
import org.kopingenieria.domain.model.user.UserConfigurationOpcUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    public static class ClientKey {
        private final String endpointUrl;
        private final String name;
        private final SecurityPolicyUri securityPolicy;
        private final String messageSecurityMode;

        public ClientKey(UserConfigurationOpcUa userConfig) {
            this.endpointUrl = userConfig.getConnection().getEndpointUrl();
            this.name = userConfig.getConnection().getName();
            this.securityPolicy = userConfig.getAuthentication().getSecurityPolicyUri();
            this.messageSecurityMode = userConfig.getAuthentication().getMessageSecurityMode().name();
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
        private final UserConfigurationOpcUa userConfig;
        private volatile long lastUsed;
        private volatile boolean isValid;

        public PooledOpcUaClient(OpcUaClient client,
                                 UserConfigurationOpcUa userConfig) {
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

    public Optional<PooledOpcUaClient> obtenerCliente(UserConfigurationOpcUa userConfig) {
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

    private Optional<PooledOpcUaClient> crearNuevoCliente(UserConfigurationOpcUa userConfig) {
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
