package org.kopingenieria.application.service.configuration.bydefault;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.response.configuration.OpcUaConfigResponse;
import org.kopingenieria.application.service.configuration.components.DefaultConfigurationComp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DefaulConfigImpl implements DefaultConfiguration {

    @Autowired
    private DefaultConfigurationComp defaultconfig;

    public OpcUaConfigResponse createDefaultOpcuaC() {
        try {

            OpcUaClient uaclient = defaultconfig.createDefaultOpcUaClient();

            return OpcUaConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .clientId(defaultconfig.getDefaultclient().getId())
                    .endpointUrl(defaultconfig.getDefaultclient().getConnection().getEndpointUrl())
                    .securityMode(null)
                    .securityPolicy(null)
                    .mensaje("Cliente OPC UA creado correctamente")
                    .build();

        } catch (Exception e) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .miloClient(null)
                    .clientId(defaultconfig.getDefaultclient().getId())
                    .endpointUrl(defaultconfig.getDefaultclient().getConnection().getEndpointUrl())
                    .securityMode(null)
                    .securityPolicy(null)
                    .mensaje("Error creando cliente OPC UA")
                    .error(e.getMessage())
                    .build();
        }
    }
}
