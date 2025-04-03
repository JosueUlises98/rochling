package org.kopingenieria.application.service.opcua.workflow.bydefault;

import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.AnonymousProvider;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.kopingenieria.application.service.opcua.workflow.user.UserConfigurationImpl;
import org.kopingenieria.application.validators.contract.user.UserAuthenticationValidator;
import org.kopingenieria.application.validators.impl.user.UserAuthenticationValidatorImpl;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;

public class DefaultAuthenticationImpl implements DefaultAutentication {

    private final OpcUaClient opcUaClient;
    private IdentityProvider currentProvider;
    private boolean isAuthenticated;
    private static final int AUTH_TIMEOUT_SECONDS = 10;
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final UserAuthenticationValidator AUTHENTICATION_VALIDATOR;

    static {
        AUTHENTICATION_VALIDATOR = new UserAuthenticationValidatorImpl();
    }

    public DefaultAuthenticationImpl() throws OpcUaConfigurationException {
        this.opcUaClient = new UserConfigurationImpl().createUserOpcUaClient();
        this.isAuthenticated = false;
    }

    @Override
    public boolean authenticate(IdentityProvider identityProvider, Object... credentials)
            throws SecurityException {
        try {
            if (!isSupported(identityProvider)) {
                throw new SecurityException("Proveedor de identidad no soportado: " + identityProvider);
            }
            // Obtener la configuración de seguridad actual del cliente
            EndpointDescription activeEndpoint = opcUaClient.getConfig().getEndpoint();

            // Verificar que el endpoint coincida con el tipo de autenticación solicitado
            switch (identityProvider) {
                case USERNAME ->
            }
            validateEndpointSecurity(activeEndpoint, identityProvider);

            // Verificar las credenciales actuales del cliente
            boolean authResult = verifyClientCredentials(credentials);

            if (authResult) {
                currentProvider = identityProvider;
                isAuthenticated = true;
            }

            return isAuthenticated;

        } catch (Exception e) {
            throw new SecurityException("Error de autenticación: " + e.getMessage());
        }
    }

    private void validateEndpointSecurity(EndpointDescription endpoint, IdentityProvider identityProvider)
            throws SecurityException {

        String securityPolicyUri = endpoint.getSecurityPolicyUri();
        MessageSecurityMode securityMode = endpoint.getSecurityMode();

        switch (identityProvider) {
            case ANONYMOUS:
                if (!securityPolicyUri.equals(SecurityPolicy.None.getUri())) {
                    throw new SecurityException("Configuración de endpoint no válida para autenticación anónima");
                }
                break;

            case USERNAME:
                if (securityMode == MessageSecurityMode.None) {
                    throw new SecurityException("Se requiere modo de seguridad para autenticación por usuario/contraseña");
                }
                break;

            case X509IDENTITY:
                if (securityMode != MessageSecurityMode.SignAndEncrypt) {
                    throw new SecurityException("Se requiere modo SignAndEncrypt para autenticación por certificado");
                }
                break;
        }
    }

    private boolean verifyClientCredentials(Object credentials) {
        try {
            org.eclipse.milo.opcua.sdk.client.api.identity.IdentityProvider identityProvider = opcUaClient.getConfig().getIdentityProvider();

            return switch (identityProvider) {
                case AnonymousProvider ignored -> verifyAnonymousConfig(identityProvider);
                case UsernameProvider ignored -> verifyUserPasswordConfig(identityProvider, credentials);
                case org.eclipse.milo.opcua.sdk.client.api.identity.X509IdentityProvider ignored ->
                        verifyCertificateConfig();
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyAnonymousConfig(Object clientIdentityProvider) {
        return clientIdentityProvider instanceof AnonymousProvider;
    }

    private boolean verifyUserPasswordConfig(Object clientIdentityProvider, Object credentials) {
        return false;
    }

    private boolean verifyCertificateConfig() {
        // Verificar el certificado configurado en el cliente
        try {
            var clientCertificate = opcUaClient.getConfig().getCertificate();
            if (clientCertificate.isEmpty()) {
                return false;
            }
            // Verificar que el certificado esté activo y sea válido
            clientCertificate.get().checkValidity();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isSupported(IdentityProvider identityProvider) {
        if (identityProvider == null) {
            return false;
        }
        // Verificar si el tipo de autenticación está configurado en el cliente
        try {
            EndpointDescription endpoint = opcUaClient.getConfig().getEndpoint();
            return switch (identityProvider) {
                case ANONYMOUS -> endpoint.getSecurityPolicyUri().equals(SecurityPolicy.None.getUri());
                case USERNAME -> endpoint.getSecurityMode() != MessageSecurityMode.None;
                case X509IDENTITY -> endpoint.getSecurityMode() == MessageSecurityMode.SignAndEncrypt;
                default -> false;
            };
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean invalidate() {
        try {
            if (isAuthenticated) {
                opcUaClient.disconnect().get();
                isAuthenticated = false;
                currentProvider = null;
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public IdentityProvider getCurrentIdentityProvider() {
        return currentProvider;
    }
}
