package org.kopingenieria.application.validators.impl.user;


import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.x509.*;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaSession;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfig;
import org.eclipse.milo.opcua.stack.core.StatusCodes;
import org.eclipse.milo.opcua.stack.core.UaException;
import org.eclipse.milo.opcua.stack.core.security.CertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.SecurityPolicy;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.enumerated.MessageSecurityMode;
import org.eclipse.milo.opcua.stack.core.types.structured.EndpointDescription;
import org.kopingenieria.application.validators.contract.user.UserSessionValidator;
import org.kopingenieria.domain.model.user.UserSessionConfiguration;
import org.kopingenieria.util.loader.CertificateLoader;
import org.kopingenieria.util.security.crl.CrlConfig;
import org.springframework.beans.factory.annotation.Autowired;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.cert.*;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class UserSessionValidatorImpl implements UserSessionValidator {

    @Autowired
    private UserSessionConfiguration sessionConfig;
    @Autowired
    private UserAuthenticationValidatorImpl authValidator;

    private final double DEFAULT_SESSION_TIMEOUT; // 1 hora en milisegundos
    private final StringBuilder validationResult;
    private static final Set<String> SUPPORTED_CRITICAL_EXTENSIONS = new HashSet<>(Arrays.asList(
            "2.5.29.15",  // KeyUsage
            "2.5.29.19",  // Basic Constraints
            "2.5.29.37"   // Extended Key Usage
    ));

    public UserSessionValidatorImpl() {
        this.validationResult = new StringBuilder();
        UserSessionConfiguration build = UserSessionConfiguration.builder().build();
        DEFAULT_SESSION_TIMEOUT = build.getTimeout().getDuration();
    }

    @Override
    public boolean validateSession(UaClient client) {
        if (client == null) {
            logValidationError("Cliente OPC UA nulo");
            return false;
        }

        try {
            CompletableFuture<Boolean> sessionValid = client.getSession().thenCompose(session -> {
                if (session == null) {
                    return CompletableFuture.completedFuture(false);
                }
                NodeId sessionId = session.getSessionId();
                return CompletableFuture.completedFuture(!NodeId.NULL_VALUE.equals(sessionId));
            });

            boolean isValid = sessionValid.get(5, TimeUnit.SECONDS);
            if (!isValid) {
                logValidationError("Sesión no válida o no establecida");
                return false;
            }

            logValidationSuccess("Sesión validada correctamente");
            return true;
        } catch (Exception e) {
            logValidationError("Error al validar sesión: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logValidationError("Token de sesión inválido");
            return false;
        }

        // En Milo, el token normalmente es un identificador único
        NodeId.parse(token);
        logValidationSuccess("Token de sesión válido");
        return true;
    }

    @Override
    public boolean isSessionActive(UserSessionConfiguration user) {
        if (user == null) {
            logValidationError("Configuración de usuario nula");
            return false;
        }
        return !user.getSessionStatus().isExpired() & !isSessionExpired(user);
    }

    @Override
    public boolean isSessionExpired(UserSessionConfiguration user) {
        if (user == null || user.getSessionStatus().isExpired()) {
            return true;
        }

        try {
            LocalDateTime lastActivity = user.getLastActivity();
            double elapsed = ChronoUnit.MILLIS.between(lastActivity, LocalDateTime.now());

            boolean expired = elapsed > user.getTimeout().toMilliseconds();

            if (expired) {
                logValidationError("Sesión expirada");
                return true;
            }

            logValidationSuccess("Sesión vigente");
            return false;
        } catch (Exception e) {
            logValidationError("Error al verificar expiración: " + e.getMessage());
            return true;
        }
    }

    @Override
    public boolean validateSessionSecurityPolicy(UaClient client, UserSessionConfiguration user) {
        try {
            EndpointDescription endpoint = client.getConfig().getEndpoint();
            SecurityPolicy clientPolicy = SecurityPolicy.fromUri(endpoint.getSecurityPolicyUri());
            org.kopingenieria.domain.enums.security.SecurityPolicy requiredPolicy = user.getSecurityPolicy();

            if (!Objects.equals(clientPolicy.name(), requiredPolicy.name())) {
                logValidationError("Política de seguridad no coincidente: " +
                        clientPolicy + " vs " + requiredPolicy);
                return false;
            }

            logValidationSuccess("Política de seguridad válida");
            return true;
        } catch (Exception e) {
            logValidationError("Error al validar política de seguridad: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionSecurityMode(UaClient client, UserSessionConfiguration user) {
        try {
            EndpointDescription endpoint = client.getConfig().getEndpoint();
            MessageSecurityMode clientMode = endpoint.getSecurityMode();
            org.kopingenieria.domain.enums.security.MessageSecurityMode requiredMode = user.getSecurityMode();

            if (!Objects.equals(clientMode.name(), requiredMode.name())) {
                logValidationError("Modo de seguridad no coincidente: " +
                        clientMode + " vs " + requiredMode);
                return false;
            }

            logValidationSuccess("Modo de seguridad válido");
            return true;
        } catch (Exception e) {
            logValidationError("Error al validar modo de seguridad: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionCertificate(UaClient client, UserSessionConfiguration user) {
        try {
            OpcUaClientConfig config = client.getConfig();

            if (config.getCertificate().isEmpty()) {
                logValidationError("Certificado del cliente no configurado");
                return false;
            }

            Optional<X509Certificate> clientCertificate = config.getCertificate();
            X509Certificate certificate = clientCertificate.orElseThrow(() ->
                    new UaException(StatusCodes.Bad_SecurityChecksFailed, "Certificado no presente"));

            if (!validateCertificateBasics(certificate)) {
                return false;
            }

            CertificateValidator certificateValidator = config.getCertificateValidator();
            if (!validateCertificateChain(certificate, certificateValidator, config)) {
                return false;
            }

            if (!validateUserSpecificCertificate(certificate, user)) {
                return false;
            }

            if (!validateCertificateUsage(certificate)) {
                return false;
            }

            if (!validateCertificateExtensions(certificate)) {
                return false;
            }

            logValidationSuccess("Certificado validado exitosamente");
            return true;

        } catch (UaException e) {
            logValidationError("Error de validación de certificado: ");
            return false;
        } catch (Exception e) {
            logValidationError("Error al validar certificado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateSessionTimeout(UaClient client) {
        try {
            Double timeout = client.getSession()
                    .thenApply(UaSession::getSessionTimeout)
                    .get(5, TimeUnit.SECONDS);

            if (timeout == null || timeout <= 0) {
                logValidationError("Timeout de sesión inválido");
                return false;
            }

            if (timeout > DEFAULT_SESSION_TIMEOUT) {
                logValidationError("Timeout de sesión excede el límite permitido");
                return false;
            }

            logValidationSuccess("Timeout de sesión válido: " + timeout + "ms");
            return true;
        } catch (Exception e) {
            logValidationError("Error al validar timeout de sesión: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getValidationResult() {
        return validationResult.toString();
    }

    private void logValidationError(String message) {
        validationResult.append("Error: ").append(message).append("\n");
    }

    private void logValidationSuccess(String message) {
        validationResult.append("Éxito: ").append(message).append("\n");
    }

    private boolean validateCertificateBasics(X509Certificate certificate) {
        try {
            certificate.checkValidity();
            return true;
        } catch (CertificateExpiredException e) {
            logValidationError("El certificado ha expirado");
            return false;
        } catch (CertificateNotYetValidException e) {
            logValidationError("El certificado aún no es válido");
            return false;
        }
    }

    private boolean validateCertificateChain(X509Certificate certificate,
                                             CertificateValidator validator,
                                             OpcUaClientConfig config) {
        try {
            List<X509Certificate> certificateChain = new ArrayList<>();
            certificateChain.add(certificate);

            config.getCertificateChain().ifPresent(chain -> certificateChain.addAll(List.of(chain)));

            validator.validateCertificateChain(certificateChain);
            return true;
        } catch (UaException e) {
            logValidationError("Error en la validación de la cadena de certificados: " + e.getMessage());
            return false;
        }
    }

    private boolean validateUserSpecificCertificate(X509Certificate certificate,
                                                    UserSessionConfiguration user) throws CertificateException, IOException {
        if (user.getClientCertificate() != null) {
            X509Certificate userCert = CertificateLoader.loadX509Certificate(user.getClientCertificate());

            if (!certificate.getSubjectX500Principal().equals(userCert.getSubjectX500Principal())) {
                logValidationError("El subject del certificado no coincide con el configurado");
                return false;
            }

            if (!certificate.getPublicKey().equals(userCert.getPublicKey())) {
                logValidationError("La clave pública del certificado no coincide");
                return false;
            }
        }
        return true;
    }

    private boolean validateCertificateUsage(X509Certificate certificate) {
        boolean[] keyUsage = certificate.getKeyUsage();
        if (keyUsage != null) {
            if (!keyUsage[0]) { // digitalSignature
                logValidationError("El certificado no permite firma digital");
                return false;
            }
            if (!keyUsage[2]) { // keyEncipherment
                logValidationError("El certificado no permite cifrado de clave");
                return false;
            }
        }

        try {
            List<String> extendedKeyUsage = certificate.getExtendedKeyUsage();
            if (extendedKeyUsage != null &&
                    !extendedKeyUsage.contains("1.3.6.1.5.5.7.3.1")) { // TLS Client Authentication
                logValidationError("El certificado no está habilitado para autenticación de cliente TLS");
                return false;
            }
        } catch (CertificateParsingException e) {
            logValidationError("Error al analizar el uso extendido de clave");
            return false;
        }

        return true;
    }

    private boolean validateCertificateExtensions(X509Certificate certificate) {
        Set<String> criticalExtensions = certificate.getCriticalExtensionOIDs();
        if (criticalExtensions != null) {
            for (String oid : criticalExtensions) {
                if (!SUPPORTED_CRITICAL_EXTENSIONS.contains(oid)) {
                    logValidationError("Extensión crítica no soportada: " + oid);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateCertificateRevocation(X509Certificate certificate, CrlConfig crlConfig) {
        try {
            // Verificar CRLs desde puntos de distribución del certificado
            List<String> crlUrls = getCrlDistributionPoints(certificate);

            for (String url : crlUrls) {
                try {
                    CRL crl = downloadCrl(new URL(url));
                    if (crl.isRevoked(certificate)) {
                        logValidationError("El certificado está revocado según CRL: " + url);
                        return false;
                    }
                } catch (Exception e) {
                    logValidationError("Error al verificar CRL " + url + ": " + e.getMessage());
                    // Continuar con el siguiente CRL si hay error
                }
            }

            // Verificar contra CRL local si está configurado
            if (crlConfig.getLocalCrlFile() != null) {
                try {
                    CRL localCrl = loadLocalCrl(crlConfig.getLocalCrlFile());
                    if (localCrl.isRevoked(certificate)) {
                        logValidationError("El certificado está revocado según CRL local");
                        return false;
                    }
                } catch (Exception e) {
                    logValidationError("Error al cargar CRL local: " + e.getMessage());
                    return false;
                }
            }

            // Verificar contra CRL en memoria si está disponible
            Optional<CRL> memCrl = crlConfig.getCertificateRevocationList();
            if (memCrl.isPresent() && memCrl.get().isRevoked(certificate)) {
                logValidationError("El certificado está revocado según CRL en memoria");
                return false;
            }

            return true;
        } catch (Exception e) {
            logValidationError("Error en la validación de revocación: " + e.getMessage());
            return false;
        }
    }

    private List<String> getCrlDistributionPoints(X509Certificate certificate) throws IOException {
        List<String> crlUrls = new ArrayList<>();
        byte[] crlDPExtension = certificate.getExtensionValue("2.5.29.31");

        if (crlDPExtension != null) {
            ASN1InputStream asn1In = new ASN1InputStream(new ByteArrayInputStream(crlDPExtension));
            DEROctetString derOctetString = (DEROctetString) asn1In.readObject();
            asn1In.close();

            ASN1InputStream asn1In2 = new ASN1InputStream(derOctetString.getOctets());
            ASN1Object asn1Object = asn1In2.readObject();
            asn1In2.close();

            CRLDistPoint distPoint = CRLDistPoint.getInstance(asn1Object);
            for (DistributionPoint dp : distPoint.getDistributionPoints()) {
                DistributionPointName dpn = dp.getDistributionPoint();
                if (dpn != null && dpn.getType() == DistributionPointName.FULL_NAME) {
                    GeneralNames generalNames = GeneralNames.getInstance(dpn.getName());
                    for (GeneralName generalName : generalNames.getNames()) {
                        if (generalName.getTagNo() == GeneralName.uniformResourceIdentifier) {
                            ASN1IA5String derStr = ASN1IA5String.getInstance(generalName.getName());
                            crlUrls.add(derStr.getString());
                        }
                    }
                }
            }
        }
        return crlUrls;
    }

    private CRL downloadCrl(URL url) throws Exception {
        try (InputStream inStream = url.openStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCRL(inStream);
        }
    }

    private CRL loadLocalCrl(String crlPath) throws Exception {
        try (InputStream inStream = new FileInputStream(crlPath)) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            return cf.generateCRL(inStream);
        }
    }
}
