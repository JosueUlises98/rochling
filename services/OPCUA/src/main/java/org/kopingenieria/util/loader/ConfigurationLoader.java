package org.kopingenieria.util.loader;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kopingenieria.application.mapper.UserConfigurationMapper;
import org.kopingenieria.application.service.files.component.UserConfigFile;
import org.kopingenieria.application.service.files.user.UserFileService;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.model.user.UserOpcUa;
import org.kopingenieria.exception.exceptions.ConfigurationMappingException;
import java.util.Properties;


public class ConfigurationLoader {

    private ConfigurationLoader() {}

    public static UserOpcUa cargarConfiguracion(String configFileName) {
        UserConfigurationMapper userConfigurationMapper = new UserConfigurationMapper();
        try {
             return userConfigurationMapper.mapConfiguration(
                    configFileName,
                    ConfigurationLoader::cargarArchivoConfiguracion
            );
        } catch (ConfigurationMappingException e) {
            throw new RuntimeException("Error al cargar la configuración del cliente", e);
        }
    }

    private static UserConfiguration cargarArchivoConfiguracion(String nombreArchivo) {
        UserFileService userFileService = new UserFileService(new UserConfigFile(new ObjectMapper(),new Properties()));
        return userFileService.getConfiguration(nombreArchivo);
    }
}
