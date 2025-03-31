package org.kopingenieria.application.service.opcua.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.kopingenieria.application.service.files.UserConfigFile;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;
import org.kopingenieria.util.security.CertificateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Service
@Getter
public class OpcUaConfiguration implements Configuration {

    @Autowired
    private CertificateManager certificateManager;
    private Map<OpcUaClient, List<UaSubscription>> mapSubscriptions;

    @Override
    public OpcUaClient createDefaultOpcUaClient() throws OpcUaConfigurationException, IOException {
        try {
            // 1. Creación del builder de configuración
            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();

            // Sección de conexión
            EndpointDescription endpoint = EndpointDescription.builder()
                    .endpointUrl("opc.tcp://localhost:4840")
                    .securityPolicyUri(SecurityPolicy.None.getUri())
                    .securityMode(MessageSecurityMode.None)
                    .build();

            config.setEndpoint(endpoint)
                    .setApplicationName(LocalizedText.english("Eclipse Milo OPC UA Client"))
                    .setApplicationUri("urn:eclipse:milo:client")
                    .setProductUri("urn:eclipse:milo:client:product")
                    .setRequestTimeout(UInteger.valueOf(5000))
                    .setChannelLifetime(UInteger.valueOf(60000))
                    .setConnectTimeout(UInteger.valueOf(5000));

            // Sección de autenticación
            config.setIdentityProvider(new AnonymousProvider());

            // Sección de encriptación
            DefaultClientCertificateValidator certificateValidator =
                    new DefaultClientCertificateValidator(new DefaultTrustListManager(new File("trustlist")));
            config.setCertificateValidator(certificateValidator);

            // Sección de sesión
            config.setSessionName(() -> "DefaultSession")
                    .setSessionTimeout(UInteger.valueOf(60000))
                    .setMaxResponseMessageSize(UInteger.valueOf(65536))
                    .setMaxPendingPublishRequests(UInteger.valueOf(10));

            // Sección de suscripciones
            OpcUaClient client = OpcUaClient.create(config.build());

            UaSubscription subscription = client.getSubscriptionManager()
                    .createSubscription(0).get();

            subscription.setMonitoringMode(MonitoringMode.Disabled, List.of());
            subscription.setPublishingMode(false);

            return client;

        } catch (UaException | InterruptedException | ExecutionException e) {
            throw new OpcUaConfigurationException("No se pudo crear el cliente OPC UA por defecto", e);
        }
    }

    @Override
    public OpcUaClient createUserOpcUaClient() throws OpcUaConfigurationException {
        try {
            // Cargar configuración
            UserConfigFile configFile = new UserConfigFile(
                    new ObjectMapper(),
                    new Properties(),
                    new UserConfiguration()
            );
            UserConfiguration userConfig =
                    configFile.loadConfiguration(configFile.extractExistingFilename());

            // Inicializar certificados
            certificateManager.configurarCertificados(userConfig);

            // Crear builder de configuración
            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();

            // 1. Configuración de conexión
            configurarConexion(config, userConfig);

            // 2. Configuración de autenticación
            configurarAutenticacion(config, userConfig);

            // 3. Configuración de seguridad
            certificateManager.aplicarConfiguracionSeguridad(config, userConfig);

            // 4. Configuración de sesión
            configurarSesion(config, userConfig);

            // 5. Crear cliente y configurar suscripciones
            OpcUaClient client = OpcUaClient.create(config.build());
            configurarSuscripciones(client, userConfig);

            return client;

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error creando cliente OPC UA", e);
        }
    }

    private void configurarConexion(OpcUaClientConfigBuilder config,
                                    UserConfiguration userConfig) {
        EndpointDescription endpoint = EndpointDescription.builder()
                .endpointUrl(userConfig.getConnection().getEndpointUrl())
                .securityPolicyUri(SecurityPolicy.valueOf(
                        String.valueOf(userConfig.getAuthentication().getSecurityPolicyUri())
                ).getUri())
                .securityMode(MessageSecurityMode.valueOf(
                        userConfig.getEncryption().getMessageSecurityMode()))
                .build();

        config.setEndpoint(endpoint)
                .setApplicationName(LocalizedText.english(
                        userConfig.getConnection().getApplicationName()))
                .setApplicationUri(userConfig.getConnection().getApplicationUri())
                .setProductUri(userConfig.getConnection().getProductUri())
                .setRequestTimeout(uint(String.valueOf(userConfig.getConnection().getTimeout())));
    }

    private void configurarAutenticacion(OpcUaClientConfigBuilder config,
                                         UserConfiguration userConfig) {
        if (userConfig.getAuthentication().isAnonymous()) {
            config.setIdentityProvider(new AnonymousProvider());
        } else if (userConfig.getAuthentication().isUsername()) {
            config.setIdentityProvider(new UsernameProvider(
                    userConfig.getAuthentication().getUserName(),
                    userConfig.getAuthentication().getPassword()
            ));
        } else if (userConfig.getAuthentication().isX509Certificate()) {
            config.setIdentityProvider(new X509IdentityProvider(
                    certificateManager.getClientCertificate(),
                    certificateManager.getPrivateKey()
            ));
        }
    }

    private void configurarSesion(OpcUaClientConfigBuilder config,
                                  UserConfiguration userConfig) {
        config.setSessionName(() -> userConfig.getSession().getSessionName())
                .setSessionTimeout(uint(String.valueOf(userConfig.getSession().getTimeout())))
                .setMaxResponseMessageSize(uint(
                        userConfig.getSession().getMaxResponseMessageSize()))
                .setSessionLocaleIds(
                        userConfig.getSession().getLocaleIds().toArray(new String[0]));
    }

    private void configurarSuscripciones(OpcUaClient client,
                                         UserConfiguration userConfig) throws Exception {
        List<UaSubscription> subscriptions = new ArrayList<>();
        mapSubscriptions = new HashMap<>();
        mapSubscriptions.put(client, subscriptions);

        for (UserConfiguration.Subscription subscriptionConfig :
                userConfig.getSubscriptions()) {
            UaSubscription nuevaSuscripcion = configurarSuscripcion(client, subscriptionConfig);
            subscriptions.add(nuevaSuscripcion);
        }
    }

    private UaSubscription configurarSuscripcion(OpcUaClient client, UserConfiguration.Subscription subscriptionConfig) throws Exception {
        // Obtener el administrador de suscripciones
        UaSubscriptionManager subscriptionManager = client.getSubscriptionManager();

        // Crear la suscripción con los parámetros configurados
        UaSubscription subscription = subscriptionManager.createSubscription(subscriptionConfig.getPublishingInterval()).get();

        // Configurar el modo de monitoreo
        subscription.setMonitoringMode(
                MonitoringMode.valueOf(subscriptionConfig.getMonitoringMode().name()),
                List.of()
        );

        // Configurar el modo de publicación
        subscription.setPublishingMode(subscriptionConfig.getPublishingEnabled());

        // Configurar parámetros de monitoreo para los items
        MonitoringParameters monitoringParameters = new MonitoringParameters(
                uint(1),
                subscriptionConfig.getSamplingInterval(),
                null,
                subscriptionConfig.getQueueSize(),
                subscriptionConfig.getDiscardOldest()
        );
        return subscription;
    }
}
