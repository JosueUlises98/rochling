package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

public class AuthenticationHandlerTcp {

    private final AuthenticationService authenticationService = new TCPAuthentication();

    public IdentityProvider authenticateAnonymouslyTcp() throws Exception {
        return authenticationService.authenticateAnonymously();
    }

    public IdentityProvider authenticateWithUsernameAndPasswordTcp(String username, String password) throws Exception {
        return authenticationService.authenticateWithUsernameAndPassword(username, password);
    }

    public IdentityProvider authenticateWithCertificateTcp(String certificatePath, String privateKeyPath) throws Exception {
        return authenticationService.authenticateWithCertificate(certificatePath, privateKeyPath);
    }

    public IdentityProvider authenticateCustomTcp(Object customData) throws Exception {
        return authenticationService.authenticateCustom(customData);
    }
}
