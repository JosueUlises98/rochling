package org.kopingenieria.application.service.opcua.workflow.user;

import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.kopingenieria.domain.model.user.UserConfigurationOpcUa;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;
import org.kopingenieria.util.security.user.UserCertificateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Service
@Getter
public class UserConfigurationImpl implements UserConfiguration {

    @Autowired
    private UserCertificateManager certificateManager;

    @Override
    public OpcUaClient createUserOpcUaClient(UserConfigurationOpcUa useropcua) throws OpcUaConfigurationException {
        try {
            // Inicializar certificados
            certificateManager.configurarCertificados(useropcua);

            // Crear builder de configuración
            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();

            // 1. Configuración de conexión
            configurarConexion(config, useropcua);

            // 2. Configuración de autenticación
            configurarAutenticacion(config, useropcua);

            // 3. Configuración de encriptacion(Seguridad)
            certificateManager.aplicarConfiguracionSeguridad(config,useropcua);

            // 4. Configuración de sesión
            configurarSesion(config, useropcua);

            // 5. Crear cliente

            return OpcUaClient.create(config.build());

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error creando cliente OPC UA", e);
        }
    }

    private void configurarConexion(OpcUaClientConfigBuilder config,
                                    UserConfigurationOpcUa userConfig) {
        EndpointDescription endpoint = EndpointDescription.builder()
                .endpointUrl(userConfig.getConnection().getEndpointUrl())
                .securityPolicyUri(SecurityPolicy.valueOf(
                        String.valueOf(userConfig.getAuthentication().getSecurityPolicyUri())
                ).getUri())
                .securityMode(MessageSecurityMode.valueOf(userConfig.getEncryption().getMessageSecurityMode().name()))
                .build();

        config.setEndpoint(endpoint)
                .setApplicationName(LocalizedText.english(
                        userConfig.getConnection().getApplicationName()))
                .setApplicationUri(userConfig.getConnection().getApplicationUri())
                .setProductUri(userConfig.getConnection().getProductUri())
                .setRequestTimeout(uint(String.valueOf(userConfig.getConnection().getTimeout())));
    }

    private void configurarAutenticacion(OpcUaClientConfigBuilder config,
                                         UserConfigurationOpcUa userConfig) {
        if (userConfig.getAuthentication().getIdentityProvider().equals(IdentityProvider.ANONYMOUS)) {
            config.setIdentityProvider(new AnonymousProvider());
        } else if (userConfig.getAuthentication().getIdentityProvider().equals(IdentityProvider.USERNAME)) {
            config.setIdentityProvider(new UsernameProvider(
                    userConfig.getAuthentication().getUserName(),
                    userConfig.getAuthentication().getPassword()
            ));
        } else if (userConfig.getAuthentication().getIdentityProvider().equals(IdentityProvider.X509IDENTITY)) {
            config.setIdentityProvider(new X509IdentityProvider(
                    certificateManager.getClientCertificate(),
                    certificateManager.getPrivateKey()
            ));
        }
    }

    private void configurarSesion(OpcUaClientConfigBuilder config,
                                  UserConfigurationOpcUa userConfig) {
        config.setSessionName(() -> userConfig.getSession().getSessionName())
                .setSessionTimeout(uint(String.valueOf(userConfig.getSession().getTimeout())))
                .setMaxResponseMessageSize(uint(
                        userConfig.getSession().getMaxResponseMessageSize()))
                .setSessionLocaleIds((String[]) userConfig.getSession().getLocaleIds().stream()
                        .map(LocaleIds::getLocaleId)
                        .toArray());
    }
}
