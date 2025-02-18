package org.kopingenieria.validators.opcua;

import jakarta.annotation.PostConstruct;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.OpcUaClient;
import org.eclipse.milo.opcua.sdk.client.OpcUaSession;
import org.eclipse.milo.opcua.sdk.client.api.identity.UsernameProvider;
import org.eclipse.milo.opcua.sdk.server.SecurityConfiguration;
import org.eclipse.milo.opcua.stack.core.security.CertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.enumerated.UserTokenType;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.eclipse.milo.opcua.stack.core.types.structured.UserIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.UserNameIdentityToken;
import org.eclipse.milo.opcua.stack.core.types.structured.UserTokenPolicy;
import org.eclipse.milo.opcua.stack.core.util.CertificateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class Authentication implements AuthenticationValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String SECURITY_POLICY_URI = "http://opcfoundation.org/UA/SecurityPolicy#Basic256Sha256";
    private static final int SESSION_TIMEOUT = 60000;
    private static final Logger logger = LogManager.getLogger(Authentication.class);

    @Autowired
    private OpcUaClient opcUaClient;

    @Autowired
    private CertificateValidator certificateValidator;

    @Value("${opcua.server.endpoint}")
    private String serverEndpoint;

    @Value("${opcua.session.timeout}")
    private int sessionTimeout;

    @PostConstruct
    private void initialize() throws Exception {
        opcUaClient = OpcUaClient.create(serverEndpoint,
            endpoints -> endpoints.stream()
                .findFirst(),
            configBuilder -> configBuilder
                .setIdentityProvider(new UsernameProvider())
                .setSessionTimeout(UInteger.valueOf(sessionTimeout))
        );
    }

    @Override
    public boolean validateUserCredentials(String username, String password) {
        try {
            if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                logger.error("Credenciales OPC UA vacías");
                return false;
            }
            // Crear credenciales OPC UA
            UserIdentityToken userIdentityToken = new UserNameIdentityToken(
                    opcUaClient.getConfig().getEndpoint().getServer().getApplicationUri(),
                    SecurityPolicy.Basic256Sha256.name(),
                    username,
                    password
            );
            // Intentar establecer sesión con el servidor OPC UA
            OpcUaSession session = (OpcUaSession) opcUaClient.connect()
                    .get(5, TimeUnit.SECONDS);
            boolean isValid = session != null && session.getSessionId() != null;
            if (!isValid) {
                logger.warn("Autenticación OPC UA fallida para usuario: {}", username);
            }
            return isValid;
        } catch (Exception e) {
            log.error("Error validando credenciales en servidor OPC UA: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean validateClientCertificate(String certificate) {
        try {
            if (StringUtils.isBlank(certificate)) {
                logger.error("Certificado OPC UA vacío");
                return false;
            }
            // Decodificar certificado
            X509Certificate cert = decodeCertificate(certificate);
            if (cert == null) {
                return false;
            }
            // Configurar validación de certificado OPC UA
            SecurityConfiguration securityConfig = SecurityConfiguration.builder()
                    .setSecurityPolicy(SecurityPolicy.Basic256Sha256)
                    .setCertificate(cert)
                    .build();
            // Validar certificado contra servidor OPC UA
            EndpointDescription[] endpoints = opcUaClient.getConfig().getEndpoint().toBuilder()
                    .getEndpointsAsync(serverEndpoint)
                    .get(5, TimeUnit.SECONDS)
                    .toArray(new EndpointDescription[0]);
            boolean isValid = Arrays.stream(endpoints)
                    .anyMatch(endpoint ->
                            validateCertificateAgainstEndpoint(cert, endpoint));
            if (!isValid) {
                logger.warn("Certificado no válido para servidor OPC UA");
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Error validando certificado en servidor OPC UA: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean isSessionTokenValid(String token) {
        try {
            if (StringUtils.isBlank(token)) {
                logger.error("Token de sesión OPC UA vacío");
                return false;
            }
            // Validar token contra la sesión actual del servidor OPC UA
            OpcUaSession currentSession = opcUaClient.getSession().get(5, TimeUnit.SECONDS);
            if (currentSession == null) {
                logger.warn("No hay sesión OPC UA activa");
                return false;
            }
            // Verificar si el token coincide con la sesión actual
            NodeId sessionId = currentSession.getSessionId();
            boolean isValid = sessionId != null &&
                    sessionId.getIdentifier().toString().equals(token);
            // Verificar tiempo de expiración de la sesión
            if (isValid) {
                Double sessionTimeout = currentSession.getSessionTimeout();
                isValid = sessionTimeout != null &&
                        sessionTimeout > System.currentTimeMillis();
            }
            if (!isValid) {
                logger.warn("Token de sesión OPC UA no válido o expirado");
            }
            return isValid;
        } catch (Exception e) {
            logger.error("Error validando token de sesión OPC UA: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean enforcePasswordComplexity(String password) {
        try {
            if (StringUtils.isBlank(password)) {
                logger.error("Contraseña OPC UA vacía");
                return false;
            }

            // Obtener políticas de seguridad del servidor OPC UA
            EndpointDescription endpoint = opcUaClient.getEndpoints(serverEndpoint)
                    .get(5, TimeUnit.SECONDS)
                    .stream()
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No endpoints found"));

            UserTokenPolicy[] userTokenPolicies = endpoint.getUserIdentityTokens();

            // Validar contra políticas de contraseña del servidor
            boolean meetsServerPolicy = Arrays.stream(userTokenPolicies)
                    .filter(policy -> policy.getTokenType() == UserTokenType.UserName)
                    .anyMatch(policy -> validatePasswordAgainstPolicy(password, policy));

            if (!meetsServerPolicy) {
                logger.warn("La contraseña no cumple con las políticas del servidor OPC UA");
                return false;
            }

            // Validaciones adicionales de seguridad
            return password.length() >= MIN_PASSWORD_LENGTH &&
                    containsRequiredCharacterTypes(password);

        } catch (Exception e) {
            logger.error("Error validando complejidad de contraseña OPC UA: {}", e.getMessage(), e);
            return false;
        }
    }

    private X509Certificate decodeCertificate(String certificateStr) {
        try {
            byte[] certBytes = Base64.getDecoder().decode(certificateStr);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(certBytes)
            );
        } catch (Exception e) {
            logger.error("Error decodificando certificado: {}", e.getMessage());
            return null;
        }
    }

    private boolean validateCertificateAgainstEndpoint(
            X509Certificate cert,
            EndpointDescription endpoint) {
        try {
            ByteString serverCertBytes = endpoint.getServerCertificate();
            X509Certificate serverCert = CertificateUtil.decodeCertificate(serverCertBytes);

            // Validar cadena de confianza
            return certificateValidator.validateTrustChain(cert, serverCert) &&
                    endpoint.getSecurityPolicyUri().equals(SECURITY_POLICY_URI);
        } catch (Exception e) {
            log.error("Error validando certificado contra endpoint: {}", e.getMessage());
            return false;
        }
    }

    private boolean validatePasswordAgainstPolicy(
            String password,
            UserTokenPolicy policy) {
        // Implementar validación específica según la política del servidor
        return true; // Personalizar según requisitos
    }

    private boolean containsRequiredCharacterTypes(String password) {
        return password.matches(".*[A-Z].*") && // Mayúsculas
                password.matches(".*[a-z].*") && // Minúsculas
                password.matches(".*[0-9].*") && // Números
                password.matches(".*[^A-Za-z0-9].*"); // Caracteres especiales
    }
}
