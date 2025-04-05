package org.kopingenieria.application.service.pool.clients.bydefault;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;
import org.kopingenieria.logging.model.LogMethod;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class OpcUaDefaultPoolManager {

    @Autowired
    private OpcUaDefaultPool clientPool;

    private final Map<String, OpcUaDefaultPool.PooledOpcUaClient> managedClients;
    private final Map<String, AtomicInteger> clientUsageCounter;
    private final Map<String, Long> clientLastErrors;

    public OpcUaDefaultPoolManager() {
        this.managedClients = new ConcurrentHashMap<>();
        this.clientUsageCounter = new ConcurrentHashMap<>();
        this.clientLastErrors = new ConcurrentHashMap<>();
    }

    @PostConstruct
    @Auditable(
            value = "inicializarPool",
            type = AuditEntryType.CREATE,
            includeParams = false,
            description = "Método para inicializar el pool de clientes en OpcUaPoolManager"
    )
    @LogSystemEvent(event = "Inicializacion del pool de clientes", description = "Inicializando OpcUaPoolManager con Pool de Clientes...")
    private void inicializar() {
    }

    @Auditable(
            value = "obtenerCliente",
            type = AuditEntryType.READ,
            description = "Método para obtener cliente opcua"
    )
    @LogMethod(description = "Inicializando OpcUaPoolManager con Pool de Clientes...",operation = "obtenerCliente")
    public Optional<OpcUaDefaultPool.PooledOpcUaClient> obtenerCliente(
            DefaultOpcUa userConfig) {
        String connectionId = generarIdConexion(userConfig);

        try {

            Optional<OpcUaDefaultPool.PooledOpcUaClient> pooledClient = clientPool.obtenerCliente(userConfig);

            pooledClient.ifPresent(client -> {
                managedClients.put(connectionId, client);
                registrarUsoCliente(connectionId);
            });
            return pooledClient;
        } catch (Exception e) {
            registrarError(connectionId);
            return Optional.empty();
        }
    }

    @Auditable(
            value = "liberarCliente",
            type = AuditEntryType.OPERATION,
            description = "Método para liberar cliente"
    )
    @LogMethod(description = "Liberando cliente opcua del pool",operation = "liberarCliente")
    public void liberarCliente(OpcUaDefaultPool.PooledOpcUaClient pooledClient) {
        String connectionId = encontrarIdConexion(pooledClient);
        if (connectionId != null) {
            managedClients.remove(connectionId);
            clientPool.liberarCliente(pooledClient);
        }
    }

    @Auditable(
            value = "reconectarCliente",
            type = AuditEntryType.OPERATION,
            description = "Método para reconectar un cliente opcua"
    )
    @LogMethod(description = "Reconectando un cliente opcua",operation = "reconectarCliente")
    public void reconectarCliente(OpcUaDefaultPool.PooledOpcUaClient pooledClient) {
        String connectionId = encontrarIdConexion(pooledClient);
        if (connectionId != null) {
            try {
                pooledClient.getClient().disconnect();
                pooledClient.getClient().connect().get();
            } catch (Exception e) {
                registrarError(connectionId);
            }
        }
    }

    @Auditable(
            value = "obtenerEstadisticasCliente",
            type = AuditEntryType.READ,
            description = "Método para obtener estadisticas del cliente"
    )
    @LogMethod(description = "Obteniendo estadisticas del cliente opcua",operation = "obtenerEstadisticasCliente")
    public Map<String, Object> obtenerEstadisticasCliente(OpcUaDefaultPool.PooledOpcUaClient pooledClient) {
        Map<String, Object> estadisticas = new HashMap<>();
        String connectionId = encontrarIdConexion(pooledClient);

        if (connectionId != null) {
            estadisticas.put("usos", clientUsageCounter.getOrDefault(connectionId, new AtomicInteger(0)).get());
            estadisticas.put("ultimoError", clientLastErrors.getOrDefault(connectionId, 0L));
            estadisticas.put("ultimoUso", pooledClient.getLastUsed());
            estadisticas.put("estadoConexion", pooledClient.getDefaultConfig().getConnection().getStatus());
            estadisticas.put("configuracion", pooledClient.getDefaultConfig());
        }

        return estadisticas;
    }

    @Auditable(
            value = "monitorizarClientes",
            type = AuditEntryType.OPERATION,
            description = "Método para monitorizar clientes opcua"
    )
    @LogSystemEvent(description = "Monitorizando clientes opcua...",event = "Monitorizacion de clientes opcua")
    @Scheduled(fixedRate = 300000) // Cada 5 minutos
    protected void monitorizarClientes() {
        managedClients.forEach((connectionId, pooledClient) -> {
            try {
                if (!pooledClient.isConnected()) {
                    reconectarCliente(pooledClient);
                }
            } catch (Exception e) {
                registrarError(connectionId);
            }
        });
    }

    private String generarIdConexion(DefaultOpcUa config) {
        return String.format("%s_%s_%s",
                config.getConnection().getEndpointUrl(),
                config.getConnection().getName(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    private String encontrarIdConexion(OpcUaDefaultPool.PooledOpcUaClient pooledClient) {
        return managedClients.entrySet().stream()
                .filter(entry -> entry.getValue().equals(pooledClient))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private void registrarUsoCliente(String connectionId) {
        clientUsageCounter.computeIfAbsent(connectionId, k -> new AtomicInteger(0))
                .incrementAndGet();
    }

    private void registrarError(String connectionId) {
        clientLastErrors.put(connectionId, System.currentTimeMillis());
    }

    @PreDestroy
    @Auditable(value = "limpiar",type = AuditEntryType.DELETE,description = "Limpieza del OpcUaPoolManager")
    @LogSystemEvent(description = "Destruyendo OpcUaPoolManager...",event = "Destruccion de OpcUaPoolManager")
    private void limpiar() {
        managedClients.forEach((id, client) -> clientPool.liberarCliente(client));
        managedClients.clear();
        clientUsageCounter.clear();
        clientLastErrors.clear();
    }
}
