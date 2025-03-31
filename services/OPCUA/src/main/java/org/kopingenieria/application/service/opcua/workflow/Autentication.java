package org.kopingenieria.application.service.opcua.workflow;

import org.kopingenieria.domain.enums.security.IdentityProvider;

public interface Autentication {

    boolean authenticate(IdentityProvider identityProvider, Object... credentials) throws SecurityException;

    boolean isSupported(IdentityProvider identityProvider);

    boolean invalidate();

    IdentityProvider getCurrentIdentityProvider();
}
