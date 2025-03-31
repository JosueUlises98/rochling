package org.kopingenieria.application.validators.contracts;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;

public interface SessionValidator extends OpcUaValidator {

    boolean validateSessionUser(UaClient client);

    boolean validateSessionDefault(UaClient client);

    boolean validateSessionUserToken(String token);
    boolean validateSession

    boolean isSessionActive(UaClient client);

    boolean isSessionExpired(UaClient client);

    boolean validateSessionSecurityPolicy(UaClient client, String securityPolicy);
    boolean validateSessionUserSecurityPolicy(UaClient client, String securityPolicy);
    boolean validateSessionUserSecurityMode(UaClient client, String securityMode);
    boolean validateSessionUserCertificate(UaClient client, String certificate);

    boolean validateSessionSecurityMode(UaClient client, String securityMode);

    boolean validateSessionCertificate(UaClient client, String certificate);

    boolean validateSessionTimeout(UaClient client, int timeout);

}
