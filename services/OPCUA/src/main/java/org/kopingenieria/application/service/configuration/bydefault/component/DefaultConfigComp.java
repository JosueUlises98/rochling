package org.kopingenieria.application.service.configuration.bydefault.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.response.configuration.bydefault.DefaultConfigResponse;
import org.kopingenieria.application.service.files.bydefault.DefaultFileService;
import org.kopingenieria.application.service.files.component.DefaultConfigFile;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component( "DefaultConfiguration")
@Getter
public class DefaultConfigComp{

    private final DefaultSDKComp defaultconfig;
    private final DefaultFileService configfile;
    private Map<DefaultOpcUa, OpcUaClient> mapclients;
    private List<Map<DefaultOpcUa, OpcUaClient>> clients;

    public DefaultConfigComp() {
        defaultconfig = new DefaultSDKComp();
        configfile = new DefaultFileService(new DefaultConfigFile(new ObjectMapper(), new Properties()));
        configfile.initializeConfiguration();
        mapclients = new HashMap<>();
        clients = new ArrayList<>();
    }

    // CREATE
    public DefaultConfigResponse createDefaultOpcuaC() {
        try {

            OpcUaClient uaclient = defaultconfig.createDefaultOpcUaClient();
            saveConfiguration();
            mapclients.put(defaultconfig.getDefaultclient(), uaclient);
            clients.add(new HashMap<>(mapclients));

            return DefaultConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .client(defaultconfig.getDefaultclient())
                    .clientId(defaultconfig.getDefaultclient().getId())
                    .endpointUrl(defaultconfig.getDefaultclient().getConnection().getEndpointUrl())
                    .mensaje("Cliente OPC UA creado correctamente")
                    .build();

        } catch (Exception e) {
            return DefaultConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error creando cliente OPC UA")
                    .error(e.getMessage())
                    .build();
        }
    }

    // READ
    public DefaultConfigResponse readDefaultConfiguration(String clientId) {
        try {

            Optional<DefaultOpcUa> client = mapclients.keySet()
                    .stream()
                    .filter(c -> c.getId().equals(clientId))
                    .findFirst();

            if (client.isEmpty()) {
                return DefaultConfigResponse.builder()
                        .exitoso(false)
                        .mensaje("Configuración no encontrada")
                        .build();
            }

            return DefaultConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(mapclients.get(client.get()))
                    .client(client.get())
                    .clientId(client.get().getId())
                    .endpointUrl(client.get().getConnection().getEndpointUrl())
                    .mensaje("Configuración recuperada exitosamente")
                    .build();

        } catch (Exception e) {
            return DefaultConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error al leer configuración")
                    .error(e.getMessage())
                    .build();
        }
    }

    // DELETE
    public DefaultConfigResponse deleteDefaultConfiguration(String clientId) {
        try {

            Optional<DefaultOpcUa> clientToRemove = mapclients.keySet()
                    .stream()
                    .filter(c -> c.getId().equals(clientId))
                    .findFirst();

            if (clientToRemove.isEmpty()) {
                return DefaultConfigResponse.builder()
                        .exitoso(false)
                        .mensaje("Configuración no encontrada para eliminar")
                        .build();
            }

            OpcUaClient removedClient = mapclients.remove(clientToRemove.get());
            if (removedClient != null) {
                removedClient.disconnect().get(); // Desconectar cliente de forma segura
            }

            configfile.deleteConfiguration(clientToRemove.get().getName());

            return DefaultConfigResponse.builder()
                    .exitoso(true)
                    .mensaje("Configuración eliminada correctamente")
                    .build();

        } catch (Exception e) {
            return DefaultConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error al eliminar configuración")
                    .error(e.getMessage())
                    .build();
        }
    }

    // READ ALL
    public List<DefaultConfigResponse> getAllConfigurations() {
        try {
            return mapclients.entrySet().stream()
                    .map(entry -> DefaultConfigResponse.builder()
                            .exitoso(true)
                            .miloClient(entry.getValue())
                            .clientId(entry.getKey().getId())
                            .endpointUrl(entry.getKey().getConnection().getEndpointUrl())
                            .mensaje("Configuración recuperada")
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of(DefaultConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error al recuperar configuraciones")
                    .error(e.getMessage())
                    .build());
        }
    }

    private void saveConfiguration() throws ConfigurationException {
        DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        configfile.saveConfiguration(defaultConfiguration, defaultConfiguration.getFilename());
    }
}
