package org.kopingenieria.application.service.opcua.workflow;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.sdk.client.api.subscriptions.UaSubscription;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;

import java.io.IOException;

public interface Configuration {
    private void configurarConexion(OpcUaClientConfigBuilder config,
                                    org.kopingenieria.config.opcua.user.UserConfiguration userConfig);
    private void configurarAutenticacion(OpcUaClientConfigBuilder config,
                                         org.kopingenieria.config.opcua.user.UserConfiguration userConfig);
    private void configurarSesion(OpcUaClientConfigBuilder config,
                                  org.kopingenieria.config.opcua.user.UserConfiguration userConfig);
    private void configurarSuscripciones(OpcUaClient client,
                                         org.kopingenieria.config.opcua.user.UserConfiguration userConfig);
    private UaSubscription configurarSuscripcion(OpcUaClient client, org.kopingenieria.config.opcua.user.UserConfiguration.Subscription subscriptionConfig);

}
