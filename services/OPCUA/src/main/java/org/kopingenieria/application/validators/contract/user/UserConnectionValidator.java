package org.kopingenieria.application.validators.contract.user;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.domain.enums.connection.UrlType;

import java.io.File;

public interface UserConnectionValidator {
    boolean validateActiveSession(UaClient client);
    boolean validateValidSession(UaClient client);
    boolean validateHost(String host);
    boolean validatePort(int port);
    boolean validateEndpoint(String endpoint);
    boolean validateTimeout(int timeout);
    boolean validateSecurityPolicy(String securityPolicy);
    boolean validateSecurityMode(String securityMode);
    boolean validateCertificate(String certificate);
    boolean validatePrivateKey(String privateKey);
    boolean validateCertificateChain(String certificateChain);
    boolean validateCertificateAlias(String certificateAlias);
    boolean validateCertificateFile(File certificateFile);
    boolean validatePrivateKeyFile(File privateKeyFile);
    boolean validateCertificateChainFile(File certificateChainFile);
    boolean validateCertificateAliasFile(String certificateAliasFile);
    boolean validateCertificateFilePassword(File certificateFilePassword);
    boolean validatePrivateKeyFilePassword(File privateKeyFilePassword);
    boolean validateCertificateChainFilePassword(String certificateChainFilePassword);
    boolean validateCertificateAliasFilePassword(String certificateAliasFilePassword);
    boolean validateLocalHost(String host);
}
