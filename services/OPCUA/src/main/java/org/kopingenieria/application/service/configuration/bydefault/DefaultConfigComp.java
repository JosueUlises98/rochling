package org.kopingenieria.application.service.configuration.bydefault;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.response.configuration.OpcUaConfigResponse;
import org.kopingenieria.application.service.files.bydefault.DefaultFileService;
import org.kopingenieria.application.service.files.component.DefaultConfigFile;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Component( "DefaultConfiguration")
public class DefaultConfigComp{

    private final DefaultSDKComp defaultconfig;
    private final DefaultFileService configfile;

    public DefaultConfigComp() {
        defaultconfig = new DefaultSDKComp();
        configfile = new DefaultFileService(new DefaultConfigFile(new ObjectMapper(), new Properties()));
        configfile.initializeConfiguration();
    }

    public OpcUaConfigResponse createDefaultOpcuaC() {
        try {

            //Creacion del cliente opcua del sdk de org.eclipse.milo
            OpcUaClient uaclient = defaultconfig.createDefaultOpcUaClient();
            //Creacion del archivo de configuracion del cliente opcua
            saveConfiguration();

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

    private void saveConfiguration() throws ConfigurationException {
        org.kopingenieria.config.opcua.bydefault.DefaultConfiguration defaultConfiguration = new org.kopingenieria.config.opcua.bydefault.DefaultConfiguration();
        configfile.saveConfiguration(defaultConfiguration, defaultConfiguration.getFilename());
    }
}
