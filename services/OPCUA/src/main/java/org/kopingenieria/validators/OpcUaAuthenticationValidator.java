package org.kopingenieria.validators;



import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import java.util.concurrent.CompletableFuture;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ApplicationType;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.ApplicationDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.kopingenieria.application.service.opcua.OpcuaConnection;
import org.kopingenieria.exception.OpcUaConfigurationException;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Pattern;

public class OpcUaAuthenticationValidator implements AuthenticationValidator {

    private final Pattern passwordPattern;
    private final OpcuaConnection initialConnection;

    public OpcUaAuthenticationValidator() throws OpcUaConfigurationException {
        initialConnection=new OpcuaConnection();
        this.passwordPattern = Pattern.compile(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        );
    }

    @Override
    public boolean validateUserCredentials(String username, String password) {
        try {
            // Intentar conectar al servidor usando las credenciales

            boolean connectedClient = initialConnection.connect().get();

            if (connectedClient) {
                initialConnection.disconnect().get();
            }

            return connectedClient;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean validateClientCertificate(String certificate) {
        try {
            CompletableFuture<OpcUaClient> connect = CompletableFuture.supplyAsync(() -> {
                    // Intentar conectar al servidor usando las credenciales

                    boolean connectedClient = initialConnection.connect().get();

                    if (connectedClient) {
                        initialConnection.disconnect().get();
                    }

                    return connectedClient;
            });
            // Si la conexión es exitosa, el certificado es válido
            OpcUaClient connectedClient = connect.get();
            boolean isValid = !connectedClient.getSession().resultNow().isEmpty();

            if (isValid) {
                connectedClient.disconnect().get();
            }

            return isValid;

        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isSessionTokenValid(String token) {
        try {
            logger.debug("Verificando validez del token de sesión");

            String[] parts = token.split("\\.");
            long timestamp = Long.parseLong(parts[1]);

            Duration elapsed = Duration.between(
                    Instant.ofEpochMilli(timestamp),
                    Instant.now()
            );

            return elapsed.toMinutes() < sessionTimeoutMinutes;

        } catch (Exception e) {
            logger.error("Error al validar token de sesión: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean enforcePasswordComplexity(String password) {
        try {
            logger.debug("Validando complejidad de contraseña");
            return passwordPattern.matcher(password).matches();
        } catch (Exception e) {
            logger.error("Error al validar complejidad de contraseña: {}", e.getMessage());
            return false;
        }
    }

    private EndpointDescription createSecureEndpointWihCertificate(String serverUrl, X509Certificate serverCertificate) throws Exception {
        // Crear ApplicationDescription
        ApplicationDescription applicationDescription = new ApplicationDescription(
                "urn:my:server:name",              // applicationUri
                "urn:my:product:uri",              // productUri
                LocalizedText.english("Server Name"), // applicationName
                ApplicationType.Server,             // applicationType
                null,                              // gatewayServerUri
                null,                              // discoveryProfileUri
                new String[]{serverUrl}            // discoveryUrls
        );

        // Crear UserTokenPolicy
        UserTokenPolicy[] userTokenPolicies = new UserTokenPolicy[]{
                new UserTokenPolicy(
                        "user_pass",              // policyId
                        UserTokenType.UserName,          // tokenType
                        null,                            // issuedTokenType
                        null,                            // issuerEndpointUrl
                        SecurityPolicy.Basic256Sha256.getUri() // securityPolicyUri
                )
        };

        return new EndpointDescription(
                serverUrl,                                    // endpointUrl
                applicationDescription,                       // server
                ByteString.of(serverCertificate.getEncoded()), // serverCertificate
                MessageSecurityMode.SignAndEncrypt,          // securityMode
                SecurityPolicy.Basic256Sha256.getUri(),      // securityPolicyUri
                userTokenPolicies,                           // userIdentityTokens
                "ocp.tcp",                                   // transportProfileUri
                UByte.valueOf(0)                             // securityLevel
        );
    }

    private EndpointDescription createSecureEndpointWithUserNameAndPassword(String serverUrl){
        // Crear ApplicationDescription
        ApplicationDescription applicationDescription = new ApplicationDescription(
                "urn:my:server:name",              // applicationUri
                "urn:my:product:uri",              // productUri
                LocalizedText.english("Server Name"), // applicationName
                ApplicationType.Server,             // applicationType
                null,                              // gatewayServerUri
                null,                              // discoveryProfileUri
                new String[]{serverUrl}            // discoveryUrls
        );

        // Crear UserTokenPolicy
        UserTokenPolicy[] userTokenPolicies = new UserTokenPolicy[]{
                new UserTokenPolicy(
                        "user_pass",              // policyId
                        UserTokenType.UserName,          // tokenType
                        null,                            // issuedTokenType
                        null,                            // issuerEndpointUrl
                        SecurityPolicy.Basic256Sha256.getUri() // securityPolicyUri
                )
        };

        return new EndpointDescription(
                serverUrl,                                    // endpointUrl
                applicationDescription,                       // server
                null,                                        // serverCertificate
                MessageSecurityMode.SignAndEncrypt,          // securityMode
                SecurityPolicy.Basic256Sha256.getUri(),      // securityPolicyUri
                userTokenPolicies,                           // userIdentityTokens
                "opc.tcp",                                   // transportProfileUri
                UByte.valueOf(0)                             // securityLevel
        );
    }

}
