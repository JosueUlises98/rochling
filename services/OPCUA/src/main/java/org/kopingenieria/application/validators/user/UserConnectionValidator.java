package org.kopingenieria.application.validators.user;

import io.micrometer.common.util.StringUtils;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.application.validators.contracts.ConnectionValidator;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import java.net.InetAddress;
import java.net.URI;
import java.util.function.Supplier;

public class UserConnectionValidator implements ConnectionValidator {

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion activa",description = "Validacion de sesion activa opcua")
    @LogSystemEvent(description = "Validacion de sesion activa opcua", event = "Validacion de sesion activa",level = LogLevel.DEBUG)
    public boolean validateActiveSession(UaClient client) {
        try {
            if (client == null) {
                return false;
            }
            return !client.getSession().isDone();
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion",description = "Validacion de sesion opcua")
    @LogSystemEvent(description = "Validacion de sesion valida opcua", event = "Validacion de sesion valida",level = LogLevel.DEBUG)
    public boolean validateValidSession(UaClient client) {
        if (StringUtils.isBlank(client.toString())) {
            return false;
        }
        try {
            return client.getSession() != null && !client.getSession().isDone();
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de host",description = "Validacion de host opcua")
    @LogSystemEvent(description = "Validacion de host opcua", event = "Validacion de host",level = LogLevel.DEBUG)
    public boolean validateHost(String host) {
        if (StringUtils.isBlank(host)) {
            return false;
        }
        try {
            URI uri = new URI(host);
            InetAddress name = InetAddress.getByName(uri.getHost());// Intentamos resolver el nombre
            if (name != null) {
                if (name.getHostAddress().equalsIgnoreCase(UrlType.OPCUA_REMOTE.getIpAddress())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de puerto",description = "Validacion de puerto opcua")
    @LogSystemEvent(description = "Validacion de puerto opcua", event = "Validacion de puerto",level = LogLevel.DEBUG)
    public boolean validatePort(int port) {
        if (StringUtils.isBlank(String.valueOf(port))) {
            return false;
        }
        if (port < 0 || port > 65535) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de endpoint",description = "Validacion de endpoint opcua")
    @LogSystemEvent(description = "Validacion de endpoint opcua", event = "Validacion de endpoint",level = LogLevel.DEBUG)
    public boolean validateEndpoint(UrlType endpoint) {
        if (StringUtils.isBlank(endpoint.getUrl())) {
            return false;
        }
        switch (endpoint){
            case OPCUA_LOCAL, OPCUA_REMOTE, OPCUA_SECURE -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de timeout",description = "Validacion de timeout de conexion opcua")
    @LogSystemEvent(description = "Validacion de timeout de conexion opcua", event = "Validacion de timeout de conexion",level = LogLevel.DEBUG)
    public boolean validateTimeout(int timeout) {
        if (StringUtils.isBlank(String.valueOf(timeout))) {
            return false;
        }
        if (timeout <= 0) {
            return false;
        }
        if (timeout > Timeouts.CONNECTION.toMilliseconds()) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de politica de seguridad ",description = "Validacion de politica de seguridad opcua")
    @LogSystemEvent(description = "Validacion de politica de seguridad opcua", event = "Validacion de politica de seguridad",level = LogLevel.DEBUG)
    public boolean validateSecurityPolicy(String securityPolicy) {
        if (StringUtils.isBlank(securityPolicy)) {
            return false;
        }
        switch (securityPolicy){
            case "None", "Basic128Rsa15", "Basic256", "Basic256Sha256", "Aes128Sha256RsaOaep", "Aes256Sha256RsaPss" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de modo de seguridad",description = "Validacion del modo de seguridad opcua")
    @LogSystemEvent(description = "Validacion de modo de seguridad opcua", event = "Validacion de modo de seguridad",level = LogLevel.DEBUG)
    public boolean validateSecurityMode(String securityMode) {
        if (StringUtils.isBlank(securityMode)) {
            return false;
        }
        switch (securityMode){
            case "None", "Sign", "SignAndEncrypt" -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de certificado",description = "Validacion de certificado X509 opcua")
    @LogSystemEvent(description = "Validacion de certificado X509 opcua", event = "Validacion de certificado X509",level = LogLevel.DEBUG)
    public boolean validateCertificate(String certificate) {
        if (StringUtils.isBlank(certificate)) {
            return false;
        }
        try {
            // Placeholder logic for certificate validation (could involve checking X.509 compliance)
            boolean isValidCertificate = certificate.startsWith("-----BEGIN CERTIFICATE-----") &&
                                          certificate.endsWith("-----END CERTIFICATE-----");
            if (!isValidCertificate) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de la clave privada",description = "Validacion de clave privada opcua")
    @LogSystemEvent(description = "Validacion de clave privada opcua", event = "Validacion de clave privada",level = LogLevel.DEBUG)
    public boolean validatePrivateKey(String privateKey) {
        if (StringUtils.isBlank(privateKey)) {
            return false;
        }
        try {
            // Check key formatting based on typical PEM structure
            boolean isValidPrivateKey = privateKey.startsWith("-----BEGIN PRIVATE KEY-----") &&
                                        privateKey.endsWith("-----END PRIVATE KEY-----");
            if (!isValidPrivateKey) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de cadena certificadora",description = "Validacion de cadena certificadora opcua")
    @LogSystemEvent(description = "Validacion de cadena de certificacion opcua", event = "Validacion de cadena de certificado",level = LogLevel.DEBUG)
    public boolean validateCertificateChain(String certificateChain) {
        if (StringUtils.isBlank(certificateChain)) {
            return false;
        }
        try {
            // Split the certificate chain into individual certificates
            String[] certificates = certificateChain.split("-----END CERTIFICATE-----");
            for (String cert : certificates) {
                cert = cert.trim();
                if (!cert.isEmpty()) {
                    cert += "-----END CERTIFICATE-----"; // Restore the delimiter
                    if (!cert.startsWith("-----BEGIN CERTIFICATE-----") || !cert.endsWith("-----END CERTIFICATE-----")) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del alias del certificado",description = "Validacion del alias del certificado opcua")
    @LogSystemEvent(description = "Validacion del alias del certificado opcua", event = "Validacion del alias del certificado",level = LogLevel.DEBUG)
    public boolean validateCertificateAlias(String certificateAlias) {
        if (StringUtils.isBlank(certificateAlias)) {
            return false;
        }
        // Validate alias with professional formatting and allowed constraints
        try {
            if (certificateAlias.length() > 50) {
                return false;
            }
            if (!certificateAlias.matches("^[a-zA-Z0-9_-]+$")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de certificacion",description = "Validacion del archivo de certificacion opcua")
    @LogSystemEvent(description = "Validacion del archivo de certificacion opcua", event = "Validacion del archivo de certificacion",level = LogLevel.DEBUG)
    public boolean validateCertificateFile(String certificateFile) {
        if (StringUtils.isBlank(certificateFile)) {
            return false;
        }
        try {
            java.io.File file = new java.io.File(certificateFile);
            if (!file.exists()) {
                return false;
            }
            if (!file.canRead()) {
                return false;
            }
            // Read file content and validate certificate structure
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            boolean isValidCertificate = fileContent.startsWith("-----BEGIN CERTIFICATE-----") &&
                                          fileContent.endsWith("-----END CERTIFICATE-----");
            if (!isValidCertificate) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de clave privada",description = "Validacion del archivo de clave privada opcua")
    @LogSystemEvent(description = "Validacion del archivo de la clave privada opcua", event = "Validacion del archivo de la clave privada",level = LogLevel.DEBUG)
    public boolean validatePrivateKeyFile(String privateKeyFile) {
        if (StringUtils.isBlank(privateKeyFile)) {
            return false;
        }
        try {
            java.io.File file = new java.io.File(privateKeyFile);
            if (!file.exists()) {
                return false;
            }
            if (!file.canRead()) {
                return false;
            }
            // Read file content and validate private key structure
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            boolean isValidPrivateKey = fileContent.startsWith("-----BEGIN PRIVATE KEY-----") &&
                                        fileContent.endsWith("-----END PRIVATE KEY-----");
            if (!isValidPrivateKey) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de la cadena certificadora",description = "Validacion del archivo de la cadena certificadora opcua")
    @LogSystemEvent(description = "Validacion del archivo de la cadena certificadora opcua", event = "Validacion del archivo de la cadena certificadora",level = LogLevel.DEBUG)
    public boolean validateCertificateChainFile(String certificateChainFile) {
        if (StringUtils.isBlank(certificateChainFile)) {
            return false;
        }
        try {
            java.io.File file = new java.io.File(certificateChainFile);
            if (!file.exists()) {
                return false;
            }
            if (!file.canRead()) {
                return false;
            }
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            String[] certificates = fileContent.split("-----END CERTIFICATE-----");
            for (String cert : certificates) {
                cert = cert.trim();
                if (!cert.isEmpty()) {
                    cert += "-----END CERTIFICATE-----";
                    if (!cert.startsWith("-----BEGIN CERTIFICATE-----") || !cert.endsWith("-----END CERTIFICATE-----")) {
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del alias del archivo de certificacion",description = "Validacion del alias del archivo de certificacion opcua")
    @LogSystemEvent(description = "Validacion del alias del archivo del certificado opcua", event = "Validacion del alias del archivo del certificado",level = LogLevel.DEBUG)
    public boolean validateCertificateAliasFile(String certificateAliasFile) {
        if (StringUtils.isBlank(certificateAliasFile)) {
            return false;
        }
        try {
            java.io.File file = new java.io.File(certificateAliasFile);
            if (!file.exists()) {
                return false;
            }
            if (!file.canRead()) {
                return false;
            }
            // Read the alias from the file
            String alias = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
            if (alias.length() > 50) {
                return false;
            }
            if (!alias.matches("^[a-zA-Z0-9_-]+$")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de password del certificado",description = "Validacion del archivo de password del certificado opcua")
    @LogSystemEvent(description = "Validacion del password del certificado", event = "Validacion del password del certificado",level = LogLevel.DEBUG)
    public boolean validateCertificateFilePassword(String certificateFilePassword) {
        if (StringUtils.isBlank(certificateFilePassword)) {
            return false;
        }
        try {
            if (certificateFilePassword.length() < 8) {
                return false;
            }
            if (!certificateFilePassword.matches(".*[A-Z].*")) {
                return false;
            }
            if (!certificateFilePassword.matches(".*[a-z].*")) {
                return false;
            }
            if (!certificateFilePassword.matches(".*\\d.*")) {
                return false;
            }
            if (!certificateFilePassword.matches(".*[@#$%^&+=].*")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del password del archivo de la clave privada",description = "Validacion del password del archivo de la clave privada opcua")
    @LogSystemEvent(description = "Validacion del archivo de password de la clave privada opcua ", event = "Validacion del archivo de password de la clave privada",level = LogLevel.DEBUG)
    public boolean validatePrivateKeyFilePassword(String privateKeyFilePassword) {
        if (StringUtils.isBlank(privateKeyFilePassword)) {
            return false;
        }
        try {
            if (privateKeyFilePassword.length() < 8) {
                return false;
            }
            if (!privateKeyFilePassword.matches(".*[A-Z].*")) {
                return false;
            }
            if (!privateKeyFilePassword.matches(".*[a-z].*")) {
                return false;
            }
            if (!privateKeyFilePassword.matches(".*\\d.*")) {
                return false;
            }
            if (!privateKeyFilePassword.matches(".*[@#$%^&+=].*")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del password del archivo de la cadena certificadora",description = "Validacion del password del archivo de la cadena certificadora opcua")
    @LogSystemEvent(description = "Validacion del archivo de password de la cadena certificadora opcua", event = "Validacion del archivo de password de la cadena certificadora",level = LogLevel.DEBUG)
    public boolean validateCertificateChainFilePassword(String certificateChainFilePassword) {
        if (StringUtils.isBlank(certificateChainFilePassword)) {
            return false;
        }
        try {
            if (certificateChainFilePassword.length() < 8) {
                return false;
            }
            if (!certificateChainFilePassword.matches(".*[A-Z].*")) {
                return false;
            }
            if (!certificateChainFilePassword.matches(".*[a-z].*")) {
                return false;
            }
            if (!certificateChainFilePassword.matches(".*\\d.*")) {
                return false;
            }
            if (!certificateChainFilePassword.matches(".*[@#$%^&+=].*")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del password del archivo del alias del certificado",description = "Validacion del password del archivo del alias del certificado")
    @LogSystemEvent(description = "Validacion del alias del archivo del certificado opcua", event = "Validacion del alias del archivo del certificado",level = LogLevel.DEBUG)
    public boolean validateCertificateAliasFilePassword(String certificateAliasFilePassword) {
        if (StringUtils.isBlank(certificateAliasFilePassword)) {
            return false;
        }
        try {
            if (certificateAliasFilePassword.length() < 8) {
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*[A-Z].*")) {
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*[a-z].*")) {
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*\\d.*")) {
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*[@#$%^&+=].*")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del host local",description = "Validacion del host local opcua")
    @LogSystemEvent(description = "Validacion del host local opcua", event = "Validacion del host local",level = LogLevel.DEBUG)
    public boolean validateLocalHost(String host) {
        try {
            URI uri = new URI(host);
            InetAddress name = InetAddress.getByName(uri.getHost());// Intentamos resolver el nombre
            if (name != null) {
                if (name.isLoopbackAddress()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validaciones de conexion opcua",description = "Validaciones de conexion de un cliente opcua")
    @LogSystemEvent(description = "Validacion de datos de conexion opcua ", event = "Validacion de datos de conexion",level = LogLevel.DEBUG)
    public boolean validate(Supplier<UaClient> clientSupplier) {
        try {
            UaClient client = clientSupplier.get();
            if (!validateActiveSession(client)) {
                return false;
            }
            if (!validateValidSession(client)) {
                return false;
            }
            if (!validateHost(client.getConfig().getEndpoint().getEndpointUrl().substring(7))) {
                return false;
            }
            if (!validatePort(Integer.parseInt(client.getConfig().getEndpoint().getEndpointUrl().substring(7).split(":")[1]))) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
