package org.kopingenieria.application.service.security.bydefault;

import org.kopingenieria.api.response.AuthenticationResponse;
import org.kopingenieria.domain.enums.security.IdentityProvider;

public interface DefaultAutentication {

    AuthenticationResponse authenticate(IdentityProvider identityProvider, Object... credentials) throws SecurityException;

    AuthenticationResponse isSupported(IdentityProvider identityProvider);

    AuthenticationResponse invalidate();

    AuthenticationResponse getCurrentIdentityProvider();
}
