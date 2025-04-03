package org.kopingenieria.application.service.opcua.workflow.bydefault;

import org.kopingenieria.domain.enums.security.IdentityProvider;

public interface DefaultAutentication {

    boolean authenticate(IdentityProvider identityProvider, Object... credentials) throws SecurityException;

    boolean isSupported(IdentityProvider identityProvider);

    boolean invalidate();

    IdentityProvider getCurrentIdentityProvider();
}
