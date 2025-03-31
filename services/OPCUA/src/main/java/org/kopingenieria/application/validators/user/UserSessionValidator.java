package org.kopingenieria.application.validators.user;


import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.application.service.files.UserConfigFile;
import org.kopingenieria.application.validators.contracts.SessionValidator;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

public class UserSessionValidator implements SessionValidator {

    @Autowired
    private UserConfigFile configFile;
    @Autowired
    private UserAuthenticationValidator authValidator;

    @Override
    public boolean validateSessionUser(UaClient client) {
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

    public boolean validateSessionDefault(UaClient client) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration("default-config.yml");
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
        
    }

    @Override
    public boolean validateSessionSecurityPolicy(UaClient client, String securityPolicy) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration("default-config.yml");
            EndpointDescription endpoint = ((OpcUaClientConfig)client.getConfig()).getEndpoint();
            return endpoint.getSecurityPolicyUri().endsWith(
                    fileConfig.getSecurity().getSecurityPolicy()
            );
        } catch (Exception e) {
            log.error("Error validando política de seguridad: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionSecurityMode(UaClient client, String securityMode) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration("default-config.yml");
            EndpointDescription endpoint = ((OpcUaClientConfig)client.getConfig()).getEndpoint();
            return endpoint.getSecurityMode().name().equals(
                    fileConfig.getSecurity().getSecurityMode()
            );
        } catch (Exception e) {
            log.error("Error validando modo de seguridad: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionCertificate(UaClient client, String certificate) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration("default-config.yml");
            return certificate.equals(fileConfig.getSecurity().getCertificatePath());
        } catch (Exception e) {
            log.error("Error validando certificado: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionTimeout(UaClient client, int timeout) {
        try {
            UserConfiguration fileConfig = configFile.loadConfiguration("default-config.yml");
            int configuredTimeout = fileConfig.getSession().getSessionTimeout();
            return timeout >= configuredTimeout * 0.8 && // Permitir una variación del 20%
                    timeout <= configuredTimeout * 1.2;
        } catch (Exception e) {
            log.error("Error validando timeout de sesión: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateSessionParameters(UaClient client, UserConfiguration fileConfig) {
        try {
            OpcUaClientConfig clientConfig = (OpcUaClientConfig) client.getConfig();

            // Validar parámetros básicos de la sesión
            return clientConfig.getSessionTimeout().toMillis() == fileConfig.getSession().getSessionTimeout() &&
                    clientConfig.getSessionName().equals(fileConfig.getSession().getSessionName()) &&
                    validateEndpointParameters(clientConfig.getEndpoint(), fileConfig);

        } catch (Exception e) {
            log.error("Error validando parámetros de sesión: {}", e.getMessage());
            return false;
        }
    }

    private boolean validateEndpointParameters(EndpointDescription endpoint, UserConfiguration fileConfig) {
        return endpoint.getEndpointUrl().equals(fileConfig.getConnection().getEndpointUrl()) &&
                endpoint.getSecurityPolicyUri().endsWith(fileConfig.getSecurity().getSecurityPolicy()) &&
                endpoint.getSecurityMode().name().equals(fileConfig.getSecurity().getSecurityMode());
    }

}
