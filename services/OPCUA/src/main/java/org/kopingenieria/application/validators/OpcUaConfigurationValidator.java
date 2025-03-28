package org.kopingenieria.application.validators;

import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.config.OpcUaConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.exception.exceptions.ConfigurationException;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.springframework.util.StringUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public class OpcUaConfigurationValidator {

    @LogSystemEvent(description = "Validacion de conexion opcua", event = "Validacion de conexion",level = LogLevel.DEBUG)
    public void validateConnection(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config,"La configuracion es obligatoria");
        OpcUaConfiguration.Connection conn = config.getConnection();
        if (conn == null) {
            throw new ConfigurationException("La configuración de conexión no puede ser nula");
        }
        // Validación de URL del endpoint
        if (!StringUtils.hasText(conn.getEndpointUrl())) {
            throw new ConfigurationException("La URL del endpoint es obligatoria");
        }
        if (!isValidOpcUaUrl(conn.getEndpointUrl())) {
            throw new ConfigurationException("URL del endpoint inválida. Debe comenzar con 'opc.tcp://'");
        }
        // Validación de nombres de aplicación
        if (!StringUtils.hasText(conn.getApplicationName())) {
            throw new ConfigurationException("El nombre de la aplicación es obligatorio");
        }
        if (conn.getApplicationName().length() > 100) {
            throw new ConfigurationException("El nombre de la aplicación no puede exceder 100 caracteres");
        }
        // Validación de URIs
        if (!StringUtils.hasText(conn.getApplicationUri())) {
            throw new ConfigurationException("El URI de la aplicación es obligatorio");
        }
        if (!isValidUri(conn.getApplicationUri())) {
            throw new ConfigurationException("URI de aplicación inválido: " + conn.getApplicationUri());
        }
        if (StringUtils.hasText(conn.getProductUri()) && !isValidUri(conn.getProductUri())) {
            throw new ConfigurationException("URI de producto inválido: " + conn.getProductUri());
        }
        //Validacion de ConectionType
        if (!StringUtils.hasText(String.valueOf(conn.getType()))) {
            throw new ConfigurationException("El URI de la aplicación es obligatorio");
        }
        if (!isValidConnectionType(conn.getType())) {
            throw new ConfigurationException("URI de aplicación inválido: " + conn.getApplicationUri());
        }
        // Validación de timeouts
        if (!StringUtils.hasText(String.valueOf(conn.getTimeout()))) {
            throw new ConfigurationException("El tiempo de espera es obligatorio");
        }
        if (!isValidConnectionTimeout(conn.getTimeout())) {
            throw new ConfigurationException("El tiempo de espera debe estar entre 100ms y 30000ms");
        }
    }

    @LogSystemEvent(description = "Validacion de autenticacion opcua", event = "Validacion de autenticacion",level = LogLevel.DEBUG)
    public void validateAuthentication(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config,"La configuracion es obligatoria");
        OpcUaConfiguration.Authentication auth = config.getAuthentication();
        if (auth == null) {
            throw new ConfigurationException("La configuración de autenticación no puede ser nula");
        }
        // Validación del modo de autenticación
        if (auth.getIdentityProvider() == null) {
            throw new ConfigurationException("Debe especificar un identity provider");
        }
        // Validación de credenciales
            if (!StringUtils.hasText(auth.getUserName())) {
                throw new ConfigurationException("El nombre de usuario es obligatorio para autenticación no anónima");
            }
            if (auth.getUserName().length() > 50) {
                throw new ConfigurationException("El nombre de usuario no puede exceder 50 caracteres");
            }
            if (!StringUtils.hasText(auth.getPassword())) {
                throw new ConfigurationException("La contraseña es obligatoria para autenticación no anónima");
            }
            if (auth.getPassword().length() < 8) {
                throw new ConfigurationException("La contraseña debe tener al menos 8 caracteres");
            }
        // Validación de políticas de seguridad
        if (!StringUtils.hasText(String.valueOf(auth.getSecurityPolicy()))) {
            throw new ConfigurationException("La política de seguridad es obligatoria");
        }
        if (!isValidSecurityPolicy(String.valueOf(auth.getSecurityPolicy()))) {
            throw new ConfigurationException("Política de seguridad no válida: " + auth.getSecurityPolicy());
        }
        if (!StringUtils.hasText(String.valueOf(auth.getMessageSecurityMode()))) {
            throw new ConfigurationException("El modo de seguridad es obligatorio");
        }
        if (!isValidSecurityMode(auth.getMessageSecurityMode())) {
            throw new ConfigurationException("Modo de seguridad no válido: " + auth.getMessageSecurityMode());
        }
        // Validación de certificados
        if (StringUtils.hasText(auth.getCertificatePath())) {
            validateCertificatePath(auth.getCertificatePath());
        }
        if (StringUtils.hasText(auth.getPrivateKeyPath())) {
            validatePrivateKeyPath(auth.getPrivateKeyPath());
        }
    }

    @LogSystemEvent(description = "Validacion de encriptacion opcua", event = "Validacion de encriptacion",level = LogLevel.DEBUG)
    public void validateEncryption(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config,"La configuracion es obligatoria");
        OpcUaConfiguration.Encryption enc = config.getEncryption();
        if (enc == null) {
            throw new ConfigurationException("La configuración de encriptación no puede ser nula");
        }
        // Validación de política de seguridad
        if (!StringUtils.hasText(enc.getSecurityPolicy())) {
            throw new ConfigurationException("La política de seguridad de encriptación es obligatoria");
        }
        if (!isValidEncryptionPolicy(enc.getSecurityPolicy())) {
            throw new ConfigurationException("Política de encriptación no válida: " + enc.getSecurityPolicy());
        }
        // Validación del modo de mensaje
        if (!StringUtils.hasText(enc.getMessageSecurityMode())) {
            throw new ConfigurationException("El modo de mensaje es obligatorio");
        }
        if (!isValidMessageMode(enc.getMessageSecurityMode())) {
            throw new ConfigurationException("Modo de mensaje no válido: " + enc.getMessageSecurityMode());
        }
        // Validación del algoritmo
        if (!StringUtils.hasText(enc.getAlgorithmName())) {
            throw new ConfigurationException("El algoritmo de encriptación es obligatorio");
        }
        if (!isValidEncryptionAlgorithm(enc.getAlgorithmName())) {
            throw new ConfigurationException("Algoritmo de encriptación no válido: " + Arrays.toString(enc.getClientCertificate()));
        }
        // Validación del tamaño de llave
        if (!StringUtils.hasText(String.valueOf(enc.getKeyLength()))) {
            throw new ConfigurationException("El algoritmo de encriptación es obligatorio");
        }
        if (!isValidKeySize(enc.getKeyLength(),enc.getAlgorithmName())) {
            throw new ConfigurationException("Tamaño de llave no válido para el algoritmo: " + enc.getAlgorithmName());
        }
        // Validación del tipo de certificado
        if (!StringUtils.hasText(String.valueOf(enc.getType()))) {
            throw new ConfigurationException("El tipo de certificado es obligatorio");
        }
        if (!isValidCertificateType(enc.getType())) {
            throw new ConfigurationException("Tipo de certificado no válido: " + enc.getType());
        }
    }

    @LogSystemEvent(description = "Validacion de session opcua", event = "Validacion de session",level = LogLevel.DEBUG)
    public void validateSession(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config,"La configuracion es obligatoria");
        OpcUaConfiguration.Session session = config.getSession();
        if (session == null) {
            throw new ConfigurationException("La configuración de sesión no puede ser nula");
        }
        // Validación del nombre de sesión
        if (!StringUtils.hasText(session.getSessionName())) {
            throw new ConfigurationException("El nombre de sesión es obligatorio");
        }
        if (session.getSessionName().length() > 100) {
            throw new ConfigurationException("El nombre de sesión no puede exceder 100 caracteres");
        }
        //Validacion de server uri
        if (!StringUtils.hasText(session.getServerUri())) {
            throw new ConfigurationException("El URI del servidor es obligatorio");
        }
        if (isValidUri(session.getServerUri())) {
            throw new ConfigurationException("La uri del servidor es invalida: " + session.getServerUri());
        }
        //Validacion de maxresponsemessagesize
        if (session.getMaxResponseMessageSize() != null) {
            if (session.getMaxResponseMessageSize() < 8192 || session.getMaxResponseMessageSize() > 16777216) {
                throw new ConfigurationException("El tamaño máximo de mensaje de respuesta debe estar entre 8KB y 16MB");
            }
        }
        //Validacion de securityMode
        if (!StringUtils.hasText(String.valueOf(session.getSecurityMode()))) {
            throw new ConfigurationException("El modo de seguridad es obligatorio");
        }
        if (!isValidSecurityMode(MessageSecurityMode.valueOf(session.getSecurityMode()))) {
            throw new ConfigurationException("Modo de seguridad invalido: " + session.getSecurityMode());
        }
        //Validacion de securityPolicyUri
        if (!StringUtils.hasText(session.getSecurityPolicyUri())) {
            throw new ConfigurationException("La uri de politica de seguridad es obligatoria");
        }
        if (!isValidSecurityPolicy(session.getSecurityPolicyUri())) {
            throw new ConfigurationException("La uri de politica de seguridad es invalida: " + session.getSecurityPolicyUri());
        }
        //Validacion de locales ids
        if (!StringUtils.hasText(String.valueOf(session.getLocaleIds()))){
            throw new ConfigurationException("Los identificadores de idioma son obligatorios");
        }
        if (!validateLocaleIds(session.getLocaleIds())){
            throw new ConfigurationException("Los identificadores de idioma son invalidos: " + session.getLocaleIds());
        }
        // Validación de timeout
        if (!StringUtils.hasText(String.valueOf(session.getTimeout()))){
            throw new ConfigurationException("El tiempo de espera es obligatorio");
        }
        if (isValidSessionTimeout(session.getTimeout())){
            throw new ConfigurationException("Los identificadores de idioma son obligatorios");
        }
    }

    @LogSystemEvent(description = "Validacion de suscripcion opcua", event = "Validacion de suscripcion",level = LogLevel.DEBUG)
    public void validateSubscription(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config,"La configuracion es obligatoria");
        List<OpcUaConfiguration.Subscription> subscriptions = config.getSubscriptions();
        if (subscriptions != null && !subscriptions.isEmpty()) {
            for (OpcUaConfiguration.Subscription sub : subscriptions) {
                // Validación del nodeid
                if (!StringUtils.hasText(sub.getNodeId())) {
                    throw new ConfigurationException("El nombre de la suscripción es obligatorio");
                }
                if (!isValidNodeId(sub.getNodeId())) {
                    throw new ConfigurationException("El id de nodo tiene un formato incorrecto: " + sub.getNodeId());
                }
                // Validación de intervalos
                if (!StringUtils.hasText(String.valueOf(sub.getPublishingInterval()))) {
                    throw new ConfigurationException("El intervalo de suscripcion es obligatorio");
                }
                if (!isValidPublishingInterval(sub.getPublishingInterval())) {
                    throw new ConfigurationException("El intervalo de suscripcion debe estar entre 1000 y 10000 milisegundos");
                }
                if (!StringUtils.hasText(String.valueOf(sub.getSamplingInterval()))) {
                    throw new ConfigurationException("El intervalo de suscripcion es obligatorio");
                }
                if (!isValidSamplingInterval(sub.getSamplingInterval())){
                    throw new ConfigurationException("El intervalo de suscripcion debe estar entre 1000 y 10000 milisegundos");
                }
                //Validacion del modo de monitoreo
                if (!StringUtils.hasText(String.valueOf(sub.getMonitoringMode()))) {
                    throw new ConfigurationException("El modo de monitoreo es obligatorio");
                }
                if (!isValidMonitoringMode(sub.getMonitoringMode())){
                    throw new ConfigurationException("El modo de monitoreo debe ser uno de: " + Arrays.toString(MonitoringMode.values()));
                }
                //Validacion del timestamp a retornar
                if (!StringUtils.hasText(String.valueOf(sub.getTimestampsToReturn()))) {
                    throw new ConfigurationException("El modo de monitoreo es obligatorio");
                }
                if (!isValidTimeStampToReturn(sub.getTimestampsToReturn())){
                    throw new ConfigurationException("El modo de monitoreo debe ser uno de: " + Arrays.toString(MonitoringMode.values()));
                }
            }
        }
    }

    @LogSystemEvent(description = "Validacion de configuracion industrial opcua", event = "Validacion de configuracion industrial",level = LogLevel.DEBUG)
    public void validateIndustrialConfiguration(OpcUaConfiguration config) throws ConfigurationException {
        Objects.requireNonNull(config,"La configuracion es obligatoria");
        OpcUaConfiguration.IndustrialConfiguration ind = config.getIndustrialConfiguration();
        if (ind == null) {
            throw new ConfigurationException("La configuración industrial no puede ser nula");
        }
        // Validación de zona industrial
        if (!StringUtils.hasText(ind.getIndustrialZone())) {
            throw new ConfigurationException("La zona industrial es obligatoria");
        }
        if (!isValidIndustrialZone(ind.getIndustrialZone())) {
            throw new ConfigurationException("Zona industrial no válida: " + ind.getIndustrialZone());
        }
        // Validación de ID de equipo
        if (!StringUtils.hasText(ind.getEquipmentId())) {
            throw new ConfigurationException("El ID del equipo es obligatorio");
        }
        if (!isValidEquipmentId(ind.getEquipmentId())) {
            throw new ConfigurationException("ID de equipo no válido: " + ind.getEquipmentId());
        }
        // Validación de ID de área
        if (!StringUtils.hasText(ind.getAreaId())) {
            throw new ConfigurationException("El ID del área es obligatorio");
        }
        if (!isValidAreaId(ind.getAreaId())) {
            throw new ConfigurationException("ID de área no válido: " + ind.getAreaId());
        }
        // Validación de ID de proceso
        if (!StringUtils.hasText(ind.getProcessId())) {
            throw new ConfigurationException("El ID del proceso es obligatorio");
        }
        if (!isValidProcessId(ind.getProcessId())) {
            throw new ConfigurationException("ID de proceso no válido: " + ind.getProcessId());
        }
        // Validación de OperatorName
        if (!StringUtils.hasText(ind.getOperatorName())) {
            throw new ConfigurationException("El nombre del operador es obligatorio");
        }
        if (!isValidOperatorName(ind.getOperatorName())) {
            throw new ConfigurationException("Nombre de operador no válido: " + ind.getProcessId());
        }
        //Validacion de ID de operador
        if (!StringUtils.hasText(ind.getOperatorId())) {
            throw new ConfigurationException("El ID del operador es obligatorio");
        }
        if (!isValidOperatorId(ind.getOperatorId())) {
            throw new ConfigurationException("ID de operador no válido: " + ind.getProcessId());
        }
    }

    // Métodos auxiliares de validación
    private boolean isValidConnectionType(ConnectionType type) {
        Set<ConnectionType> validTypes = Set.of(ConnectionType.OPCUA);
        return validTypes.contains(type);
    }

    private boolean isValidConnectionTimeout(Timeouts timeout) {
        return timeout != null && timeout.toMilliseconds() >= Timeouts.CONNECTION.toMilliseconds() && timeout.toMilliseconds() <= Timeouts.CONNECTION.toMilliseconds();
    }

    private boolean isValidSessionTimeout(Timeouts timeout) {
        return timeout != null && timeout.toMilliseconds() >= Timeouts.SESSION.toMilliseconds() && timeout.toMilliseconds() <= Timeouts.SESSION.toMilliseconds();
    }

    private boolean isValidOpcUaUrl(String url) {
        if (url == null) return false;
        try {
            if (!url.toLowerCase().startsWith("opc.tcp://")) {
                return false;
            }
            URI uri = new URI(url.replace("opc.tcp", "http")); // Hack para usar URI parser
            return uri.getHost() != null && uri.getPort() != -1;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isValidUri(String uri) {
        try {
            new URI(uri);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    private boolean isValidSecurityPolicy(String policy) {
        Set<String> validPolicies = Set.of(
                "None",
                "Basic128Rsa15",
                "Basic256",
                "Basic256Sha256",
                "Aes128_Sha256_RsaOaep",
                "Aes256_Sha256_RsaPss"
        );
        return validPolicies.contains(policy);
    }

    private boolean isValidSecurityMode(MessageSecurityMode mode) {
        Set<MessageSecurityMode> validModes = Set.of(MessageSecurityMode.NONE,
                MessageSecurityMode.SIGN,
                MessageSecurityMode.INVALID,
                MessageSecurityMode.SIGNANDENCRYPT);
        return validModes.contains(mode);
    }

    private void validateCertificatePath(String path) throws ConfigurationException {
        File certFile = new File(path);
        if (!certFile.exists()) {
            throw new ConfigurationException("El archivo de certificado no existe: " + path);
        }
        if (!certFile.isFile()) {
            throw new ConfigurationException("La ruta del certificado no es un archivo: " + path);
        }
        if (!path.toLowerCase().endsWith(".der") && !path.toLowerCase().endsWith(".pem")) {
            throw new ConfigurationException("El certificado debe tener extensión .der o .pem");
        }
    }

    private void validatePrivateKeyPath(String path) throws ConfigurationException {
        File keyFile = new File(path);
        if (!keyFile.exists()) {
            throw new ConfigurationException("El archivo de llave privada no existe: " + path);
        }
        if (!keyFile.isFile()) {
            throw new ConfigurationException("La ruta de la llave privada no es un archivo: " + path);
        }
        if (!path.toLowerCase().endsWith(".pem")) {
            throw new ConfigurationException("La llave privada debe tener extensión .pem");
        }
    }

    private boolean isValidEncryptionPolicy(String policy) {
        Set<String> validPolicies = Set.of(
                "None",
                "Basic128Rsa15",
                "Basic256",
                "Basic256Sha256",
                "Aes128_Sha256_RsaOaep",
                "Aes256_Sha256_RsaPss"
        );
        return validPolicies.contains(policy);
    }

    private boolean isValidMessageMode(String mode) {
        Set<String> validModes = Set.of(
                "None",
                "Sign",
                "SignAndEncrypt"
        );
        return validModes.contains(mode);
    }

    private boolean isValidEncryptionAlgorithm(String algorithm) {
        Set<String> validAlgorithms = Set.of(
                "RSA",
                "AES",
                "SHA256",
                "SHA384",
                "SHA512"
        );
        return validAlgorithms.contains(algorithm);
    }

    private boolean isValidKeySize(Integer keySize, String algorithm) {
        if (algorithm == null || keySize == null) return false;

        return switch (algorithm.toUpperCase()) {
            case "RSA" -> keySize >= 2048 && keySize <= 4096 && keySize % 1024 == 0;
            case "AES" -> keySize == 128 || keySize == 192 || keySize == 256;
            default -> false;
        };
    }

    private boolean isValidCertificateType(String type) {
        Set<String> validTypes = Set.of(
                "X509",
                "DER",
                "PEM"
        );
        return validTypes.contains(type);
    }

    private boolean isValidNodeId(String nodeId) {
        if (nodeId == null) return false;

        // Formato básico: ns=X;i=Y o ns=X;s=Y
        Pattern pattern = Pattern.compile("^(ns=\\d+;[is]=.+)$");
        return pattern.matcher(nodeId).matches();
    }

    private boolean isValidBrowsePath(String browsePath) {
        if (browsePath == null) return false;

        // Formato: /Folder1/Folder2/NodeName
        Pattern pattern = Pattern.compile("^(/[\\w-]+)+$");
        return pattern.matcher(browsePath).matches();
    }

    private boolean isValidDataType(String dataType) {
        Set<String> validTypes = Set.of(
                "Boolean",
                "SByte",
                "Byte",
                "Int16",
                "UInt16",
                "Int32",
                "UInt32",
                "Int64",
                "UInt64",
                "Float",
                "Double",
                "String",
                "DateTime",
                "Guid",
                "ByteString",
                "XmlElement",
                "NodeId",
                "ExpandedNodeId",
                "StatusCode",
                "QualifiedName",
                "LocalizedText",
                "ExtensionObject",
                "DataValue",
                "Variant",
                "DiagnosticInfo"
        );
        return validTypes.contains(dataType);
    }

    private boolean isValidTriggerType(String triggerType) {
        Set<String> validTypes = Set.of(
                "Status",
                "StatusValue",
                "StatusValueTimestamp"
        );
        return validTypes.contains(triggerType);
    }

    private boolean isValidIndustrialZone(String zone) {
        if (zone == null) return false;

        // Formato: ZONE_XX donde XX son números
        Pattern pattern = Pattern.compile("^ZONE_\\d{2}$");
        return pattern.matcher(zone).matches();
    }

    private boolean isValidEquipmentId(String equipmentId) {
        if (equipmentId == null) return false;

        // Formato: EQ_XXXX donde X son alfanuméricos
        Pattern pattern = Pattern.compile("^EQ_[A-Z0-9]{4}$");
        return pattern.matcher(equipmentId).matches();
    }

    private boolean isValidAreaId(String areaId) {
        if (areaId == null) return false;

        // Formato: AREA_XXX donde X son alfanuméricos
        Pattern pattern = Pattern.compile("^AREA_[A-Z0-9]{3}$");
        return pattern.matcher(areaId).matches();
    }

    private boolean isValidProcessId(String processId) {
        if (processId == null) return false;

        // Formato: PROC_XXXXX donde X son alfanuméricos
        Pattern pattern = Pattern.compile("^PROC_[A-Z0-9]{5}$");
        return pattern.matcher(processId).matches();
    }

    private boolean isValidOperatorName(String operatorName) {
        if (operatorName == null) return false;
        return operatorName.matches("^[A-Za-záéíóúÁÉÍÓÚñÑ\\s]{1,50}$");
    }

    private boolean isValidOperatorId(String operatorId) {
        if (operatorId == null) return false;
        return operatorId.matches("^OP-\\d{5}$");
    }

    private boolean validateLocaleIds(List<String> localeIds) {
        if (localeIds == null || localeIds.isEmpty()) {
            return false;
        }
        for (String locale : localeIds) {
            if (!isValidLocale(locale)) {
                return false;
            }
        }
        return true;
    }

    private boolean isValidLocale(String locale) {
        if (locale == null || locale.isBlank()) return false;
        Pattern localePattern = Pattern.compile("^[a-z]{2}(-[A-Z]{2})?$");
        return localePattern.matcher(locale).matches();
    }

    private boolean isValidPublishingInterval(Double interval) {
        return interval != null && interval > 0 && interval <= 10000;
    }
    private boolean isValidSamplingInterval(Double interval) {
        return interval != null && interval > 0 && interval <= 10000;
    }
    private boolean isValidMonitoringMode(MonitoringMode mode) {
        return (mode == MonitoringMode.Disabled || mode == MonitoringMode.Sampling || mode == MonitoringMode.Reporting);
    }
    private boolean isValidTimeStampToReturn(TimestampsToReturn timeStampsToReturn) {
        return (timeStampsToReturn == TimestampsToReturn.Both || timeStampsToReturn == TimestampsToReturn.Server || timeStampsToReturn == TimestampsToReturn.Source);
    }
}
