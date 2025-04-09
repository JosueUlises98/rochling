package org.kopingenieria.application.service.configuration.bydefault;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.domain.enums.communication.SessionStatus;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.model.bydefault.DefaultConnectionConfiguration;
import org.kopingenieria.domain.model.bydefault.DefaultIndustrialConfiguration;
import org.kopingenieria.domain.model.bydefault.DefaultOpcUa;
import org.kopingenieria.domain.model.bydefault.DefaultSessionConfiguration;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;
import org.springframework.stereotype.Component;


import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Component( "DefaultConfiguration" )
@Getter
@NoArgsConstructor
public class DefaultSDKComp {

    private DefaultOpcUa defaultclient;

    public OpcUaClient createDefaultOpcUaClient() throws OpcUaConfigurationException {
        try {
            DefaultConfiguration defconfig = new DefaultConfiguration();
            DefaultOpcUa defaultopcua = DefaultOpcUa.builder()
                    .name("Default_Client_OPCUA")
                    .connection(DefaultConnectionConfiguration.builder()
                            .name(defconfig.getConnection().getName())
                    .endpointUrl(defconfig.getConnection().getEndpointUrl())
                            .applicationName(defconfig.getConnection().getApplicationName())
                            .applicationUri(defconfig.getConnection().getApplicationUri())
                            .type(defconfig.getConnection().getType())
                            .status(defconfig.getConnection().getStatus())
                            .build())
                    .session(DefaultSessionConfiguration.builder()
                            .sessionName(defconfig.getSession().getSessionName())
                            .serverUri(defconfig.getSession().getServerUri())
                            .maxResponseMessageSize(defconfig.getSession().getMaxResponseMessageSize())
                            .localeIds(defconfig.getSession().getLocaleIds())
                            .maxChunkCount(defconfig.getSession().getMaxChunkCount())
                            .sessionStatus(SessionStatus.INACTIVE)
                            .build())
                    .industrial(DefaultIndustrialConfiguration.builder()
                            .industrialZone(defconfig.getIndustrialConfiguration().getIndustrialZone())
                            .equipmentId(defconfig.getIndustrialConfiguration().getEquipmentId())
                            .areaId(defconfig.getIndustrialConfiguration().getAreaId())
                            .processId(defconfig.getIndustrialConfiguration().getProcessId())
                            .operatorName(defconfig.getIndustrialConfiguration().getOperatorName())
                            .operatorId(defconfig.getIndustrialConfiguration().getOperatorId())
                            .build())
                    .build();

            defaultclient = defaultopcua;

            // Crear builder de configuración
            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();

            // 1. Configuración de conexión
            configurarConexion(config, defaultopcua);

            // 2. Configuración de sesión
            configurarSesion(config, defaultopcua);

            // 3. Crear cliente

            return OpcUaClient.create(config.build());

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error creando cliente OPC UA", e);
        }
    }

    private void configurarConexion(OpcUaClientConfigBuilder config,
                                    DefaultOpcUa userConfig) {
        EndpointDescription endpoint = EndpointDescription.builder()
                .endpointUrl(userConfig.getConnection().getEndpointUrl())
                .securityPolicyUri(null)
                .securityMode(null)
                .build();

        config.setEndpoint(endpoint)
                .setApplicationName(LocalizedText.english(
                        userConfig.getConnection().getApplicationName()))
                .setApplicationUri(userConfig.getConnection().getApplicationUri())
                .setProductUri(userConfig.getConnection().getProductUri())
                .setRequestTimeout(uint(String.valueOf(userConfig.getConnection().getTimeout())));
    }

    private void configurarSesion(OpcUaClientConfigBuilder config,
                                  DefaultOpcUa userConfig) {
        config.setSessionName(() -> userConfig.getSession().getSessionName())
                .setSessionTimeout(uint(String.valueOf(userConfig.getSession().getTimeout())))
                .setMaxResponseMessageSize(uint(
                        userConfig.getSession().getMaxResponseMessageSize()))
                .setSessionLocaleIds((String[]) userConfig.getSession().getLocaleIds().stream()
                        .map(LocaleIds::getLocaleId)
                        .toArray());
    }
}
