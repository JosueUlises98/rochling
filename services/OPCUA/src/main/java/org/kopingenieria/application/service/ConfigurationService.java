package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.kopingenieria.exception.ConfigurationException;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class ConfigurationService implements Configuration {

    public OpcUaClient create(String endpointurl, OpcUaClientConfig opcUaClientConfig) throws UaException{
        return OpcUaClient.create(
                endpointurl,
                endpoints -> {
                    try {
                        return Optional.ofNullable(endpoints.stream()
                                .filter(endpoint -> endpoint.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri()))
                                .findFirst()
                                .orElseThrow(() -> new Exception("No se encontró un endpoint con la política de seguridad adecuada.")));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                },
                configBuilder -> {
                    try {
                        return configBuilder
                                .setApplicationName(opcUaClientConfig.getApplicationName())
                                .setApplicationUri(opcUaClientConfig.getApplicationUri())
                                .setProductUri(opcUaClientConfig.getProductUri())
                                .setChannelLifetime(opcUaClientConfig.getChannelLifetime())
                                .setSessionName(opcUaClientConfig.getSessionName())
                                .setSessionTimeout(opcUaClientConfig.getSessionTimeout())
                                .setRequestTimeout(opcUaClientConfig.getRequestTimeout())
                                .setAcknowledgeTimeout(opcUaClientConfig.getAcknowledgeTimeout())
                                .setCertificate(opcUaClientConfig.getCertificate().orElseThrow(()->new ConfigurationException("Ocurrio un error al obtener el certificado")))
                                .setEventLoop(opcUaClientConfig.getEventLoop())
                                .setIdentityProvider(opcUaClientConfig.getIdentityProvider())
                                .setCertificateValidator(opcUaClientConfig.getCertificateValidator())
                                .setCertificateChain(opcUaClientConfig.getCertificateChain().orElseThrow(()->new ConfigurationException("Ocurrio un error al obtener la cadena del certificado")))
                                .setEncodingLimits(opcUaClientConfig.getEncodingLimits())
                                .setKeepAliveFailuresAllowed(opcUaClientConfig.getKeepAliveFailuresAllowed())
                                .setKeepAliveInterval(opcUaClientConfig.getKeepAliveInterval())
                                .setKeepAliveTimeout(opcUaClientConfig.getKeepAliveTimeout())
                                .setConnectTimeout(opcUaClientConfig.getConnectTimeout())
                                .setKeyPair(opcUaClientConfig.getKeyPair().orElseThrow(()->new ConfigurationException("Ocurrio un error al obtener el par de claves")))
                                .setMaxPendingPublishRequests(opcUaClientConfig.getMaxPendingPublishRequests())
                                .setEncodingLimits(opcUaClientConfig.getEncodingLimits())
                                .setSessionLocaleIds(opcUaClientConfig.getSessionLocaleIds())
                                .setWheelTimer(opcUaClientConfig.getWheelTimer())
                                .build();
                    } catch (ConfigurationException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }


    public OpcUaClient create(String endpointurl) throws UaException {
        return null;
    }

    public OpcUaClient create(OpcUaClientConfig opcUaClientConfig) throws UaException {
        return null;
    }


    public OpcUaClient create(String endpointUrl, Function<List<EndpointDescription>, Optional<EndpointDescription>> selectEndpoint, Function<OpcUaClientConfigBuilder, OpcUaClientConfig> buildConfig) throws UaException {
        return null;
    }


    public OpcUaClient create() throws UaException {
        return null;
    }


}
