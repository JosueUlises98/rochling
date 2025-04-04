package org.kopingenieria.application.validators.user;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.domain.model.user.UserSessionConfiguration;


public interface UserSessionValidator {

    boolean validateSession(UaClient client);

    boolean validateSessionToken(String token);

    boolean isSessionActive(UserSessionConfiguration user);

    boolean isSessionExpired(UserSessionConfiguration user);

    boolean validateSessionSecurityPolicy(UaClient client, UserSessionConfiguration user);

    boolean validateSessionSecurityMode(UaClient client, UserSessionConfiguration user);

    boolean validateSessionCertificate(UaClient client, UserSessionConfiguration user);

    boolean validateSessionTimeout(UaClient client);

    String getValidationResult();

}
