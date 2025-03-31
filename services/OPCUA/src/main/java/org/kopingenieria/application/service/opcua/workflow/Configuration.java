package org.kopingenieria.application.service.opcua.workflow;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;

import java.io.IOException;

public interface Configuration {
    OpcUaClient createDefaultOpcUaClient() throws UaException, OpcUaConfigurationException, IOException;
    OpcUaClient createUserOpcUaClient() throws OpcUaConfigurationException;
}
