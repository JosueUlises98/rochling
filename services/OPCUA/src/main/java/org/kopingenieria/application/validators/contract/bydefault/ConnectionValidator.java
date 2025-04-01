package org.kopingenieria.application.validators.contract.bydefault;

import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.domain.enums.connection.UrlType;

public interface ConnectionValidator {
    boolean validateActiveSession(UaClient client);

    boolean validateValidSession(UaClient client);

    boolean validateHost(String host);

    boolean validatePort(int port);

    boolean validateEndpoint(UrlType endpoint);

    boolean validateTimeout(int timeout);

    boolean validateSecurityPolicy(String securityPolicy);

    boolean validateSecurityMode(String securityMode);

    boolean validateCertificate(String certificate);

    boolean validatePrivateKey(String privateKey);

    boolean validateCertificateChain(String certificateChain);

    boolean validateCertificateAlias(String certificateAlias);

    boolean validateCertificateFile(String certificateFile);

    boolean validatePrivateKeyFile(String privateKeyFile);

    boolean validateCertificateChainFile(String certificateChainFile);

    boolean validateCertificateAliasFile(String certificateAliasFile);

    boolean validateCertificateFilePassword(String certificateFilePassword);

    boolean validatePrivateKeyFilePassword(String privateKeyFilePassword);

    boolean validateCertificateChainFilePassword(String certificateChainFilePassword);

    boolean validateCertificateAliasFilePassword(String certificateAliasFilePassword);

    boolean validateLocalHost(String host);

    String getValidationResult();
}
