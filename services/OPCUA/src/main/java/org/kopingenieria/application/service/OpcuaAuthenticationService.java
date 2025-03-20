package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

public class OpcuaAuthenticationService extends AuthenticationService {
    @Override
    public IdentityProvider authenticateAnonymously() {
        return null;
    }

    @Override
    public IdentityProvider authenticateWithUsernameAndPassword(String username, String password) throws IllegalArgumentException {
        return null;
    }

    @Override
    public IdentityProvider authenticateWithCertificate(String certificatePath, String privateKeyPath) throws Exception {
        return null;
    }

    @Override
    public IdentityProvider authenticateCustom(Object customData) {
        return null;
    }
}
