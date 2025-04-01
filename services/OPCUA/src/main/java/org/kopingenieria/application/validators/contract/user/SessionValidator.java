package org.kopingenieria.application.validators.contract.user;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;

public interface SessionValidator {

    boolean validateSession(UaClient client);

    boolean validateSessionToken(String token);

    boolean isSessionActive(UaClient client);

    boolean isSessionExpired(UaClient client);

    boolean validateSessionSecurityPolicy(UaClient client, String securityPolicy);

    boolean validateSessionSecurityMode(UaClient client, String securityMode);

    boolean validateSessionCertificate(UaClient client, String certificate);

    boolean validateSessionTimeout(UaClient client, int timeout);

    String getValidationResult();

}
