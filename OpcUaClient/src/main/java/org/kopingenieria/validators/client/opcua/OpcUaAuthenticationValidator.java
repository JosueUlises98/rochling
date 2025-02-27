package org.kopingenieria.validators.client.opcua;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import java.util.concurrent.CompletableFuture;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.LocalizedText;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UByte;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.Unsigned;
import org.eclipse.milo.opcua.stack.core.types.enumerated.ApplicationType;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.ApplicationDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.springframework.beans.factory.annotation.Value;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateFactory;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.regex.Pattern;

public class OpcUaAuthenticationValidator implements AuthenticationValidator {

    private static final Logger logger = LogManager.getLogger(OpcUaAuthenticationValidator.class);

    @Value("${opcua.server.endpoint}")
    private String opcUaEndpoint;

    @Value("${opcua.session.timeout}")
    private long sessionTimeoutMinutes;

    private final Pattern passwordPattern;

    public OpcUaAuthenticationValidator() {
        // Patrón para validar complejidad de contraseña
        this.passwordPattern = Pattern.compile(
                "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$"
        );
    }

    @Override
    public boolean validateUserCredentials(String username, String password) {
        try {
            logger.debug("Validando credenciales para usuario: {}", username);

            // Configurar los parámetros de autenticación
            UsernameProvider identityProvider = new UsernameProvider(username, password);

            // Crear configuración del cliente
            OpcUaClientConfig config = OpcUaClientConfig.builder()
                    .setIdentityProvider(identityProvider)
                    .setEndpoint(createSecureEndpointWithUserNameAndPassword(opcUaEndpoint))
                    .setRequestTimeout(Unsigned.uint(5000))
                    .build();

            // Intentar conectar al servidor usando las credenciales

            CompletableFuture<OpcUaClient> connect = CompletableFuture.supplyAsync(() -> {
                try {
                    OpcUaClient opcUaClient = OpcUaClient.create(config);
                    opcUaClient.connect();
                    return opcUaClient;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            // Esperar la respuesta y verificar la conexión
            OpcUaClient connectedClient = connect.get();
            boolean isConnected = !connectedClient.getSession().resultNow().isEmpty();

            if (isConnected) {
                connectedClient.disconnect().get();
            }

            return isConnected;

        } catch (Exception e) {
            logger.error("Error al validar credenciales: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateClientCertificate(String certificate) {
        try {
            logger.debug("Validando certificado del cliente");

            // Decodificar certificado de Base64
            byte[] certBytes = Base64.getDecoder().decode(certificate);
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));

            // Configurar parámetros de seguridad para validación de certificado
            OpcUaClientConfig config = OpcUaClientConfig.builder()
                    .setCertificate(x509Cert)
                    .setEndpoint(createSecureEndpointWihCertificate(opcUaEndpoint, x509Cert))
                    .build();

            CompletableFuture<OpcUaClient> connect = CompletableFuture.supplyAsync(() -> {
                try {
                    OpcUaClient opcUaClient = OpcUaClient.create(config);
                    opcUaClient.connect();
                    return opcUaClient;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
            // Si la conexión es exitosa, el certificado es válido
            OpcUaClient connectedClient = connect.get();
            boolean isValid = !connectedClient.getSession().resultNow().isEmpty();

            if (isValid) {
                connectedClient.disconnect().get();
            }

            return isValid;

        } catch (Exception e) {
            logger.error("Error al validar certificado: {}", e.getMessage());
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
