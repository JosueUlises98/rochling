package org.kopingenieria.application.mapper;

import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.model.user.*;
import org.kopingenieria.exception.exceptions.ConfigurationMappingException;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Component("userConfigurationMapper")
public class UserConfigurationMapper {

    public UserOpcUa mapConfiguration(
            String filename,
            Function<String, UserConfiguration> configLoader) throws ConfigurationMappingException {
        try {
            UserConfiguration baseConfig = configLoader.apply(filename);
            return mapToOpcUaConfiguration(baseConfig);
        } catch (Exception e) {
            throw new ConfigurationMappingException(
                    "Error en el proceso de carga y mapeo de configuración para: " + filename, e);
        }
    }

    private UserOpcUa mapToOpcUaConfiguration(UserConfiguration userConfig) throws ConfigurationMappingException {
        if (userConfig == null) {
            throw new ConfigurationMappingException("La configuración base es nula");
        }

        return UserOpcUa.builder()
                .name(userConfig.getFilename())
                .connection(mapConnectionConfig(userConfig))
                .encryption(mapEncryptionConfig(userConfig))
                .industrial(mapIndustrialConfig(userConfig))
                .authentication(mapAuthenticationConfig(userConfig))
                .session(mapSessionConfig(userConfig))
                .build();
    }

    private UserConnectionConfiguration mapConnectionConfig(UserConfiguration config) {
        return UserConnectionConfiguration.builder()
                .name(config.getConnection().getName())
                .endpointUrl(config.getConnection().getEndpointUrl())
                .applicationName(config.getConnection().getApplicationName())
                .applicationUri(config.getConnection().getApplicationUri())
                .productUri(config.getConnection().getProductUri())
                .type(config.getConnection().getType())
                .status(ConnectionStatus.UNKNOWN) // Estado inicial por defecto
                .build();
    }

    private UserEncryptionConfiguration mapEncryptionConfig(UserConfiguration config) {
        return UserEncryptionConfiguration.builder()
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

    private UserIndustrialConfiguration mapIndustrialConfig(UserConfiguration config) {
        return UserIndustrialConfiguration.builder()
                .industrialZone(config.getIndustrialConfiguration().getIndustrialZone())
                .equipmentId(config.getIndustrialConfiguration().getEquipmentId())
                .areaId(config.getIndustrialConfiguration().getAreaId())
                .processId(config.getIndustrialConfiguration().getProcessId())
                .operatorName(config.getIndustrialConfiguration().getOperatorName())
                .operatorId(config.getIndustrialConfiguration().getOperatorId())
                .build();
    }

    private UserAuthenticationConfiguration mapAuthenticationConfig(UserConfiguration config) {
        return UserAuthenticationConfiguration.builder()
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

    private UserSessionConfiguration mapSessionConfig(UserConfiguration config) {
        return UserSessionConfiguration.builder()
                .sessionName(config.getSession().getSessionName())
                .serverUri(config.getSession().getServerUri())
                .maxResponseMessageSize(config.getSession().getMaxResponseMessageSize())
                .securityMode(config.getSession().getSecurityMode())
                .securityPolicyUri(config.getSession().getSecurityPolicyUri())
                .clientCertificate(config.getSession().getClientCertificate())
                .serverCertificate(config.getSession().getServerCertificate())
                .localeIds(config.getSession().getLocaleIds())
                .maxChunkCount(config.getSession().getMaxChunkCount())
                .build();
    }

}
