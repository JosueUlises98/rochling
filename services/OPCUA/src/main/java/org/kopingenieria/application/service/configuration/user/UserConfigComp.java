package org.kopingenieria.application.service.configuration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.OpcUaConfigResponse;
import org.kopingenieria.application.service.files.component.UserConfigFile;
import org.kopingenieria.application.service.files.user.UserFileService;
import org.kopingenieria.util.helper.ConfigurationHelper;
import org.springframework.stereotype.Component;
import java.util.Properties;

@Component("UserConfiguration")
public class UserConfigComp {

    private final UserSDKComp sdkconfig;
    private final UserFileService configfile;

    public UserConfigComp() {
        configfile = new UserFileService(new UserConfigFile(new ObjectMapper(), new Properties()));
        sdkconfig = new UserSDKComp();
        configfile.initializeSystem();
    }

    public OpcUaConfigResponse createUserOpcUaClient(UserConfigRequest useropcua) {
        if (useropcua == null) {
            return OpcUaConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Configuración no puede ser null")
                    .build();
        }
        try {

            //Creacion del cliente opcua del sdk de org.eclipse.milo
            OpcUaClient uaclient = sdkconfig.createUserOpcUaClient(useropcua.getUserConfig());
            //Creacion del archivo de configuracion del cliente opcua
            saveConfiguration(useropcua);

            return OpcUaConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .clientId(useropcua.getUserConfig().getId())
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

    private void saveConfiguration(UserConfigRequest userconfig) {
        configfile.saveConfiguration(org.kopingenieria.config.opcua.user.UserConfiguration.builder()
                .id(userconfig.getUserConfig().getId())
                .filename(userconfig.getUserConfig().getName())
                .description("Configuracion de cliente OPC UA del usuario")
                .enabled(true)
                .version(ConfigurationHelper.getNextVersion())
                .connection(org.kopingenieria.config.opcua.user.UserConfiguration.Connection.builder()
                        .name(userconfig.getUserConfig().getConnection().getName())
                        .endpointUrl(userconfig.getUserConfig().getConnection().getEndpointUrl())
                        .applicationName(userconfig.getUserConfig().getConnection().getApplicationName())
                        .applicationUri(userconfig.getUserConfig().getConnection().getApplicationUri())
                        .productUri(userconfig.getUserConfig().getConnection().getProductUri())
                        .type(userconfig.getUserConfig().getConnection().getType())
                        .status(userconfig.getUserConfig().getConnection().getStatus())
                        .build())
                .authentication(org.kopingenieria.config.opcua.user.UserConfiguration.Authentication.builder()
                        .identityProvider(userconfig.getUserConfig().getAuthentication().getIdentityProvider())
                        .userName(userconfig.getUserConfig().getAuthentication().getUserName())
                        .password(userconfig.getUserConfig().getAuthentication().getPassword())
                        .securityPolicy(userconfig.getUserConfig().getAuthentication().getSecurityPolicy())
                        .messageSecurityMode(userconfig.getUserConfig().getAuthentication().getMessageSecurityMode())
                        .certificatePath(userconfig.getUserConfig().getAuthentication().getCertificatePath())
                        .privateKeyPath(userconfig.getUserConfig().getAuthentication().getPrivateKeyPath())
                        .trustListPath(userconfig.getUserConfig().getAuthentication().getTrustListPath())
                        .issuerListPath(userconfig.getUserConfig().getAuthentication().getIssuerListPath())
                        .revocationListPath(userconfig.getUserConfig().getAuthentication().getRevocationListPath())
                        .securityPolicyUri(userconfig.getUserConfig().getAuthentication().getSecurityPolicyUri())
                        .build())
                .encryption(org.kopingenieria.config.opcua.user.UserConfiguration.Encryption.builder()
                        .securityPolicy(userconfig.getUserConfig().getEncryption().getSecurityPolicy())
                        .messageSecurityMode(userconfig.getUserConfig().getEncryption().getMessageSecurityMode())
                        .clientCertificate(userconfig.getUserConfig().getEncryption().getClientCertificate())
                        .privateKey(userconfig.getUserConfig().getEncryption().getPrivateKey())
                        .trustedCertificates(userconfig.getUserConfig().getEncryption().getTrustedCertificates())
                        .keyLength(userconfig.getUserConfig().getEncryption().getKeyLength())
                        .algorithmName(userconfig.getUserConfig().getEncryption().getAlgorithmName())
                        .protocolVersion(userconfig.getUserConfig().getEncryption().getProtocolVersion())
                        .type(userconfig.getUserConfig().getEncryption().getType())
                        .build())
                .session(org.kopingenieria.config.opcua.user.UserConfiguration.Session.builder()
                        .sessionName(userconfig.getUserConfig().getSession().getSessionName())
                        .serverUri(userconfig.getUserConfig().getSession().getServerUri())
                        .maxResponseMessageSize(userconfig.getUserConfig().getSession().getMaxResponseMessageSize())
                        .securityMode(userconfig.getUserConfig().getSession().getSecurityMode())
                        .securityPolicyUri(userconfig.getUserConfig().getSession().getSecurityPolicyUri())
                        .clientCertificate(userconfig.getUserConfig().getSession().getClientCertificate())
                        .serverCertificate(userconfig.getUserConfig().getSession().getServerCertificate())
                        .localeIds(userconfig.getUserConfig().getSession().getLocaleIds())
                        .maxChunkCount(userconfig.getUserConfig().getSession().getMaxChunkCount())
                        .build())
                .industrialConfiguration(org.kopingenieria.config.opcua.user.UserConfiguration.IndustrialConfiguration
                        .builder().industrialZone(userconfig.getUserConfig().getIndustrial().getIndustrialZone())
                        .equipmentId(userconfig.getUserConfig().getIndustrial().getEquipmentId())
                        .areaId(userconfig.getUserConfig().getIndustrial().getAreaId())
                        .processId(userconfig.getUserConfig().getIndustrial().getProcessId())
                        .operatorName(userconfig.getUserConfig().getIndustrial().getOperatorName())
                        .operatorId(userconfig.getUserConfig().getIndustrial().getOperatorId())
                        .build())
                .build(), userconfig.getUserConfig().getName());
    }
}
