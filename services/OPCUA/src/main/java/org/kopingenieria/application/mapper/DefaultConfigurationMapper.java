package org.kopingenieria.application.mapper;

import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.model.bydefault.*;
import org.kopingenieria.exception.exceptions.ConfigurationMappingException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component("defaultConfigurationMapper")
public class DefaultConfigurationMapper {

    public DefaultConfigurationOpcUa mapConfiguration(
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

    private DefaultConfigurationOpcUa mapToOpcUaConfiguration(DefaultConfiguration defaultConfig) throws ConfigurationMappingException {
        if (defaultConfig == null) {
            throw new ConfigurationMappingException("La configuración base es nula");
        }

        return DefaultConfigurationOpcUa.builder()
                .connection(mapConnectionConfig(defaultConfig))
                .encryption(mapEncryptionConfig(defaultConfig))
                .industrial(mapIndustrialConfig(defaultConfig))
                .authentication(mapAuthenticationConfig(defaultConfig))
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

    private DefaultEncryptionConfiguration mapEncryptionConfig(DefaultConfiguration config) {
        return DefaultEncryptionConfiguration.builder()
                .securityPolicy(config.getEncryption().getSecurityPolicy())
                .messageSecurityMode(config.getEncryption().getMessageSecurityMode())
                .clientCertificate(config.getEncryption().getClientCertificate())
                .privateKey(config.getEncryption().getPrivateKey())
                .trustedCertificates(config.getEncryption().getTrustedCertificates())
                .keyLength(config.getEncryption().getKeyLength())
                .algorithmName(config.getEncryption().getAlgorithmName())
                .protocolVersion(config.getEncryption().getProtocolVersion())
                .type(config.getEncryption().getType())
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

    private DefaultAuthenticationConfiguration mapAuthenticationConfig(DefaultConfiguration config) {
        return DefaultAuthenticationConfiguration.builder()
                .identityProvider(config.getAuthentication().getIdentityProvider())
                .userName(config.getAuthentication().getUserName())
                .password(config.getAuthentication().getPassword())
                .securityPolicy(config.getAuthentication().getSecurityPolicy())
                .messageSecurityMode(config.getAuthentication().getMessageSecurityMode())
                .certificatePath(config.getAuthentication().getCertificatePath())
                .privateKeyPath(config.getAuthentication().getPrivateKeyPath())
                .trustListPath(config.getAuthentication().getTrustListPath())
                .issuerListPath(config.getAuthentication().getIssuerListPath())
                .revocationListPath(config.getAuthentication().getRevocationListPath())
                .securityPolicyUri(config.getAuthentication().getSecurityPolicyUri())
                .build();
    }
    
    private DefaultSessionConfiguration mapSessionConfig(DefaultConfiguration config) {
        return DefaultSessionConfiguration.builder()
                .sessionName(config.getSession().getSessionName())
                .serverUri(config.getSession().getServerUri())
                .maxResponseMessageSize(config.getSession().getMaxResponseMessageSize())
                .securityMode(MessageSecurityMode.valueOf(config.getSession().getSecurityMode()))
                .securityPolicyUri(config.getSession().getSecurityPolicyUri())
                .clientCertificate(config.getSession().getClientCertificate())
                .serverCertificate(config.getSession().getServerCertificate())
                .localeIds(config.getSession().getLocaleIds())
                .maxChunkCount(config.getSession().getMaxChunkCount())
                .build();
    }
}
