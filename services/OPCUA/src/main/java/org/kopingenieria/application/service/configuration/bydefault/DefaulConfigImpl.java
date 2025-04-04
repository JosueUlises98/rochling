package org.kopingenieria.application.service.configuration.bydefault;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.response.OpcUaConfigResponse;
import org.kopingenieria.application.service.configuration.components.DefaultConfigurationComp;
import org.kopingenieria.domain.model.bydefault.DefaultConfigurationOpcUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class DefaulConfigImpl implements DefaultConfiguration {

    @Autowired
    private DefaultConfigurationComp defaultconfig;

    public OpcUaConfigResponse createDefaultOpcuaC(DefaultConfigurationOpcUa defaultopcua) {
        if (defaultopcua == null) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Configuración no puede ser null")
                    .build();
        }
        try {

            OpcUaClient uaclient = defaultconfig.createDefaultOpcUaClient(defaultopcua);

            return OpcUaConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .clientId(UUID.randomUUID().toString())
                    .endpointUrl(defaultopcua.getConnection().getEndpointUrl())
                    .securityMode(defaultopcua.getEncryption().getMessageSecurityMode().name())
                    .securityPolicy(defaultopcua.getAuthentication().getSecurityPolicyUri().name())
                    .mensaje("Cliente OPC UA creado correctamente")
                    .build();

        } catch (Exception e) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .miloClient(null)
                    .clientId(null)
                    .endpointUrl(defaultopcua.getConnection().getEndpointUrl())
                    .securityMode(defaultopcua.getEncryption().getMessageSecurityMode().name())
                    .securityPolicy(defaultopcua.getAuthentication().getSecurityPolicyUri().name())
                    .mensaje("Error creando cliente OPC UA")
                    .error(e.getMessage())
                    .build();
        }
    }
}
