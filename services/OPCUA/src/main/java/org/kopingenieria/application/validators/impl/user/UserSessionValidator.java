package org.kopingenieria.application.validators.impl.user;


import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.kopingenieria.application.service.files.user.UserConfigFile;
import org.kopingenieria.application.validators.contract.user.SessionValidator;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.springframework.beans.factory.annotation.Autowired;

public class UserSessionValidator implements SessionValidator {

    @Autowired
    private UserConfigFile configFile;
    @Autowired
    private UserAuthenticationValidator authValidator;

    @Override
    public boolean validateSession(UaClient client) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration(configFile.extractExistingFilename());
            if (client == null || fileConfig == null) {
                return false;
            }
            return validateSessionParameters(client, fileConfig) &&
                    isSessionActive(client) &&
                    !isSessionExpired(client);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateSessionToken(String token) {
        return authValidator.isSessionTokenValid(token);
    }

    @Override
    public boolean isSessionActive(UaClient client) {
        try {
            return client.getSession().isDone();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isSessionExpired(UaClient client) {
        return true;
    }

    @Override
    public boolean validateSessionSecurityPolicy(UaClient client, String securityPolicy) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration(configFile.extractExistingFilename());
            EndpointDescription endpoint = client.getConfig().getEndpoint();
            return endpoint.getSecurityPolicyUri().endsWith(
                    String.valueOf(fileConfig.getAuthentication().getSecurityPolicy()));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateSessionSecurityMode(UaClient client, String securityMode) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration(configFile.extractExistingFilename());
            EndpointDescription endpoint = client.getConfig().getEndpoint();
            return endpoint.getSecurityMode().name().equals(
                    fileConfig.getEncryption().getMessageSecurityMode());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateSessionCertificate(UaClient client, String certificate) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration(configFile.extractExistingFilename());
            return certificate.equals(fileConfig.getAuthentication().getCertificatePath());
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateSessionTimeout(UaClient client, int timeout) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration(configFile.extractExistingFilename());
            int configuredTimeout = Timeouts.SESSION.getDuration();
            return timeout >= configuredTimeout * 0.8 && // Permitir una variación del 20%
                    timeout <= configuredTimeout * 1.2;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateSessionParameters(UaClient client, UserConfiguration fileConfig) {
        try {
            OpcUaClientConfig clientConfig = client.getConfig();

            // Validar parámetros básicos de la sesión
            return clientConfig.getSessionTimeout() == UInteger.valueOf() &&
                    clientConfig.getSessionName().equals(fileConfig.getSession().getSessionName()) &&
                    validateEndpointParameters(clientConfig.getEndpoint(), fileConfig);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateEndpointParameters(EndpointDescription endpoint, UserConfiguration fileConfig) {
        return endpoint.getEndpointUrl().equals(fileConfig.getConnection().getEndpointUrl()) &&
                endpoint.getSecurityPolicyUri().endsWith(fileConfig.getSecurity().getSecurityPolicy()) &&
                endpoint.getSecurityMode().name().equals(fileConfig.getSecurity().getSecurityMode());
    }

}
