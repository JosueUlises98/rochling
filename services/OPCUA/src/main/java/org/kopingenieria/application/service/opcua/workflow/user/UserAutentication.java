package org.kopingenieria.application.service.opcua.workflow.user;

import org.kopingenieria.domain.enums.security.IdentityProvider;

public interface UserAutentication {

    boolean authenticate(IdentityProvider identityProvider, Object... credentials) throws SecurityException;

    boolean isSupported(IdentityProvider identityProvider);

    boolean invalidate();

    IdentityProvider getCurrentIdentityProvider();
}
