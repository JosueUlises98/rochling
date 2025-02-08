package org.kopingenieria.services;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.kopingenieria.model.SessionObject;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public interface Configuration {
    OpcUaClient create(String endpointurl, OpcUaClientConfig opcUaClientConfig) throws UaException;
    OpcUaClient create(String endpointurl) throws UaException;
    OpcUaClient create(OpcUaClientConfig opcUaClientConfig) throws UaException;
    OpcUaClient create(String endpointUrl, Function<List<EndpointDescription>, Optional<EndpointDescription>> selectEndpoint, Function<OpcUaClientConfigBuilder, OpcUaClientConfig> buildConfig) throws UaException;
    OpcUaClient create() throws UaException;
}
