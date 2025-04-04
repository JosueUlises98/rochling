package org.kopingenieria.application.validators.user;

import org.eclipse.milo.opcua.stack.core.types.enumerated.TimestampsToReturn;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.domain.enums.connection.ConnectionType;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.locale.LocaleIds;
import org.kopingenieria.domain.enums.monitoring.MonitoringMode;
import org.kopingenieria.domain.enums.security.CertificateType;
import org.kopingenieria.domain.enums.security.EncryptionAlgorithm;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.springframework.util.StringUtils;
import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class UserConfigurationValidatorImpl implements UserConfigurationValidator {

    @Auditable(type = AuditEntryType.OPERATION, value = "Validacion de configuracion de conexion", description = "Validacion de configuracion de conexion opcua")
    @LogSystemEvent(description = "Validacion de conexion opcua", event = "Validacion de conexion", level = LogLevel.DEBUG)
    public boolean validateConnection(UserConfiguration connection) {
        Objects.requireNonNull(connection, "La configuracion es obligatoria");
        UserConfiguration.Connection conn = connection.getConnection();
        if (conn == null) {
            return false;
        }
        // Validación de URL del endpoint
        if (!StringUtils.hasText(conn.getEndpointUrl())) {
            return false;
        }
        if (!isValidOpcUaUrl(conn.getEndpointUrl())) {
            return false;
        }
        // Validación de nombres de aplicación
        if (!StringUtils.hasText(conn.getApplicationName())) {
            return false;
        }
        if (conn.getApplicationName().length() > 100) {
            return false;
        }
        // Validación de URIs
        if (!StringUtils.hasText(conn.getApplicationUri())) {
            return false;
        }
        if (!isValidUri(conn.getApplicationUri())) {
            return false;
        }
        if (StringUtils.hasText(conn.getProductUri()) && !isValidUri(conn.getProductUri())) {
            return false;
        }
        //Validacion de ConectionType
        if (!StringUtils.hasText(String.valueOf(conn.getType()))) {
            return false;
        }
        if (!isValidConnectionType(String.valueOf(conn.getType()))) {
            return false;
        }
        // Validación de timeouts
        if (!StringUtils.hasText(String.valueOf(conn.getTimeout()))) {
            return false;
        }
        if (!isValidConnectionTimeout(conn.getTimeout().toMilliseconds())) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION, value = "Validacion de configuracion de autenticacion", description = "Validacion de configuracion de autenticacion opcua")
    @LogSystemEvent(description = "Validacion de autenticacion opcua", event = "Validacion de autenticacion", level = LogLevel.DEBUG)
    public boolean validateAuthentication(UserConfiguration authentication) {
        Objects.requireNonNull(authentication, "La configuracion es obligatoria");
        UserConfiguration.Authentication auth = authentication.getAuthentication();
        if (auth == null) {
            return false;
        }
        // Validación del modo de autenticación
        if (auth.getIdentityProvider() == null) {
            return false;
        }
        // Validación de credenciales
        if (!StringUtils.hasText(auth.getUserName())) {
            return false;
        }
        if (auth.getUserName().length() > 50) {
            return false;
        }
        if (!StringUtils.hasText(auth.getPassword())) {
            return false;
        }
        if (auth.getPassword().length() < 8) {
            return false;
        }
        // Validación de políticas de seguridad
        if (!StringUtils.hasText(String.valueOf(auth.getSecurityPolicy()))) {
            return false;
        }
        if (!isValidSecurityPolicy(String.valueOf(auth.getSecurityPolicy()))) {
            return false;
        }
        if (!StringUtils.hasText(String.valueOf(auth.getMessageSecurityMode()))) {
            return false;
        }
        if (!isValidSecurityMode(String.valueOf(auth.getMessageSecurityMode()))) {
            return false;
        }
        // Validación de certificados
        if (StringUtils.hasText(auth.getCertificatePath())) {
            return validateCertificatePath(auth.getCertificatePath());
        }
        if (StringUtils.hasText(auth.getPrivateKeyPath())) {
            return validatePrivateKeyPath(auth.getPrivateKeyPath());
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION, value = "Validacion de configuracion de encriptacion", description = "Validacion de configuracion de encriptacion opcua")
    @LogSystemEvent(description = "Validacion de encriptacion opcua", event = "Validacion de encriptacion", level = LogLevel.DEBUG)
    public boolean validateEncryption(UserConfiguration encryption) {
        Objects.requireNonNull(encryption, "La configuracion es obligatoria");
        UserConfiguration.Encryption enc = encryption.getEncryption();
        if (enc == null) {
            return false;
        }
        // Validación de política de seguridad
        if (!StringUtils.hasText(String.valueOf(enc.getSecurityPolicy()))) {
            return false;
        }
        if (!isValidEncryptionPolicy(String.valueOf(enc.getSecurityPolicy()))) {
            return false;
        }
        // Validación del modo de mensaje
        if (!StringUtils.hasText(String.valueOf(enc.getMessageSecurityMode()))) {
            return false;
        }
        if (!isValidMessageMode(String.valueOf(enc.getMessageSecurityMode()))) {
            return false;
        }
        // Validación del algoritmo
        if (!StringUtils.hasText(String.valueOf(enc.getAlgorithmName()))) {
            return false;
        }
        if (!isValidEncryptionAlgorithm(String.valueOf(enc.getAlgorithmName()))) {
            return false;
        }
        // Validación del tamaño de llave
        if (!StringUtils.hasText(String.valueOf(enc.getKeyLength()))) {
            return false;
        }
        if (!isValidKeySize(enc.getKeyLength(), String.valueOf(enc.getAlgorithmName()))) {
            return false;
        }
        // Validación del tipo de certificado
        if (!StringUtils.hasText(String.valueOf(enc.getType()))) {
            return false;
        }
        if (!isValidCertificateType(enc.getType())) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION, value = "Validacion de configuracion de sesion", description = "Validacion de configuracion de sesion opcua")
    @LogSystemEvent(description = "Validacion de session opcua", event = "Validacion de session", level = LogLevel.DEBUG)
    public boolean validateSession(UserConfiguration sessionua) {
        Objects.requireNonNull(sessionua, "La configuracion es obligatoria");
        UserConfiguration.Session session = sessionua.getSession();
        if (session == null) {
            return false;
        }
        // Validación del nombre de sesión
        if (!StringUtils.hasText(session.getSessionName())) {
            return false;
        }
        if (session.getSessionName().length() > 100) {
            return false;
        }
        //Validacion de server uri
        if (!StringUtils.hasText(session.getServerUri())) {
            return false;
        }
        if (isValidUri(session.getServerUri())) {
            return false;
        }
        //Validacion de maxresponsemessagesize
        if (session.getMaxResponseMessageSize() != null) {
            if (session.getMaxResponseMessageSize() < 8192 || session.getMaxResponseMessageSize() > 16777216) {
                return false;
            }
        }
        //Validacion de securityMode
        if (!StringUtils.hasText(String.valueOf(session.getSecurityMode()))) {
            return false;
        }
        if (!isValidSecurityMode(String.valueOf(session.getSecurityMode()))) {
            return false;
        }
        //Validacion de securityPolicyUri
        if (!StringUtils.hasText(String.valueOf(session.getSecurityPolicyUri()))) {
            return false;
        }
        if (!isValidSecurityPolicy(String.valueOf(session.getSecurityPolicyUri()))) {
            return false;
        }
        //Validacion de locales ids
        if (!StringUtils.hasText(String.valueOf(session.getLocaleIds()))) {
            return false;
        }
        if (!validateLocaleIds(session.getLocaleIds().stream().map(String::valueOf).collect(Collectors.toList()))) {
            return false;
        }
        // Validación de timeout
        if (!StringUtils.hasText(String.valueOf(session.getTimeout().toMilliseconds()))) {
            return false;
        }
        if (!isValidSessionTimeout(session.getTimeout().toMilliseconds())) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION, value = "Validacion de configuracion industrial", description = "Validacion de configuracion industrial opcua")
    @LogSystemEvent(description = "Validacion de configuracion industrial opcua", event = "Validacion de configuracion industrial", level = LogLevel.DEBUG)
    public boolean validateIndustrialConfiguration(UserConfiguration industrialconfig) {
        Objects.requireNonNull(industrialconfig, "La configuracion es obligatoria");
        UserConfiguration.IndustrialConfiguration ind = industrialconfig.getIndustrialConfiguration();
        if (ind == null) {
            return false;
        }
        // Validación de zona industrial
        if (!StringUtils.hasText(ind.getIndustrialZone())) {
            return false;
        }
        if (!isValidIndustrialZone(ind.getIndustrialZone())) {
            return false;
        }
        // Validación de ID de equipo
        if (!StringUtils.hasText(ind.getEquipmentId())) {
            return false;
        }
        if (!isValidEquipmentId(ind.getEquipmentId())) {
            return false;
        }
        // Validación de ID de área
        if (!StringUtils.hasText(ind.getAreaId())) {
            return false;
        }
        if (!isValidAreaId(ind.getAreaId())) {
            return false;
        }
        // Validación de ID de proceso
        if (!StringUtils.hasText(ind.getProcessId())) {
            return false;
        }
        if (!isValidProcessId(ind.getProcessId())) {
            return false;
        }
        // Validación de OperatorName
        if (!StringUtils.hasText(ind.getOperatorName())) {
            return false;
        }
        if (!isValidOperatorName(ind.getOperatorName())) {
            return false;
        }
        //Validacion de ID de operador
        if (!StringUtils.hasText(ind.getOperatorId())) {
            return false;
        }
        if (!isValidOperatorId(ind.getOperatorId())) {
            return false;
        }
        return true;
    }

    public String getValidationResult(UserConfiguration config) {
        Objects.requireNonNull(config, "La configuracion es obligatoria");
        String result = "failed";
        if (validateConnection(config) & validateAuthentication(config) & validateEncryption(config) &
                validateSession(config) & validateIndustrialConfiguration(config)) {
            result = "success";
        }
        return result;
    }

    // Métodos auxiliares de validación
    private boolean isValidConnectionType(String type) {
        Set<ConnectionType> validTypes = Set.of(ConnectionType.OPCUA);
        return validTypes.contains(ConnectionType.valueOf(type));
    }

    private boolean isValidConnectionTimeout(Long timeout) {
        return timeout != null && timeout >= 0 && timeout <= Timeouts.CONNECTION.toMilliseconds();
    }

    private boolean isValidSessionTimeout(Long timeout) {
        return timeout != null && timeout > 0 && timeout <= Timeouts.SESSION.toMilliseconds();
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
        Set<SecurityPolicy> validPolicies = Set.of(SecurityPolicy.NONE, SecurityPolicy.BASIC256,
                SecurityPolicy.BASIC128RSA15, SecurityPolicy.BASIC256SHA256,
                SecurityPolicy.AES128_SHA256_RSAOAEP, SecurityPolicy.AES256_SHA256_RSAPSS);
        return validPolicies.contains(SecurityPolicy.valueOf(policy));
    }

    private boolean isValidSecurityMode(String mode) {
        Set<MessageSecurityMode> validModes = Set.of(MessageSecurityMode.NONE,
                MessageSecurityMode.SIGN,
                MessageSecurityMode.INVALID,
                MessageSecurityMode.SIGNANDENCRYPT);
        return validModes.contains(MessageSecurityMode.valueOf(mode));
    }

    private boolean validateCertificatePath(String path) {
        File certFile = new File(path);
        if (!certFile.exists()) {
            return false;
        }
        if (!certFile.isFile()) {
            return false;
        }
        if (!path.toLowerCase().endsWith(".der") && !path.toLowerCase().endsWith(".pem")) {
            return false;
        }
        return true;
    }

    private boolean validatePrivateKeyPath(String path) {
        File keyFile = new File(path);
        if (!keyFile.exists()) {
            return false;
        }
        if (!keyFile.isFile()) {
            return false;
        }
        if (!path.toLowerCase().endsWith(".pem")) {
            return false;
        }
        return true;
    }

    private boolean isValidEncryptionPolicy(String policy) {
        Set<SecurityPolicy> validPolicies = Set.of(SecurityPolicy.NONE, SecurityPolicy.BASIC256,
                SecurityPolicy.BASIC128RSA15);
        return validPolicies.contains(SecurityPolicy.valueOf(policy));
    }

    private boolean isValidMessageMode(String mode) {
        Set<MessageSecurityMode> validModes = Set.of(MessageSecurityMode.NONE, MessageSecurityMode.SIGN,
                MessageSecurityMode.SIGNANDENCRYPT);
        return validModes.contains(MessageSecurityMode.valueOf(mode));
    }

    private boolean isValidEncryptionAlgorithm(String algorithm) {
        Set<EncryptionAlgorithm> validAlgorithms = Set.of(EncryptionAlgorithm.SHA256, EncryptionAlgorithm.SHA512,
                EncryptionAlgorithm.SHA384, EncryptionAlgorithm.AES, EncryptionAlgorithm.RSA);
        return validAlgorithms.contains(EncryptionAlgorithm.valueOf(algorithm));
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
        Set<CertificateType> validTypes = Set.of(CertificateType.X509, CertificateType.DER, CertificateType.PEM);
        return validTypes.contains(CertificateType.valueOf(type));
    }

    private boolean isValidNodeId(String nodeId) {
        if (nodeId == null) return false;

        // Formato básico: ns=X;i=Y o ns=X;s=Y
        Pattern pattern = Pattern.compile("^(ns=\\d+;s=.*)$");
        return pattern.matcher(nodeId).matches();
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
        Set<LocaleIds> validLocales = Set.of(LocaleIds.values());
        return validLocales.contains(LocaleIds.valueOf(locale));
    }

    private boolean isValidPublishingInterval(Double interval) {
        return interval != null && interval > 0 && interval <= 10000;
    }

    private boolean isValidSamplingInterval(Double interval) {
        return interval != null && interval > 0 && interval <= 10000;
    }

    private boolean isValidMonitoringMode(String mode) {
        return (Objects.equals(mode, MonitoringMode.Disabled.name()) || Objects.equals(mode, MonitoringMode.Sampling.name()) || Objects.equals(mode, MonitoringMode.Reporting.name()));
    }

    private boolean isValidTimeStampToReturn(String timeStampsToReturn) {
        return (Objects.equals(timeStampsToReturn, TimestampsToReturn.Both.name()) || Objects.equals(timeStampsToReturn, TimestampsToReturn.Server.name()) || Objects.equals(timeStampsToReturn, TimestampsToReturn.Source.name()));
    }
}
