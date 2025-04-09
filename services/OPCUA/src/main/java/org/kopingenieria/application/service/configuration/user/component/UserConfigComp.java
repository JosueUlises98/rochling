package org.kopingenieria.application.service.configuration.user.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.response.configuration.user.UserConfigResponse;
import org.kopingenieria.application.service.files.component.UserConfigFile;
import org.kopingenieria.application.service.files.user.UserFileService;
import org.kopingenieria.domain.model.user.UserOpcUa;
import org.kopingenieria.util.helper.ConfigurationHelper;
import org.springframework.stereotype.Component;
import java.util.*;
import java.util.stream.Collectors;

@Component("UserConfiguration")
@Getter
public class UserConfigComp {

    private final UserSDKComp sdkconfig;
    private final UserFileService configfile;
    private Map<UserOpcUa, OpcUaClient> mapclients;
    private List<Map<UserOpcUa, OpcUaClient>> clients;

    public UserConfigComp() {
        configfile = new UserFileService(new UserConfigFile(new ObjectMapper(), new Properties()));
        sdkconfig = new UserSDKComp();
        configfile.initializeSystem();
        mapclients = new HashMap<>();
        clients = new ArrayList<>();
    }

    // CREATE
    public UserConfigResponse createUserOpcUaClient(UserConfigRequest useropcua) {
        if (useropcua == null) {
            return UserConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Configuración no puede ser null")
                    .build();
        }
        try {

            OpcUaClient uaclient = sdkconfig.createUserOpcUaClient(useropcua.getUserConfig());
            saveConfiguration(useropcua);
            mapclients.put(useropcua.getUserConfig(), uaclient);
            clients.add(new HashMap<>(mapclients));

            return UserConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(uaclient)
                    .clientId(useropcua.getUserConfig().getId())
                    .endpointUrl(useropcua.getUserConfig().getConnection().getEndpointUrl())
                    .securityMode(useropcua.getUserConfig().getAuthentication().getMessageSecurityMode().toString())
                    .securityPolicy(useropcua.getUserConfig().getAuthentication().getSecurityPolicy().toString())
                    .mensaje("Cliente OPC UA creado correctamente")
                    .build();
        } catch (Exception e) {
            return UserConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error creando cliente OPC UA")
                    .error(e.getMessage())
                    .build();
        }
    }

    // READ
    public UserConfigResponse getUserConfiguration(String clientId) {
        try {

            Optional<UserOpcUa> userConfig = mapclients.keySet()
                    .stream()
                    .filter(config -> config.getId().equals(clientId))
                    .findFirst();

            if (userConfig.isEmpty()) {
                return UserConfigResponse.builder()
                        .exitoso(false)
                        .mensaje("Configuración no encontrada")
                        .build();
            }

            UserOpcUa config = userConfig.get();
            OpcUaClient client = mapclients.get(config);

            return UserConfigResponse.builder()
                    .exitoso(true)
                    .miloClient(client)
                    .client(config)
                    .clientId(config.getId())
                    .endpointUrl(config.getConnection().getEndpointUrl())
                    .securityMode(config.getAuthentication().getMessageSecurityMode().toString())
                    .securityPolicy(config.getAuthentication().getSecurityPolicy().toString())
                    .mensaje("Configuración recuperada exitosamente")
                    .build();

        } catch (Exception e) {
            return UserConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error al recuperar configuración")
                    .error(e.getMessage())
                    .build();
        }
    }

    // DELETE
    public UserConfigResponse deleteUserConfiguration(String clientId) {
        try {

            Optional<UserOpcUa> configToRemove = mapclients.keySet()
                    .stream()
                    .filter(config -> config.getId().equals(clientId))
                    .findFirst();

            if (configToRemove.isEmpty()) {

                return UserConfigResponse.builder()
                        .exitoso(false)
                        .mensaje("Configuración no encontrada para eliminar")
                        .build();
            }

            OpcUaClient client = mapclients.remove(configToRemove.get());
            if (client != null) {
                client.disconnect().get();
            }

            configfile.deleteConfiguration(configToRemove.get().getName());

            return UserConfigResponse.builder()
                    .exitoso(true)
                    .mensaje("Configuración eliminada correctamente")
                    .build();

        } catch (Exception e) {
            return UserConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error al eliminar configuración")
                    .error(e.getMessage())
                    .build();
        }
    }

    // READ ALL
    public List<UserConfigResponse> getAllUserConfigurations() {
        try {
            return mapclients.entrySet().stream()
                    .map(entry -> UserConfigResponse.builder()
                            .exitoso(true)
                            .miloClient(entry.getValue())
                            .client(entry.getKey())
                            .clientId(entry.getKey().getId())
                            .endpointUrl(entry.getKey().getConnection().getEndpointUrl())
                            .securityMode(entry.getKey().getAuthentication().getMessageSecurityMode().toString())
                            .securityPolicy(entry.getKey().getAuthentication().getSecurityPolicy().toString())
                            .mensaje("Configuración recuperada")
                            .build())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            return List.of(UserConfigResponse.builder()
                    .exitoso(false)
                    .mensaje("Error al recuperar configuraciones")
                    .error(e.getMessage())
                    .build());
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
