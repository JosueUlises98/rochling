package org.kopingenieria.application.validators.bydefault;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.domain.model.user.UserSessionConfiguration;

public interface DefaultSessionValidator {

    boolean validateSession(UaClient client);

    boolean validateSessionToken(String token);

    boolean isSessionActive(UserSessionConfiguration client);

    boolean isSessionExpired(UserSessionConfiguration client);

    boolean validateSessionTimeout(UaClient client);

    String getValidationResult();

}
