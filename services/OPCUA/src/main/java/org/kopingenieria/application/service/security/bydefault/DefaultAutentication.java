package org.kopingenieria.application.service.security.bydefault;

import org.kopingenieria.api.request.security.UserAuthenticationRequest;
import org.kopingenieria.api.response.security.AuthenticationResponse;

public interface DefaultAutentication {

    AuthenticationResponse authenticate(UserAuthenticationRequest authenticationRequest) throws SecurityException;

    AuthenticationResponse isSupported(UserAuthenticationRequest authenticationRequest);

    AuthenticationResponse invalidate(UserAuthenticationRequest authenticationRequest);

    AuthenticationResponse getCurrentIdentityProvider(UserAuthenticationRequest authenticationRequest);
}
