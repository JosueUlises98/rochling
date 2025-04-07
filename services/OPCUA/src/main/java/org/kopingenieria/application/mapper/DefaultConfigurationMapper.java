package org.kopingenieria.application.mapper;

import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.model.bydefault.*;
import org.kopingenieria.exception.exceptions.ConfigurationMappingException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component("defaultConfigurationMapper")
public class DefaultConfigurationMapper {

    public DefaultOpcUa mapConfiguration(
            String filename,
            Function<String, DefaultConfiguration> configLoader) throws ConfigurationMappingException {
        try {
            DefaultConfiguration baseConfig = configLoader.apply(filename);
            return mapToOpcUaConfiguration(baseConfig);
        } catch (Exception e) {
            throw new ConfigurationMappingException(
                    "Error en el proceso de carga y mapeo de configuración para: " + filename, e);
        }
    }

    private DefaultOpcUa mapToOpcUaConfiguration(DefaultConfiguration defaultConfig) throws ConfigurationMappingException {
        if (defaultConfig == null) {
            throw new ConfigurationMappingException("La configuración base es nula");
        }

        return DefaultOpcUa.builder()
                .connection(mapConnectionConfig(defaultConfig))
                .industrial(mapIndustrialConfig(defaultConfig))
                .session(mapSessionConfig(defaultConfig))
                .build();
    }

    private DefaultConnectionConfiguration mapConnectionConfig(DefaultConfiguration config) {
        return DefaultConnectionConfiguration.builder()
                .name(config.getConnection().getName())
                .endpointUrl(config.getConnection().getEndpointUrl())
                .applicationName(config.getConnection().getApplicationName())
                .applicationUri(config.getConnection().getApplicationUri())
                .productUri(config.getConnection().getProductUri())
                .type(config.getConnection().getType())
                .status(ConnectionStatus.UNKNOWN) // Estado inicial por defecto
                .build();
    }

    private DefaultIndustrialConfiguration mapIndustrialConfig(DefaultConfiguration config) {
        return DefaultIndustrialConfiguration.builder()
                .industrialZone(config.getIndustrialConfiguration().getIndustrialZone())
                .equipmentId(config.getIndustrialConfiguration().getEquipmentId())
                .areaId(config.getIndustrialConfiguration().getAreaId())
                .processId(config.getIndustrialConfiguration().getProcessId())
                .operatorName(config.getIndustrialConfiguration().getOperatorName())
                .operatorId(config.getIndustrialConfiguration().getOperatorId())
                .build();
    }
    
    private DefaultSessionConfiguration mapSessionConfig(DefaultConfiguration config) {
        return DefaultSessionConfiguration.builder()
                .sessionName(config.getSession().getSessionName())
                .serverUri(config.getSession().getServerUri())
                .maxResponseMessageSize(config.getSession().getMaxResponseMessageSize())
                .localeIds(config.getSession().getLocaleIds())
                .maxChunkCount(config.getSession().getMaxChunkCount())
                .build();
    }
}
