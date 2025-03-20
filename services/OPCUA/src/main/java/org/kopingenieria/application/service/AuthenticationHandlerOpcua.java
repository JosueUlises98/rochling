package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

public class AuthenticationHandlerOpcua {

    private final AuthenticationService authenticationService = new OpcuaAuthenticationService();

    public IdentityProvider authenticateAnonymouslyOpcUa() throws Exception {
        return authenticationService.authenticateAnonymously();
    }

    public IdentityProvider authenticateWithUsernameAndPasswordOpcUa(String username, String password) throws Exception {
       return authenticationService.authenticateWithUsernameAndPassword(username, password);
    }

    public IdentityProvider authenticateWithCertificateOpcUa(String certificatePath, String privateKeyPath) throws Exception {
        return authenticationService.authenticateWithCertificate(certificatePath, privateKeyPath);
    }

    public IdentityProvider authenticateCustomOpcUa(Object customData) throws Exception {
        return authenticationService.authenticateCustom(customData);
    }

}
