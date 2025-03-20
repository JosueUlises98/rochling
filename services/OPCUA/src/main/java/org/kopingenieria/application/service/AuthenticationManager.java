package org.kopingenieria.application.service;

import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;

public class AuthenticationManager {

    private final AuthenticationHandlerOpcua authenticationHandlerOpcua = new AuthenticationHandlerOpcua();
    private final AuthenticationHandlerSSL authenticationHandlerSSL = new AuthenticationHandlerSSL();
    private final AuthenticationHandlerTcp authenticationHandlerTcp = new AuthenticationHandlerTcp();

    public IdentityProvider authenticateAnonymouslyOpcUa() throws Exception {
        return authenticationHandlerOpcua.authenticateAnonymouslyOpcUa();
    }

    public IdentityProvider authenticateWithUsernameAndPasswordOpcUa(String username, String password) throws Exception {
        return authenticationHandlerOpcua.authenticateWithUsernameAndPasswordOpcUa(username, password);
    }

    public IdentityProvider authenticateWithCertificateOpcUa(String certificatePath, String privateKeyPath) throws Exception {
        return authenticationHandlerOpcua.authenticateWithCertificateOpcUa(certificatePath, privateKeyPath);
    }

    public IdentityProvider authenticateCustomOpcUa(Object customData) throws Exception {
        return authenticationHandlerOpcua.authenticateCustomOpcUa(customData);
    }

    public IdentityProvider authenticateAnonymouslySSL() throws Exception {
        return authenticationHandlerSSL.authenticateAnonymouslySSL();
    }

    public IdentityProvider authenticateWithUsernameAndPasswordSSL(String username, String password) throws Exception {
        return authenticationHandlerSSL.authenticateWithUsernameAndPasswordSSL(username, password);
    }

    public IdentityProvider authenticateWithCertificateSSL(String certificatePath, String privateKeyPath) throws Exception {
        return authenticationHandlerSSL.authenticateWithCertificateSSL(certificatePath, privateKeyPath);
    }

    public IdentityProvider authenticateCustomSSL(Object customData) throws Exception {
        return authenticationHandlerSSL.authenticateCustomSSL(customData);
    }

    public IdentityProvider authenticateAnonymouslyTcp() throws Exception {
        return authenticationHandlerTcp.authenticateAnonymouslyTcp();
    }

    public IdentityProvider authenticateWithUsernameAndPasswordTcp(String username, String password) throws Exception {
        return authenticationHandlerTcp.authenticateWithUsernameAndPasswordTcp(username, password);
    }

    public IdentityProvider authenticateWithCertificateTcp(String certificatePath, String privateKeyPath) throws Exception {
        return authenticationHandlerTcp.authenticateWithCertificateTcp(certificatePath, privateKeyPath);
    }

    public IdentityProvider authenticateCustomTcp(Object customData) throws Exception {
        return authenticationHandlerTcp.authenticateCustomTcp(customData);
    }

}
