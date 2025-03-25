package org.kopingenieria.application.service.opcua;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscriptionManager;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MonitoringMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.MonitoringParameters;
import org.kopingenieria.application.service.files.OpcUaConfigFile;
import org.kopingenieria.exception.OpcUaConfigurationException;
import org.kopingenieria.util.CertificateLoader;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import static org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned.uint;

@Service
@Getter
public class OpcUaConfiguration implements Configuration {

    private Map<OpcUaClient,List<UaSubscription>>mapSubscriptions;

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
            config.setSessionName(()->"DefaultSession")
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
            org.kopingenieria.config.OpcUaConfiguration userConfig;
            OpcUaConfigFile configFile = new OpcUaConfigFile(new ObjectMapper(),new Properties(),new org.kopingenieria.config.OpcUaConfiguration());
             userConfig = configFile.loadConfiguration(configFile.extractExistingFilename());

            // Creamos el builder de configuración
            OpcUaClientConfigBuilder config = new OpcUaClientConfigBuilder();

            // 1. Configuración de conexión
            EndpointDescription endpoint = EndpointDescription.builder()
                    .endpointUrl(userConfig.getConnection().getEndpointUrl())
                    .securityPolicyUri(SecurityPolicy.valueOf(
                            String.valueOf(userConfig.getAuthentication().getSecurityPolicy())
                    ).getUri())
                    .securityMode(MessageSecurityMode.valueOf(userConfig.getEncryption().getMessageSecurityMode()))
                    .build();
            config.setEndpoint(endpoint)
                    .setApplicationName(LocalizedText.english(userConfig.getConnection().getApplicationName()))
                    .setApplicationUri(userConfig.getConnection().getApplicationUri())
                    .setProductUri(userConfig.getConnection().getProductUri())
                    .setRequestTimeout(UInteger.valueOf(String.valueOf(userConfig.getConnection().getTimeout())));

            // 2. Configuración de autenticación
            if (userConfig.getAuthentication().isAnonymous()) {
                config.setIdentityProvider(new AnonymousProvider());
            } else if (userConfig.getAuthentication().isUsername()) {
                config.setIdentityProvider(new UsernameProvider(
                        userConfig.getAuthentication().getUserName(),
                        userConfig.getAuthentication().getPassword()
                ));
            } else if (userConfig.getAuthentication().isX509Certificate()) {
                try {
                    X509Certificate certificate = CertificateLoader.loadX509Certificate(
                            userConfig.getAuthentication().getCertificatePath()
                    );
                    PrivateKey privateKey = CertificateLoader.loadPrivateKey(
                            userConfig.getAuthentication().getPrivateKeyPath()
                    );
                    config.setIdentityProvider(new X509IdentityProvider(certificate, privateKey));
                } catch (CertificateException e) {
                    throw new OpcUaConfigurationException("Error al cargar el certificado: " + e.getMessage(), e);
                } catch (IOException e) {
                    throw new OpcUaConfigurationException("Error al leer los archivos: " + e.getMessage(), e);
                } catch (GeneralSecurityException e) {
                    throw new OpcUaConfigurationException("Error en la clave privada: " + e.getMessage(), e);
                }
            }

            // 3. Configuración de Encriptación
            //La configuracion de encriptacion se define en la seccion de conexion en el endpoint.

            // 5. Configuración de sesión
            config.setSessionName(()->userConfig.getSession().getSessionName())
                    .setSessionTimeout(UInteger.valueOf(userConfig.getSession().getTimeout()))
                    .setMaxResponseMessageSize(UInteger.valueOf(
                            userConfig.getSession().getMaxResponseMessageSize()))
                    .setSessionLocaleIds(userConfig.getSession().getLocaleIds().toArray(new String[0]));

            // Crear el cliente
            // Sección de suscripciones
            OpcUaClient client = OpcUaClient.create(config.build());
            List<UaSubscription> subscriptions = new ArrayList<>();
            mapSubscriptions = Map.of(client,subscriptions);

            // Configurar todas las suscripciones definidas
            for (org.kopingenieria.config.OpcUaConfiguration.Subscription subscriptionConfig : userConfig.getSubscriptions()) {
                UaSubscription nuevaSuscripcion = configurarSuscripcion(client, subscriptionConfig);
                subscriptions.add(nuevaSuscripcion);
                mapSubscriptions.put(client, subscriptions);
            }

            return client;
        } catch (Exception e) {
            throw new OpcUaConfigurationException("No se pudo crear el cliente OPC UA personalizado", e);
        }
    }

    private UaSubscription configurarSuscripcion(OpcUaClient client, org.kopingenieria.config.OpcUaConfiguration.Subscription subscriptionConfig) throws Exception {
        // Obtener el administrador de suscripciones
        UaSubscriptionManager subscriptionManager = client.getSubscriptionManager();

        // Crear la suscripción con los parámetros configurados
        UaSubscription subscription = subscriptionManager.createSubscription(subscriptionConfig.getPublishingInterval()).get();

        // Configurar el modo de monitoreo
        subscription.setMonitoringMode(
                subscriptionConfig.getMonitoringMode(),
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
