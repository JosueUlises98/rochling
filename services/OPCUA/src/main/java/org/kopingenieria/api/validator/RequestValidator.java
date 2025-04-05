package org.kopingenieria.api.validator;

import lombok.RequiredArgsConstructor;
import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.api.request.configuration.UserConfigRequest;
import org.kopingenieria.api.request.connection.OpcUaSessionRequest;
import org.kopingenieria.api.request.connection.bydefault.DefaultConnectionRequest;
import org.kopingenieria.api.request.security.bydefault.OpcUaAuthenticationRequest;
import org.kopingenieria.api.request.security.bydefault.OpcUaEncryptionRequest;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;
import org.kopingenieria.domain.enums.security.IdentityProvider;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    public void validateAuthenticationParameters(OpcUaAuthenticationRequest request, List<String> errors) {
        // Validar proveedor de identidad
        if (request.getIdentityProvider() != null) {
            validateIdentityProvider(request.getIdentityProvider(), errors);
        }

        // Validar credenciales según el proveedor de identidad
        if (request.getIdentityProvider() == IdentityProvider.USERNAME) {
            validateUsernameCredentials(request.getUserName(), request.getPassword(), errors);
        }

        // Validar política de seguridad
        if (request.getSecurityPolicy() != null) {
            validateSecurityPolicy(request.getSecurityPolicy(), errors);
        }

        // Validar modo de seguridad del mensaje
        if (request.getMessageSecurityMode() != null) {
            validateMessageSecurityMode(request.getMessageSecurityMode(), errors);
        }

        // Validar rutas de certificados y claves
        validateCertificateConfiguration(request, errors);
    }

    public void validateConnectionParameters(DefaultConnectionRequest request, List<String> errors) {
        // Validar endpoint URL
        if (request.getEndpointUrl() != null) {
            validateEndpointUrl(String.valueOf(request.getEndpointUrl()), errors);
        }
        // Validar nombre de aplicación
        if (request.getApplicationName() != null) {
            validateApplicationName(request.getApplicationName(), errors);
        }
        // Validar URI de aplicación
        if (request.getApplicationUri() != null) {
            validateApplicationUri(request.getApplicationUri(), errors);
        }
        // Validar URI de producto
        if (request.getProductUri() != null) {
            validateProductUri(request.getProductUri(), errors);
        }
        // Validar tipo de conexión
        if (request.getType() != null) {
            validateConnectionType(request.getType(), errors);
        }
        // Validar timeouts
        if (request.getTimeout() != null) {
            validateTimeouts(request.getTimeout(), errors);
        }
    }

    public void validateEncryptionParameters(OpcUaEncryptionRequest request, List<String> errors) {
        // Validar política de seguridad
        if (request.getSecurityPolicy() != null) {
            validateEncryptionSecurityPolicy(String.valueOf(request.getSecurityPolicy()), errors);
        }

        // Validar modo de seguridad del mensaje
        if (request.getMessageSecurityMode() != null) {
            validateEncryptionSecurityMode(String.valueOf(request.getMessageSecurityMode()),
                    String.valueOf(request.getSecurityPolicy()), errors);
        }

        // Validar certificado del cliente
        if (request.getClientCertificate() != null) {
            validateCertificateBytes(request.getClientCertificate(),
                    "certificado del cliente", errors);
        }

        // Validar clave privada
        if (request.getPrivateKey() != null) {
            validatePrivateKeyBytes(request.getPrivateKey(), errors);
        }

        // Validar certificados de confianza
        if (request.getTrustedCertificates() != null) {
            validateTrustedCertificates(request.getTrustedCertificates(), errors);
        }

        // Validar longitud de clave
        if (request.getKeyLength() != null) {
            validateKeyLength(String.valueOf(request.getKeyLength()), errors);
        }

        // Validar algoritmo
        if (request.getAlgorithmName() != null) {
            validateAlgorithm(String.valueOf(request.getAlgorithmName()), errors);
        }

        // Validar versión del protocolo
        if (request.getProtocolVersion() != null) {
            validateProtocolVersion(request.getProtocolVersion(), errors);
        }
    }

    public void validateSessionParameters(OpcUaSessionRequest request, List<String> errors) {
        // Validar nombre de sesión
        if (request.getSessionName() != null) {
            validateSessionName(request.getSessionName(), errors);
        }

        // Validar URI del servidor
        if (request.getServerUri() != null) {
            validateServerUri(request.getServerUri(), errors);
        }

        // Validar tamaño máximo de mensaje de respuesta
        if (request.getMaxResponseMessageSize() != null) {
            validateMaxResponseMessageSize(request.getMaxResponseMessageSize(), errors);
        }

        // Validar modo de seguridad
        if (request.getSecurityMode() != null) {
            validateSessionSecurityMode(request.getSecurityMode(), errors);
        }

        // Validar URI de política de seguridad
        if (request.getSecurityPolicyUri() != null) {
            validateSecurityPolicyUri(request.getSecurityPolicyUri(), errors);
        }

        // Validar certificados
        validateSessionCertificates(request, errors);

        // Validar IDs de localización
        if (request.getLocaleIds() != null) {
            validateLocaleIds(request.getLocaleIds(), errors);
        }

        // Validar conteo máximo de fragmentos
        if (request.getMaxChunkCount() != null) {
            validateMaxChunkCount(request.getMaxChunkCount(), errors);
        }

        // Validar tiempo de espera
        if (request.getTimeout() != null) {
            validateSessionTimeout(request.getTimeout().toMilliseconds(), errors);
        }
    }

    //Metodos auxiliares de validacion de autenticacion
    private void validateIdentityProvider(IdentityProvider provider, List<String> errors) {
        try {
            switch (provider) {
                case ANONYMOUS:
                case USERNAME:
                case X509IDENTITY:
                    break;
                default:
                    errors.add("Proveedor de identidad no soportado: " + provider);
            }
        } catch (IllegalArgumentException e) {
            errors.add("Proveedor de identidad inválido");
        }
    }

    private void validateUsernameCredentials(String username, String password, List<String> errors) {
        // Validar nombre de usuario
        if (username != null) {
            if (username.length() < AuthenticationConstants.MIN_USERNAME_LENGTH || username.length() > AuthenticationConstants.MAX_USERNAME_LENGTH) {
                errors.add(String.format("El nombre de usuario debe tener entre %d y %d caracteres",
                        AuthenticationConstants.MIN_USERNAME_LENGTH, AuthenticationConstants.MAX_USERNAME_LENGTH));
            }
            if (!AuthenticationConstants.USERNAME_PATTERN.matcher(username).matches()) {
                errors.add("El nombre de usuario contiene caracteres no permitidos");
            }
        }
        // Validar contraseña
        if (password != null) {
            if (password.length() < AuthenticationConstants.MIN_PASSWORD_LENGTH || password.length() > AuthenticationConstants.MAX_PASSWORD_LENGTH) {
                errors.add(String.format("La contraseña debe tener entre %d y %d caracteres",
                        AuthenticationConstants.MIN_PASSWORD_LENGTH, AuthenticationConstants.MAX_PASSWORD_LENGTH));
            }
            validatePasswordComplexity(password, errors);
        }
    }

    private void validatePasswordComplexity(String password, List<String> errors) {
        boolean hasUpperCase = false;
        boolean hasLowerCase = false;
        boolean hasDigit = false;
        boolean hasSpecial = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpperCase = true;
            if (Character.isLowerCase(c)) hasLowerCase = true;
            if (Character.isDigit(c)) hasDigit = true;
            if (!Character.isLetterOrDigit(c)) hasSpecial = true;
        }

        if (!(hasUpperCase && hasLowerCase && hasDigit && hasSpecial)) {
            errors.add("La contraseña debe contener mayúsculas, minúsculas, números y caracteres especiales");
        }
    }

    private void validateSecurityPolicy(SecurityPolicy policy, List<String> errors) {
        try {
            switch (policy) {
                case NONE:
                case BASIC128RSA15:
                case BASIC256:
                case BASIC256SHA256:
                case AES128_SHA256_RSAOAEP:
                case AES256_SHA256_RSAPSS:
                    break;
                default:
                    errors.add("Política de seguridad no soportada: " + policy);
            }
        } catch (IllegalArgumentException e) {
            errors.add("Política de seguridad inválida");
        }
    }

    private void validateMessageSecurityMode(MessageSecurityMode mode,
                                             List<String> errors) {
        try {
            switch (mode) {
                case NONE:
                case SIGN:
                case SIGNANDENCRYPT:
                    break;
                default:
                    errors.add("Modo de seguridad no soportado: " + mode);
            }
        } catch (IllegalArgumentException e) {
            errors.add("Modo de seguridad inválido");
        }
    }

    private void validateCertificateConfiguration(OpcUaAuthenticationRequest request,
                                                  List<String> errors) {
        // Validar ruta del certificado
        validateCertificatePath(request.getCertificatePath(), "certificado", errors);

        // Validar ruta de la clave privada
        validateCertificatePath(request.getPrivateKeyPath(), "clave privada", errors);

        // Validar ruta de la lista de confianza
        validateCertificatePath(request.getTrustListPath(), "lista de confianza", errors);

        // Validar ruta de la lista resolutora
        validateCertificatePath(request.getIssuerListPath(), "lista resolutora", errors);

        // Validar ruta de la lista de revocación
        validateCertificatePath(request.getRevocationListPath(), "lista de revocación", errors);
    }

    private void validateCertificatePath(String path, String type, List<String> errors) {
        if (path != null) {
            File file = new File(path);

            // Validar existencia
            if (!file.exists()) {
                errors.add(String.format("El archivo de %s no existe: %s", type, path));
                return;
            }

            // Validar permisos
            if (!file.canRead()) {
                errors.add(String.format("No hay permisos de lectura para el archivo de %s: %s",
                        type, path));
            }

            // Validar extensión
            String extension = getFileExtension(path).toLowerCase();
            if (type.contains("clave")) {
                if (!AuthenticationConstants.VALID_KEY_EXTENSIONS.contains(extension)) {
                    errors.add(String.format("Extensión no válida para archivo de %s: %s",
                            type, extension));
                }
            } else {
                if (!AuthenticationConstants.VALID_CERTIFICATE_EXTENSIONS.contains(extension)) {
                    errors.add(String.format("Extensión no válida para archivo de %s: %s",
                            type, extension));
                }
            }
        }
    }

    private String getFileExtension(String path) {
        int lastDotIndex = path.lastIndexOf('.');
        return lastDotIndex > 0 ? path.substring(lastDotIndex) : "";
    }
    //Metodos auxiliares de validacion de conexion
    private void validateEndpointUrl(String endpointUrl, List<String> errors) {
        if (!endpointUrl.startsWith("opc.tcp://")) {
            errors.add("El endpoint URL debe comenzar con 'opc.tcp://'");
            return;
        }
        try {
            URI uri = new URI(endpointUrl);
            String host = uri.getHost();
            int port = uri.getPort();
            if (host == null || host.isEmpty()) {
                errors.add("El host en el endpoint URL no es válido");
            }
            if (port != -1 && (port < ConnectionConstants.MIN_PORT || port > ConnectionConstants.MAX_PORT)) {
                errors.add(String.format("El puerto debe estar entre %d y %d", ConnectionConstants.MIN_PORT, ConnectionConstants.MAX_PORT));
            }
            if (!ConnectionConstants.URL_PATTERN.matcher(endpointUrl).matches()) {
                errors.add("Formato de endpoint URL inválido");
            }
        } catch (URISyntaxException e) {
            errors.add("El endpoint URL tiene un formato inválido: " + e.getMessage());
        }
    }

    private void validateApplicationName(String applicationName, List<String> errors) {
        if (applicationName.length() < 3 || applicationName.length() > 100) {
            errors.add("El nombre de la aplicación debe tener entre 3 y 100 caracteres");
        }
        if (!applicationName.matches("^[\\w\\s.-]+$")) {
            errors.add("El nombre de la aplicación solo puede contener letras, números, espacios, puntos y guiones");
        }
    }

    private void validateApplicationUri(String applicationUri, List<String> errors) {
        if (!applicationUri.startsWith("urn:")) {
            errors.add("El URI de la aplicación debe comenzar con 'urn:'");
        }
        String[] segments = applicationUri.split(":");
        if (segments.length < 3) {
            errors.add("El URI de la aplicación debe tener al menos 3 segmentos (urn:namespace:identifier)");
        }
        if (!applicationUri.matches("^urn:[\\w.-]+:[\\w.-]+(?:/[\\w.-]+)*$")) {
            errors.add("Formato de URI de aplicación inválido");
        }
    }

    private void validateProductUri(String productUri, List<String> errors) {
        if (!productUri.startsWith("urn:")) {
            errors.add("El URI del producto debe comenzar con 'urn:'");
        }
        if (!productUri.matches("^urn:[\\w.-]+:[\\w.-]+(?:/[\\w.-]+)*$")) {
            errors.add("Formato de URI de producto inválido");
        }
    }

    private void validateConnectionType(ConnectionType type, List<String> errors) {
        try {
            switch (type) {
                case OPCUA:
                    break;
                default:
                    errors.add("Tipo de conexión no soportado: " + type);
            }
        } catch (IllegalArgumentException e) {
            errors.add("Tipo de conexión inválido");
        }
    }

    private void validateTimeouts(Timeouts timeout, List<String> errors) {
        // Validar timeout de conexión
        if (timeout.toMilliseconds() < 1000 || timeout.toMilliseconds() > 300000) {
            errors.add("El timeout de conexión debe estar entre 1000ms y 300000ms");
        }
    }
    //Metodos de validacion de encriptacion
    private void validateEncryptionSecurityPolicy(String securityPolicy, List<String> errors) {
        if (!EncryptionConstants.VALID_SECURITY_POLICIES.contains(securityPolicy)) {
            errors.add("Política de seguridad no válida: " + securityPolicy);
            errors.add("Políticas válidas: " + String.join(", ", EncryptionConstants.VALID_SECURITY_POLICIES));
        }
    }

    private void validateEncryptionSecurityMode(String securityMode,
                                                String securityPolicy,
                                                List<String> errors) {
        if (!EncryptionConstants.VALID_SECURITY_MODES.contains(securityMode)) {
            errors.add("Modo de seguridad no válido: " + securityMode);
            errors.add("Modos válidos: " + String.join(", ", EncryptionConstants.VALID_SECURITY_MODES));
        }
        // Validar coherencia entre política y modo
        if ("None".equals(securityPolicy) && !"None".equals(securityMode)) {
            errors.add("El modo de seguridad debe ser 'None' cuando la política es 'None'");
        }
    }

    private void validateCertificateBytes(byte[] certificate, String type, List<String> errors) {
        if (certificate.length < EncryptionConstants.MIN_CERTIFICATE_LENGTH) {
            errors.add(String.format("El %s es demasiado corto (mínimo %d bytes)",
                    type, EncryptionConstants.MIN_CERTIFICATE_LENGTH));
        }
        if (certificate.length > EncryptionConstants.MAX_CERTIFICATE_LENGTH) {
            errors.add(String.format("El %s es demasiado largo (máximo %d bytes)",
                    type, EncryptionConstants.MAX_CERTIFICATE_LENGTH));
        }
        try {
            CertificateFactory.getInstance("X.509").generateCertificate(
                    new ByteArrayInputStream(certificate)
            );
        } catch (CertificateException e) {
            errors.add(String.format("El %s no es válido: %s", type, e.getMessage()));
        }
    }

    private void validatePrivateKeyBytes(byte[] privateKey, List<String> errors) {
        if (privateKey.length < EncryptionConstants.MIN_PRIVATE_KEY_LENGTH) {
            errors.add(String.format("La clave privada es demasiado corta (mínimo %d bytes)",
                    EncryptionConstants.MIN_PRIVATE_KEY_LENGTH));
        }
        if (privateKey.length > EncryptionConstants.MAX_PRIVATE_KEY_LENGTH) {
            errors.add(String.format("La clave privada es demasiado larga (máximo %d bytes)",
                    EncryptionConstants.MAX_PRIVATE_KEY_LENGTH));
        }
    }

    private void validateTrustedCertificates(List<byte[]> certificates, List<String> errors) {
        if (certificates.isEmpty()) {
            errors.add("La lista de certificados de confianza no puede estar vacía");
            return;
        }
        for (int i = 0; i < certificates.size(); i++) {
            byte[] cert = certificates.get(i);
            if (cert == null) {
                errors.add(String.format("El certificado de confianza #%d es nulo", i + 1));
                continue;
            }
            validateCertificateBytes(cert,
                    String.format("certificado de confianza #%d", i + 1),
                    errors);
        }
    }

    private void validateKeyLength(String keyLength, List<String> errors) {
        try {
            int length = Integer.parseInt(keyLength);
            if (!EncryptionConstants.VALID_KEY_LENGTHS.contains(length)) {
                errors.add("Longitud de clave no válida: " + length);
                errors.add("Longitudes válidas: " +
                        EncryptionConstants.VALID_KEY_LENGTHS.stream()
                                .map(String::valueOf)
                                .collect(Collectors.joining(", ")));
            }
        } catch (NumberFormatException e) {
            errors.add("La longitud de clave debe ser un número entero");
        }
    }

    private void validateAlgorithm(String algorithm, List<String> errors) {
        if (!EncryptionConstants.VALID_ALGORITHMS.contains(algorithm)) {
            errors.add("Algoritmo no válido: " + algorithm);
            errors.add("Algoritmos válidos: " + String.join(", ", EncryptionConstants.VALID_ALGORITHMS));
        }
    }

    private void validateProtocolVersion(String version, List<String> errors) {
        if (!version.matches(EncryptionConstants.PROTOCOL_VERSION_PATTERN)) {
            errors.add("Versión del protocolo no válida: " + version);
            errors.add("El formato debe ser: X.Y.Z donde X, Y, Z son números");
        }
    }
    //Metodos auxiliares de validacion de session
    private void validateSessionName(String sessionName, List<String> errors) {
        if (sessionName.isEmpty() ||
                sessionName.length() > SessionConstants.MAX_SESSION_NAME_LENGTH) {
            errors.add(String.format("El nombre de la sesión debe tener entre %d y %d caracteres",
                    SessionConstants.MIN_SESSION_NAME_LENGTH,
                    SessionConstants.MAX_SESSION_NAME_LENGTH));
        }
        if (!sessionName.matches(SessionConstants.SESSION_NAME_PATTERN)) {
            errors.add("El nombre de la sesión contiene caracteres no válidos");
        }
    }

    private void validateServerUri(String serverUri, List<String> errors) {
        try {
            URI uri = new URI(serverUri);
            if (!uri.isAbsolute()) {
                errors.add("El URI del servidor debe ser absoluto");
            }
            if (!SessionConstants.VALID_URI_SCHEMES.contains(uri.getScheme())) {
                errors.add("Esquema de URI no válido: " + uri.getScheme());
                errors.add("Esquemas válidos: " +
                        String.join(", ", SessionConstants.VALID_URI_SCHEMES));
            }
        } catch (URISyntaxException e) {
            errors.add("URI del servidor no válido: " + e.getMessage());
        }
    }

    private void validateMaxResponseMessageSize(Long maxSize, List<String> errors) {
        if (maxSize > SessionConstants.MAX_RESPONSE_SIZE_LIMIT) {
            errors.add(String.format("El tamaño máximo de respuesta no puede superar %d bytes",
                    SessionConstants.MAX_RESPONSE_SIZE_LIMIT));
        }
    }

    private void validateSessionSecurityMode(String securityMode, List<String> errors) {
        if (!SessionConstants.VALID_SECURITY_MODES.contains(securityMode)) {
            errors.add("Modo de seguridad no válido: " + securityMode);
            errors.add("Modos válidos: " +
                    String.join(", ", SessionConstants.VALID_SECURITY_MODES));
        }
    }

    private void validateSecurityPolicyUri(String policyUri, List<String> errors) {
        if (!SessionConstants.VALID_SECURITY_POLICY_URIS.contains(policyUri)) {
            errors.add("URI de política de seguridad no válido: " + policyUri);
            errors.add("URIs válidos: " +
                    String.join(", ", SessionConstants.VALID_SECURITY_POLICY_URIS));
        }
    }

    private void validateSessionCertificates(OpcUaSessionRequest request, List<String> errors) {
        // Validar certificado del cliente
        if (request.getClientCertificate() != null) {
            validateCertificateString(request.getClientCertificate(),
                    "certificado del cliente", errors);
        }
        // Validar certificado del servidor
        if (request.getServerCertificate() != null) {
            validateCertificateString(request.getServerCertificate(),
                    "certificado del servidor", errors);
        }
    }

    private void validateCertificateString(String certificate, String type, List<String> errors) {
        try {
            byte[] decoded = Base64.getDecoder().decode(certificate);
            if (decoded.length < SessionConstants.MIN_CERTIFICATE_LENGTH ||
                    decoded.length > SessionConstants.MAX_CERTIFICATE_LENGTH) {
                errors.add(String.format("El %s tiene un tamaño no válido", type));
            }
        } catch (IllegalArgumentException e) {
            errors.add(String.format("El %s no está correctamente codificado en Base64", type));
        }
    }

    private void validateLocaleIds(List<String> localeIds, List<String> errors) {
        for (String localeId : localeIds) {
            try {
                Locale locale = Locale.forLanguageTag(localeId);
                if (locale.getLanguage().isEmpty()) {
                    errors.add("ID de localización no válido: " + localeId);
                }
            } catch (IllegalArgumentException e) {
                errors.add("ID de localización malformado: " + localeId);
            }
        }
    }

    private void validateMaxChunkCount(Integer maxChunkCount, List<String> errors) {
        if (maxChunkCount > SessionConstants.MAX_CHUNK_COUNT_LIMIT) {
            errors.add(String.format("El conteo máximo de fragmentos no puede superar %d",
                    SessionConstants.MAX_CHUNK_COUNT_LIMIT));
        }
    }

    private void validateSessionTimeout(Long timeout, List<String> errors) {
        if (timeout > SessionConstants.MAX_TIMEOUT) {
            errors.add(String.format("El tiempo de espera no puede superar %d ms",
                    SessionConstants.MAX_TIMEOUT));
        }
    }

    //Metodos auxiliares de validacion de subscripcion
    private void validateNodeId(String nodeId, List<String> errors) {
        if (!nodeId.matches(SubscriptionConstants.NODE_ID_PATTERN)) {
            errors.add("Formato de NodeId no válido. Debe seguir el patrón: ns=<namespace>;s=<identifier> " +
                    "o i=<numeric identifier>");
        }
    }

    //Metodos auxiliares de validacion de configuracion general de un cliente opcua
    private void validateBasicFields(UserConfigRequest request, List<String> errors) {
        // Validar nombre
        if (request.getName() != null) {
            if (!request.getName().matches(ConfigConstants.NAME_PATTERN)) {
                errors.add("El nombre solo puede contener letras, números, guiones y guiones bajos");
            }
            if (request.getName().length() < ConfigConstants.MIN_NAME_LENGTH ||
                    request.getName().length() > ConfigConstants.MAX_NAME_LENGTH) {
                errors.add(String.format("El nombre debe tener entre %d y %d caracteres",
                        ConfigConstants.MIN_NAME_LENGTH, ConfigConstants.MAX_NAME_LENGTH));
            }
        }
        // Validar descripción
        if (request.getDescription() != null) {
            if (request.getDescription().length() > ConfigConstants.MAX_DESCRIPTION_LENGTH) {
                errors.add(String.format("La descripción no puede exceder los %d caracteres",
                        ConfigConstants.MAX_DESCRIPTION_LENGTH));
            }
        }
    }

    private void validateMainComponents(UserConfigRequest request, List<String> errors) {
        // Validar configuración de conexión
        if (request.getConnection() != null) {
            validateConnectionParameters(request.getConnection(), errors);
        }

        // Validar configuración de autenticación
        if (request.getAuthentication() != null) {
            validateAuthenticationParameters(request.getAuthentication(), errors);
        }

        // Validar configuración de encriptación
        if (request.getEncryption() != null) {
            validateEncryptionParameters(request.getEncryption(), errors);
        }

        // Validar configuración de sesión
        if (request.getSession() != null) {
            validateSessionParameters(request.getSession(), errors);
        }
    }

    private void validateIndustrialConfiguration(
            UserConfigRequest.IndustrialConfigurationRequest config,
            List<String> errors) {

        // Validar zona industrial
        if (config.getIndustrialZone() != null) {
            validateIndustrialZone(config.getIndustrialZone(), errors);
        }

        // Validar ID de equipo
        if (config.getEquipmentId() != null) {
            validateEquipmentId(config.getEquipmentId(), errors);
        }

        // Validar ID de área
        if (config.getAreaId() != null) {
            validateAreaId(config.getAreaId(), errors);
        }

        // Validar ID de proceso
        if (config.getProcessId() != null) {
            validateProcessId(config.getProcessId(), errors);
        }

        // Validar información del operador
        validateOperatorInfo(config, errors);
    }

    private void validateIndustrialZone(String zone, List<String> errors) {
        if (!ConfigConstants.VALID_INDUSTRIAL_ZONES.contains(zone)) {
            errors.add("Zona industrial no válida: " + zone);
            errors.add("Zonas válidas: " +
                    String.join(", ", ConfigConstants.VALID_INDUSTRIAL_ZONES));
        }
    }

    private void validateEquipmentId(String equipmentId, List<String> errors) {
        if (!equipmentId.matches(ConfigConstants.EQUIPMENT_ID_PATTERN)) {
            errors.add("Formato de ID de equipo no válido. Debe seguir el patrón: EQ-XXX-###");
        }
    }

    private void validateAreaId(String areaId, List<String> errors) {
        if (!areaId.matches(ConfigConstants.AREA_ID_PATTERN)) {
            errors.add("Formato de ID de área no válido. Debe seguir el patrón: AR-XXX-###");
        }
    }

    private void validateProcessId(String processId, List<String> errors) {
        if (!processId.matches(ConfigConstants.PROCESS_ID_PATTERN)) {
            errors.add("Formato de ID de proceso no válido. Debe seguir el patrón: PR-XXX-###");
        }
    }

    private void validateOperatorInfo(
            UserConfigRequest.IndustrialConfigurationRequest config,
            List<String> errors) {
        // Validar nombre del operador
        if (config.getOperatorName() != null) {
            if (!config.getOperatorName().matches(ConfigConstants.OPERATOR_NAME_PATTERN)) {
                errors.add("El nombre del operador solo puede contener letras y espacios");
            }
        }
        // Validar ID del operador
        if (config.getOperatorId() != null) {
            if (!config.getOperatorId().matches(ConfigConstants.OPERATOR_ID_PATTERN)) {
                errors.add("Formato de ID de operador no válido. Debe seguir el patrón: OP-####");
            }
        }
    }

    //Clases utilitarias de constantes de configuracion
    private static final class ConnectionConstants{
        private static final int MIN_PORT = 1;
        private static final int MAX_PORT = 65535;
        private static final Pattern URL_PATTERN = Pattern.compile(
                "^(opc.tcp|http|https)://[\\w.-]+(:\\d{1,5})?(/[\\w.-]*)*$"
        );
    }

    private static final class AuthenticationConstants{
        private static final int MIN_USERNAME_LENGTH = 3;
        private static final int MAX_USERNAME_LENGTH = 100;
        private static final int MIN_PASSWORD_LENGTH = 8;
        private static final int MAX_PASSWORD_LENGTH = 128;
        private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9._-]+$");
        private static final Set<String> VALID_CERTIFICATE_EXTENSIONS =
                Set.of(".pem", ".der", ".crt");
        private static final Set<String> VALID_KEY_EXTENSIONS =
                Set.of(".pem", ".key", ".pfx");
    }

    private static final class EncryptionConstants{
        private static final Set<String> VALID_SECURITY_POLICIES = Set.of(
                "None",
                "Basic128Rsa15",
                "Basic256",
                "Basic256Sha256",
                "Aes128_Sha256_RsaOaep",
                "Aes256_Sha256_RsaPss"
        );
        private static final Set<String> VALID_SECURITY_MODES = Set.of(
                "None",
                "Sign",
                "SignAndEncrypt"
        );
        private static final Set<String> VALID_ALGORITHMS = Set.of(
                "RSA",
                "AES",
                "SHA256",
                "SHA384",
                "SHA512"
        );
        private static final Set<Integer> VALID_KEY_LENGTHS = Set.of(
                1024, 2048, 3072, 4096  // Para RSA
        );
        private static final int MIN_CERTIFICATE_LENGTH = 128;
        private static final int MAX_CERTIFICATE_LENGTH = 10240;  // 10KB
        private static final int MIN_PRIVATE_KEY_LENGTH = 64;
        private static final int MAX_PRIVATE_KEY_LENGTH = 8192;   // 8KB
        private static final String PROTOCOL_VERSION_PATTERN = "^\\d+\\.\\d+\\.\\d+$";
    }

    private static final class SessionConstants {
        private static final int MIN_SESSION_NAME_LENGTH = 1;
        private static final int MAX_SESSION_NAME_LENGTH = 512;
        private static final String SESSION_NAME_PATTERN = "^[a-zA-Z0-9._\\-]+$";

        private static final Set<String> VALID_URI_SCHEMES = Set.of(
                "opc.tcp", "http", "https"
        );

        private static final Set<String> VALID_SECURITY_MODES = Set.of(
                "None", "Sign", "SignAndEncrypt"
        );

        private static final Set<String> VALID_SECURITY_POLICY_URIS = Set.of(
                "http://opcfoundation.org/UA/SecurityPolicy#None",
                "http://opcfoundation.org/UA/SecurityPolicy#Basic128Rsa15",
                "http://opcfoundation.org/UA/SecurityPolicy#Basic256",
                "http://opcfoundation.org/UA/SecurityPolicy#Basic256Sha256",
                "http://opcfoundation.org/UA/SecurityPolicy#Aes128_Sha256_RsaOaep",
                "http://opcfoundation.org/UA/SecurityPolicy#Aes256_Sha256_RsaPss"
        );

        private static final long MAX_RESPONSE_SIZE_LIMIT = 1024 * 1024 * 16; // 16MB
        private static final int MIN_CERTIFICATE_LENGTH = 128;
        private static final int MAX_CERTIFICATE_LENGTH = 10240; // 10KB
        private static final int MAX_CHUNK_COUNT_LIMIT = 1000;
        private static final long MAX_TIMEOUT = 3600000; // 1 hora en milisegundos
    }

    private static final class SubscriptionConstants {
        private static final String NODE_ID_PATTERN =
                "^(ns=[0-9]+;)?[si]=[0-9a-zA-Z_\\.\\-]+$";
        private static final double MIN_PUBLISHING_INTERVAL = 100.0;  // ms
        private static final double MAX_PUBLISHING_INTERVAL = 3600000.0;  // 1 hora
        private static final double MIN_SAMPLING_INTERVAL = 50.0;  // ms
        private static final double MAX_SAMPLING_INTERVAL = 3600000.0;  // 1 hora
        private static final long MAX_LIFETIME_COUNT = 100000;
        private static final long MAX_KEEP_ALIVE_COUNT = 10000;
        private static final long MAX_NOTIFICATIONS_PER_PUBLISH = 1000;
        private static final int MAX_PRIORITY = 255;
        private static final long MAX_QUEUE_SIZE = 10000;
        private static final Set<MonitoringMode> VALID_MONITORING_MODES =
                EnumSet.allOf(MonitoringMode.class);
        private static final Set<TimestampsToReturn> VALID_TIMESTAMPS_TO_RETURN =
                EnumSet.allOf(TimestampsToReturn.class);
    }

    private static final class ConfigConstants {
        private static final int MIN_NAME_LENGTH = 3;
        private static final int MAX_NAME_LENGTH = 50;
        private static final int MAX_DESCRIPTION_LENGTH = 500;
        private static final int MAX_SUBSCRIPTIONS = 100;

        private static final String NAME_PATTERN = "^[a-zA-Z0-9_-]+$";
        private static final String EQUIPMENT_ID_PATTERN = "^EQ-[A-Z]{3}-\\d{3}$";
        private static final String AREA_ID_PATTERN = "^AR-[A-Z]{3}-\\d{3}$";
        private static final String PROCESS_ID_PATTERN = "^PR-[A-Z]{3}-\\d{3}$";
        private static final String OPERATOR_NAME_PATTERN = "^[a-zA-Z\\s]+$";
        private static final String OPERATOR_ID_PATTERN = "^OP-\\d{4}$";

        private static final Set<String> VALID_INDUSTRIAL_ZONES = Set.of(
                "PRODUCCION",
                "ALMACEN",
                "EMPAQUE",
                "CONTROL_CALIDAD",
                "MANTENIMIENTO"
        );
    }
}
