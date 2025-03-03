package org.kopingenieria.services;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

public class AuthenticationHandlerSSL {

    private final AuthenticationService authenticationService = new SSLAuthentication();

    public IdentityProvider authenticateAnonymouslySSL() throws Exception {
        return authenticationService.authenticateAnonymously();
    }

    public IdentityProvider authenticateWithUsernameAndPasswordSSL(String username, String password) throws Exception {
        return authenticationService.authenticateWithUsernameAndPassword(username, password);
    }

    public IdentityProvider authenticateWithCertificateSSL(String certificatePath, String privateKeyPath) throws Exception {
        return authenticationService.authenticateWithCertificate(certificatePath, privateKeyPath);
    }

    public IdentityProvider authenticateCustomSSL(Object customData) throws Exception {
        return authenticationService.authenticateCustom(customData);
    }
}
