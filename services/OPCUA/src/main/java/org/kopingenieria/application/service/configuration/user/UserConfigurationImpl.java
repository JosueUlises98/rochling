package org.kopingenieria.application.service.configuration.user;

import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.OpcUaConfigResponse;
import org.kopingenieria.application.service.configuration.components.UserConfigurationComp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Getter
public class UserConfigurationImpl implements UserConfiguration {

    @Autowired
    private UserConfigurationComp userconfig;

    @Override
    public OpcUaConfigResponse createUserOpcUaClient(UserConfigRequest useropcua) {
        if (useropcua == null) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Configuración no puede ser null")
                    .build();
        }
        try {

            OpcUaClient uaclient = userconfig.createUserOpcUaClient(useropcua.getUserConfig());

            return OpcUaConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .clientId(UUID.randomUUID().toString())
                    .endpointUrl(useropcua.getUserConfig().getConnection().getEndpointUrl())
                    .securityMode(useropcua.getUserConfig().getAuthentication().getMessageSecurityMode().toString())
                    .securityPolicy(useropcua.getUserConfig().getAuthentication().getSecurityPolicy().toString())
                    .mensaje("Cliente OPC UA creado correctamente")
                    .build();

        } catch (Exception e) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .miloClient(null)
                    .clientId(useropcua.getUserConfig().getId())
                    .endpointUrl(useropcua.getUserConfig().getConnection().getEndpointUrl())
                    .securityMode(useropcua.getUserConfig().getAuthentication().getMessageSecurityMode().name())
                    .securityPolicy(useropcua.getUserConfig().getAuthentication().getSecurityPolicy().name())
                    .mensaje("Error creando cliente OPC UA")
                    .error(e.getMessage())
                    .build();
        }
    }
}
