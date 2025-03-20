package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.stack.core.UaException;

public interface Configuration {
    OpcUaClient create(ConnectionRequest connectionRequest) throws UaException;
    OpcUaClient create() throws UaException;
}
