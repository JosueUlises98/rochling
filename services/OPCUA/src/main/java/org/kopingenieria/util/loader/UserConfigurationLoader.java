package org.kopingenieria.util.loader;

import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionStatus;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.security.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class UserConfigurationLoader {

    private UserConfiguration mapPropertiesToConfig(Properties props) {
        return UserConfiguration.builder()
                .id(props.getProperty("opcua.user.id"))
                .filename(props.getProperty("opcua.user.filename"))
                .description(props.getProperty("opcua.user.description"))
                .enabled(Boolean.valueOf(props.getProperty("opcua.user.enabled", "true")))
                .version(Long.valueOf(props.getProperty("opcua.user.version", "1")))
                .connection(mapConnection(props))
                .authentication(mapAuthentication(props))
                .encryption(mapEncryption(props))
                .session(mapSession(props))
                .industrialConfiguration(mapIndustrialConfig(props))
                .build();
    }

    private UserConfiguration.Connection mapConnection(Properties props) {
        return UserConfiguration.Connection.builder()
                .name(props.getProperty("opcua.user.connection.name"))
                .endpointUrl(props.getProperty("opcua.user.connection.endpointUrl"))
                .applicationName(props.getProperty("opcua.user.connection.applicationName"))
                .applicationUri(props.getProperty("opcua.user.connection.applicationUri"))
                .productUri(props.getProperty("opcua.user.connection.productUri"))
                .type(ConnectionType.valueOf(props.getProperty("opcua.user.connection.type", "TCP")))
                .status(ConnectionStatus.valueOf(props.getProperty("opcua.user.connection.status", "DISCONNECTED")))
                .build();
    }

    private UserConfiguration.Authentication mapAuthentication(Properties props) {
        return UserConfiguration.Authentication.builder()
                .identityProvider(IdentityProvider.valueOf(
                        props.getProperty("opcua.user.authentication.identityProvider", "ANONYMOUS")))
                .userName(props.getProperty("opcua.user.authentication.userName"))
                .password(props.getProperty("opcua.user.authentication.password"))
                .securityPolicy(SecurityPolicy.valueOf(
                        props.getProperty("opcua.user.authentication.securityPolicy", "NONE")))
                .messageSecurityMode(MessageSecurityMode.valueOf(
                        props.getProperty("opcua.user.authentication.messageSecurityMode", "NONE")))
                .certificatePath(props.getProperty("opcua.user.authentication.certificatePath"))
                .privateKeyPath(props.getProperty("opcua.user.authentication.privateKeyPath"))
                .trustListPath(props.getProperty("opcua.user.authentication.trustListPath"))
                .issuerListPath(props.getProperty("opcua.user.authentication.issuerListPath"))
                .revocationListPath(props.getProperty("opcua.user.authentication.revocationListPath"))
                .securityPolicyUri(SecurityPolicyUri.valueOf(
                        props.getProperty("opcua.user.authentication.securityPolicyUri", "NONE")))
                .build();
    }

    private UserConfiguration.Session mapSession(Properties props) {
        return UserConfiguration.Session.builder()
                .sessionName(props.getProperty("opcua.user.session.sessionName"))
                .serverUri(props.getProperty("opcua.user.session.serverUri"))
                .maxResponseMessageSize(Long.valueOf(
                        props.getProperty("opcua.user.session.maxResponseMessageSize", "0")))
                .securityMode(MessageSecurityMode.valueOf(
                        props.getProperty("opcua.user.session.securityMode", "NONE")))
                .securityPolicyUri(SecurityPolicyUri.valueOf(
                        props.getProperty("opcua.user.session.securityPolicyUri", "NONE")))
                .clientCertificate(props.getProperty("opcua.user.session.clientCertificate"))
                .serverCertificate(props.getProperty("opcua.user.session.serverCertificate"))
                .maxChunkCount(Integer.valueOf(
                        props.getProperty("opcua.user.session.maxChunkCount", "0")))
                .build();
    }

    private UserConfiguration.IndustrialConfiguration mapIndustrialConfig(Properties props) {
        return UserConfiguration.IndustrialConfiguration.builder()
                .industrialZone(props.getProperty("opcua.user.industrial.zone"))
                .equipmentId(props.getProperty("opcua.user.industrial.equipmentId"))
                .areaId(props.getProperty("opcua.user.industrial.areaId"))
                .processId(props.getProperty("opcua.user.industrial.processId"))
                .operatorName(props.getProperty("opcua.user.industrial.operatorName"))
                .operatorId(props.getProperty("opcua.user.industrial.operatorId"))
                .build();
    }

    private UserConfiguration.Encryption mapEncryption(Properties props) {
        return UserConfiguration.Encryption.builder()
                .securityPolicy(SecurityPolicy.valueOf(
                        props.getProperty("opcua.user.encryption.securityPolicy", "NONE")))
                .messageSecurityMode(MessageSecurityMode.valueOf(
                        props.getProperty("opcua.user.encryption.messageSecurityMode", "NONE")))
                .keyLength(Integer.valueOf(
                        props.getProperty("opcua.user.encryption.keyLength", "128")))
                .algorithmName(EncryptionAlgorithm.valueOf(
                        props.getProperty("opcua.user.encryption.algorithmName", "AES")))
                .protocolVersion(props.getProperty("opcua.user.encryption.protocolVersion"))
                .type(props.getProperty("opcua.user.encryption.type"))
                .build();
    }

    public UserConfiguration loadConfiguration(String filePath) {
        try (InputStream is = new FileInputStream(filePath)) {
            Properties props = new Properties();
            props.load(is);
            return mapPropertiesToConfig(props);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar la configuración: " + filePath, e);
        }
    }
}
