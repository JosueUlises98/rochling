package org.kopingenieria.application.service.configuration.user;

import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.response.OpcUaConfigResponse;
import org.kopingenieria.application.service.configuration.components.UserConfigurationComp;
import org.kopingenieria.domain.model.user.UserConfigurationOpcUa;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@Getter
public class UserConfigurationImpl implements UserConfiguration {

    @Autowired
    private UserConfigurationComp userconfig;

    @Override
    public OpcUaConfigResponse createUserOpcUaClient(UserConfigurationOpcUa useropcua) {
        if (useropcua == null) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Configuración no puede ser null")
                    .build();
        }
        try {

            OpcUaClient uaclient = userconfig.createUserOpcUaClient(useropcua);

            return OpcUaConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .clientId(UUID.randomUUID().toString())
                    .endpointUrl(useropcua.getConnection().getEndpointUrl())
                    .securityMode(useropcua.getEncryption().getMessageSecurityMode().name())
                    .securityPolicy(useropcua.getAuthentication().getSecurityPolicyUri().name())
                    .mensaje("Cliente OPC UA creado correctamente")
                    .build();

        } catch (Exception e) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .miloClient(null)
                    .clientId(null)
                    .endpointUrl(useropcua.getConnection().getEndpointUrl())
                    .securityMode(useropcua.getEncryption().getMessageSecurityMode().name())
                    .securityPolicy(useropcua.getAuthentication().getSecurityPolicyUri().name())
                    .mensaje("Error creando cliente OPC UA")
                    .error(e.getMessage())
                    .build();
        }
    }
}
