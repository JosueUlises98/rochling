package org.kopingenieria.application.validators.impl.user;

import io.micrometer.common.util.StringUtils;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.application.validators.contract.user.UserConnectionValidator;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.kopingenieria.util.loader.CertificateLoader;
import org.kopingenieria.util.loader.PrivateKeyLoader;
import org.kopingenieria.util.security.user.UserCertificateManager;
import java.io.*;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class UserConnectionValidatorImpl implements UserConnectionValidator {

    private final UserCertificateManager certificateManager= new UserCertificateManager();
    private static final String PASSWORD_FILE_EXTENSION = ".pwd";
    private static final long MAX_CERTIFICATE_SIZE = 10_485_760; // 10MB
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private static final Set<String> VALID_EXTENSIONS = Set.of(
            ".pem", ".key", ".private", ".pk8", ".pkcs8"
    );

    private static final Set<String> VALID_HEADERS = Set.of(
            "-----BEGIN PRIVATE KEY-----",
            "-----BEGIN RSA PRIVATE KEY-----",
            "-----BEGIN EC PRIVATE KEY-----",
            "-----BEGIN DSA PRIVATE KEY-----"
    );

    private static final Set<String> VALID_FOOTERS = Set.of(
            "-----END PRIVATE KEY-----",
            "-----END RSA PRIVATE KEY-----",
            "-----END EC PRIVATE KEY-----",
            "-----END DSA PRIVATE KEY-----"
    );

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
            //Si y solo si
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
    public boolean validateEndpoint(String endpoint) {
        if (StringUtils.isBlank(endpoint)) {
            return false;
        }
        switch (endpoint){
            case UrlType.OpcUa.LOCAL, UrlType.OpcUa.REMOTE, UrlType.OpcUa.SECURE -> {
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
            // Validar formato básico del certificado
            if (!validarFormatoCertificado(certificate)) {
                return false;
            }

            // Crear archivo temporal para el certificado
            File tempCertFile = crearArchivoTemporalCertificado(certificate);

            try {
                // Cargar el certificado usando CertificateLoader
                X509Certificate x509Certificate = CertificateLoader.loadX509Certificate(
                        tempCertFile.getAbsolutePath()
                );

                certificateManager.validarCertificado(x509Certificate);

                return true;

            } finally {
                // Asegurar la limpieza del archivo temporal
                if (tempCertFile.exists()) {
                    tempCertFile.delete();
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de la clave privada",description = "Validacion de clave privada opcua")
    @LogSystemEvent(description = "Validacion de clave privada opcua", event = "Validacion de clave privada",level = LogLevel.DEBUG)
    public boolean validatePrivateKey(String privateKeyPath) {
        // Validación de parámetros de entrada
        if (StringUtils.isBlank(privateKeyPath)) {
            return false;
        }

        File privateKeyFile = new File(privateKeyPath);

        // Validación de existencia y permisos del archivo
        try {
            validateKeyFile(privateKeyFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            // Validación del tamaño del archivo
            validatePrivateKeyFileSize(privateKeyFile);

            // Validación del formato del archivo
            validateKeyFormat(privateKeyFile);

            // Intento de carga de la clave
            PrivateKey privateKey = PrivateKeyLoader.loadPrivateKey(privateKeyPath);

            // Validación adicional de la clave cargada
            return validateLoadedKey(privateKey);

        } catch (GeneralSecurityException | IOException e) {
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
            if (certificateAlias.length() <= 0 & certificateAlias.length() >= 64) {
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
    public boolean validateCertificateFile(File certificateFile) {
        if (StringUtils.isBlank(String.valueOf(certificateFile))) {
            return false;
        }
        try {
            // Validación de parámetros de entrada
            if (certificateFile == null || StringUtils.isBlank(certificateFile.getName())) {
                return false;
            }

            // Validación de existencia y permisos
            if (!validateFileProperties(certificateFile)) {
                return false;
            }

            // Validación de tamaño
            if (!validateCertificateFileSize(certificateFile)) {
                return false;
            }

            // Validación de extensión
            if (!validateCertificateFileExtension(certificateFile)) {
                return false;
            }

            // Lectura y validación del contenido
            return validateCertificateContent(certificateFile);

        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de clave privada",description = "Validacion del archivo de clave privada opcua")
    @LogSystemEvent(description = "Validacion del archivo de la clave privada opcua", event = "Validacion del archivo de la clave privada",level = LogLevel.DEBUG)
    public boolean validatePrivateKeyFile(File privateKeyFile) {
        if (StringUtils.isBlank(String.valueOf(privateKeyFile))) {
            return false;
        }
        try {
            // Validación de parámetros de entrada
            if (privateKeyFile == null || StringUtils.isBlank(privateKeyFile.getName())) {
                return false;
            }

            // Validación de existencia y permisos
            if (!validateFileProperties(privateKeyFile)) {
                return false;
            }

            // Validación de tamaño
            if (!validatePrivateKeyFileSize(privateKeyFile)) {
                return false;
            }

            // Validación de extensión
            if (!validatePrivateKeyFileExtension(privateKeyFile)) {
                return false;
            }

            // Lectura y validación del contenido
            return validatePrivateKeyContent(privateKeyFile);

        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de la cadena certificadora",description = "Validacion del archivo de la cadena certificadora opcua")
    @LogSystemEvent(description = "Validacion del archivo de la cadena certificadora opcua", event = "Validacion del archivo de la cadena certificadora",level = LogLevel.DEBUG)
    public boolean validateCertificateChainFile(File certificateChainFile) {
        if (StringUtils.isBlank(String.valueOf(certificateChainFile))) {
            return false;
        }
        try {
            if (!certificateChainFile.exists()) {
                return false;
            }
            if (!certificateChainFile.canRead()) {
                return false;
            }
            String fileContent = new String(java.nio.file.Files.readAllBytes(certificateChainFile.toPath()));
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
            if (!certificateAliasFile.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=]).{8,}$")) {
                return false;
            }
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del archivo de password del certificado",description = "Validacion del archivo de password del certificado opcua")
    @LogSystemEvent(description = "Validacion del password del certificado", event = "Validacion del password del certificado",level = LogLevel.DEBUG)
    public boolean validateCertificateFilePassword(File certificateFilePassword) {
        if (StringUtils.isBlank(String.valueOf(certificateFilePassword))) {
            return false;
        }
        try {
            // Validación básica del path
            if (!isValidPasswordFileExtensions(certificateFilePassword.getName())) {
                return false;
            }
            // Validaciones del archivo
            if (!validatePasswordFileProperties(certificateFilePassword)) {
                return false;
            }

            // Validación del contenido del archivo
            return validatePasswordFileContent(certificateFilePassword);

        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del password del archivo de la clave privada",description = "Validacion del password del archivo de la clave privada opcua")
    @LogSystemEvent(description = "Validacion del archivo de password de la clave privada opcua ", event = "Validacion del archivo de password de la clave privada",level = LogLevel.DEBUG)
    public boolean validatePrivateKeyFilePassword(File privateKeyFilePassword) {
        if (StringUtils.isBlank(String.valueOf(privateKeyFilePassword))) {
            return false;
        }
        try {
            // Validación básica del path
            if (!isValidPasswordFileExtensions(privateKeyFilePassword.getName())) {
                return false;
            }

            // Validaciones del archivo
            if (!validatePasswordFileProperties(privateKeyFilePassword)) {
                return false;
            }

            // Validación del contenido del archivo
            return validatePasswordFileContent(privateKeyFilePassword);

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

    private boolean validarFormatoCertificado(String certificate) {
        return certificate.startsWith("-----BEGIN CERTIFICATE-----") &&
                certificate.endsWith("-----END CERTIFICATE-----") &&
                certificate.contains("\n");
    }

    private File crearArchivoTemporalCertificado(String certificate) throws IOException {
        File tempFile = File.createTempFile("temp_cert_", ".pem");
        tempFile.deleteOnExit();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(certificate);
        }

        return tempFile;
    }

    private static void validateKeyFile(File privateKeyFile) throws IOException {
        if (!privateKeyFile.exists()) {
            throw new NoSuchFileException("El archivo de clave privada no existe: " + privateKeyFile.getPath());
        }
        if (!privateKeyFile.isFile()) {
            throw new IOException("La ruta especificada no es un archivo válido: " + privateKeyFile.getPath());
        }
        if (!privateKeyFile.canRead()) {
            throw new IOException("No hay permisos de lectura para el archivo: " + privateKeyFile.getPath());
        }
    }

    private static boolean validatePrivateKeyFileSize(File privateKeyFile) throws IOException {
        long fileSize = privateKeyFile.length();
        // Tamaños típicos de claves privadas (ajustar según necesidades)
        return (fileSize < 100 || fileSize > 10000);
    }

    private static void validateKeyFormat(File privateKeyFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(privateKeyFile))) {
            String firstLine = reader.readLine();
            if (firstLine == null ||
                    (!firstLine.contains("BEGIN PRIVATE KEY") &&
                            !firstLine.contains("BEGIN RSA PRIVATE KEY"))) {
                throw new IOException("Formato de archivo de clave privada inválido");
            }
        }
    }

    private static boolean validateLoadedKey(PrivateKey privateKey) {
        if (privateKey == null) {
            return false;
        }
        // Validación del algoritmo
        String algorithm = privateKey.getAlgorithm();
        if (!"RSA".equals(algorithm)) {
            return false;
        }
        // Validación del formato
        String format = privateKey.getFormat();
        if (!"PKCS#8".equals(format)) {
            return false;
        }
        return true;
    }

    private boolean validateFileProperties(File file){
        if (!file.exists()) {
            return false;
        }
        if (!file.isFile()) {
            return false;
        }
        if (!file.canRead()) {
            return false;
        }
        return true;
    }

    private boolean validateCertificateFileSize(File file) {
        return file.length() > 0 && file.length() <= MAX_CERTIFICATE_SIZE;
    }

    private boolean validateCertificateFileExtension(File file) {
        String fileName = file.getName().toLowerCase();
        return fileName.endsWith(".pem") || fileName.endsWith(".crt") || fileName.endsWith(".cer");
    }

    private boolean validateCertificateContent(File file) throws IOException {
        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);

        if (!content.contains(BEGIN_CERT) || !content.contains(END_CERT)) {
            return false;
        }

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            try (InputStream is = new ByteArrayInputStream(content.getBytes())) {
                cf.generateCertificate(is);
                return true;
            }
        } catch (CertificateException e) {
            return false;
        }
    }

    private boolean isValidPasswordFileExtensions(String passwordFilePath) {
        if (StringUtils.isBlank(passwordFilePath)) {
            return false;
        }
        if (!passwordFilePath.toLowerCase().endsWith(PASSWORD_FILE_EXTENSION)) {
            return false;
        }
        return true;
    }

    private boolean validatePasswordFileProperties(File passwordFile) {
        // Verificar existencia
        if (!passwordFile.exists()) {
            return false;
        }

        // Verificar que es un archivo
        if (!passwordFile.isFile()) {
            return false;
        }

        // Verificar permisos
        if (!passwordFile.canRead()) {
            return false;
        }

        // Verificar tamaño
        long fileSize = passwordFile.length();
        if (fileSize == 0 || fileSize > 1024) { // máximo 1KB
            return false;
        }

        return true;
    }

    private boolean validatePasswordFileContent(File passwordFile) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(passwordFile))) {
            String passwordContent = reader.readLine();

            // Verificar que solo hay una línea
            if (reader.readLine() == null) {
                return false;
            }

            // Verificar contenido vacío
            if (StringUtils.isBlank(passwordContent)) {
                return false;
            }

            // Verificar que el contenido está codificado en base64
            if (!isBase64Encoded(passwordContent)) {
                return false;
            }

            // Verificar caracteres válidos después de decodificar
            String decodedContent = new String(Base64.getDecoder().decode(passwordContent));
            if (!containsOnlyValidCharacters(decodedContent)) {
                return false;
            }
            return true;
        }
    }

    private boolean isBase64Encoded(String content) {
        try {
            byte[] decode = Base64.getDecoder().decode(content);
            return decode.length != 0;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean containsOnlyValidCharacters(String content) {
        // Patrón para caracteres válidos (letras, números y caracteres especiales comunes)
        return content.matches("^[a-zA-Z0-9@#$%^&+=!*()_\\-]+$");
    }

    private boolean validatePrivateKeyFileExtension(File privateKeyFile) {
        try {
            String fileName = privateKeyFile.getName().toLowerCase();
            String extension = fileName.substring(fileName.lastIndexOf('.'));
            if (!VALID_EXTENSIONS.contains(extension)) {
                return false;
            }
            // Validación adicional del nombre del archivo
            if (fileName.contains("..") || fileName.contains("@")) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validatePrivateKeyContent(File privateKeyFile) {
        try (BufferedReader reader = new BufferedReader(new FileReader(privateKeyFile))) {
            List<String> lines = reader.lines().collect(Collectors.toList());
            if (lines.isEmpty()) {
                return false;
            }
            // Validar estructura básica
            if (!validateKeyStructure(lines)) {
                return false;
            }
            // Validar contenido Base64
            if (!validateBase64Content(lines)) {
                return false;
            }
            // Validar tamaño mínimo
            if (!validateKeySize(lines)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean validateKeyStructure(List<String> lines) {
        if (lines.size() < 3) {
            return false;
        }
        String firstLine = lines.get(0).trim();
        String lastLine = lines.get(lines.size() - 1).trim();
        // Validar header
        if (!VALID_HEADERS.contains(firstLine)) {
            return false;
        }
        // Validar footer
        if (!VALID_FOOTERS.contains(lastLine)) {
            return false;
        }
        // Validar que el header y footer coincidan en tipo
        String headerType = firstLine.substring(11, firstLine.length() - 5);
        String footerType = lastLine.substring(9, lastLine.length() - 5);
        if (!headerType.equals(footerType)) {
            return false;
        }
        return true;
    }

    private boolean validateBase64Content(List<String> lines) {
        // Obtener solo las líneas de contenido (excluyendo header y footer)
        List<String> contentLines = lines.subList(1, lines.size() - 1);
        String content = String.join("", contentLines).trim();
        try {
            // Intentar decodificar el contenido
            Base64.getDecoder().decode(content);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private boolean validateKeySize(List<String> lines) {
        // Calcular tamaño aproximado en bits
        int contentLength = String.join("", lines).length() * 6; // aproximadamente 6 bits por caracter Base64
        // Validar tamaño mínimo (por ejemplo, 2048 bits)
        if (contentLength < 2048) {
            return false;
        }
        return true;
    }
}

