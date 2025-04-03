package org.kopingenieria.application.validators.contract.bydefault;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.domain.model.bydefault.DefaultSessionConfiguration;
import org.kopingenieria.domain.model.user.UserSessionConfiguration;

public interface DefaultSessionValidator {

    boolean validateSession(UaClient client);

    boolean validateSessionToken(String token);

    boolean isSessionActive(UserSessionConfiguration client);

    boolean isSessionExpired(UserSessionConfiguration client);

    boolean validateSessionSecurityPolicy(UaClient client, DefaultSessionConfiguration user);

    boolean validateSessionSecurityMode(UaClient client, DefaultSessionConfiguration user);

    boolean validateSessionCertificate(UaClient client, DefaultSessionConfiguration user);

    boolean validateSessionTimeout(UaClient client);

    String getValidationResult();

}
