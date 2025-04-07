package org.kopingenieria.application.service.security.bydefault;

import org.kopingenieria.api.request.security.OpcUaAuthenticationRequest;
import org.kopingenieria.api.response.security.AuthenticationResponse;

public interface DefaultAutentication {

    AuthenticationResponse authenticate(OpcUaAuthenticationRequest authenticationRequest) throws SecurityException;

    AuthenticationResponse isSupported(OpcUaAuthenticationRequest authenticationRequest);

    AuthenticationResponse invalidate(OpcUaAuthenticationRequest authenticationRequest);

    AuthenticationResponse getCurrentIdentityProvider(OpcUaAuthenticationRequest authenticationRequest);
}
