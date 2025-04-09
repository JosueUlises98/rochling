package org.kopingenieria.application.service.security;

import org.kopingenieria.api.request.security.AuthenticationRequest;
import org.kopingenieria.api.response.security.AuthenticationResponse;

public interface UserAutentication {

    AuthenticationResponse authenticate(AuthenticationRequest request) throws SecurityException;

    AuthenticationResponse isSupported(AuthenticationRequest request);

    AuthenticationResponse invalidate();

    AuthenticationResponse getCurrentIdentityProvider();
}
