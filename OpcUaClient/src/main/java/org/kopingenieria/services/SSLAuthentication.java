package org.kopingenieria.services;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

public class SSLAuthentication extends AuthenticationService{


    public IdentityProvider authenticateAnonymously() {
        return null;
    }

    public IdentityProvider authenticateWithUsernameAndPassword(String username, String password) throws IllegalArgumentException {
        return null;
    }

    public IdentityProvider authenticateWithCertificate(String certificatePath, String privateKeyPath) throws Exception {
        return null;
    }

    public IdentityProvider authenticateCustom(Object customData) {
        return null;
    }
}
