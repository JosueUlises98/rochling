package org.kopingenieria.application.validators.bydefault;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;


public interface DefaultConnectionValidator {
    boolean validateActiveSession(UaClient client);
    boolean validateValidSession(UaClient client);
    boolean validateHost(String host);
    boolean validatePort(int port);
    boolean validateEndpoint(String endpoint);
    boolean validateTimeout(int timeout);
    boolean validateLocalHost(String host);
}
