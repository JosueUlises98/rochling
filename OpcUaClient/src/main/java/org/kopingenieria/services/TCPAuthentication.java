package org.kopingenieria.services;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.kopingenieria.exceptions.ConnectionException;
import org.kopingenieria.model.enums.client.network.connection.UrlType;
import java.util.concurrent.ExecutionException;


public class TCPAuthentication extends AuthenticationService {

    private final TcpConnection client;

    public TCPAuthentication(TcpConnection client) {
        this.client = client;
    }

    public IdentityProvider authenticateAnonymously() throws ExecutionException, InterruptedException, ConnectionException {
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

    public static void main(String[] args) throws UaException, ExecutionException, InterruptedException {
        TCPAuthentication tcpAuthentication = new TCPAuthentication(OpcUaClient.create(UrlType.Adress1.getUrl()));
        tcpAuthentication.authenticateAnonymously();
    }
}
