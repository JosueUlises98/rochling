package org.kopingenieria.validators.client.opcua;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.model.enums.network.connection.UrlType;
import java.net.InetAddress;
import java.net.URI;
import java.util.function.Supplier;


public class OpcUaConnectionValidator implements ConnectionValidator {
    /**
     * Logger instance used for logging messages in the {@code Connection} class.
     * This provides a mechanism to track, debug, and report application's operational
     * information and potential issues throughout the validation and connection logic.
     */
    private static final Logger logger = LogManager.getLogger(OpcUaConnectionValidator.class);
    /**
     * Validates whether the provided OPC UA client's session is active.
     * This method checks the client's session state and ensures it is completed successfully.
     *
     * @param client the OPC UA client whose session is to be validated
     * @return {@code true} if the client's session is active and successfully completed, {@code false} otherwise
     */
    public boolean validateActiveSession(UaClient client) {
        try {
            if (client == null) {
                logger.error("Cliente OPC UA es nulo");
                return false;
            }
            return client.getSession().isDone();
        } catch (Exception e) {
            logger.error("Error validando sesión activa: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates if the provided OPC UA client's session is valid.
     * This method checks if the client is not null and ensures that its session is active
     * and not completed.
     *
     * @param client the OPC UA client whose session validity is to be checked
     * @return {@code true} if the session is valid and active; {@code false} if the client
     *         is null, the session is completed, or an error occurs during validation
     */
    public boolean validateValidSession(UaClient client) {
        if (StringUtils.isBlank(client.toString())) {
            logger.error("Cliente OPC UA es nulo.");
            return false;
        }
        try {
            return client.getSession() != null && !client.getSession().isDone();
        } catch (Exception e) {
            logger.error("Error validando sesión válida: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates whether the provided host is resolvable and matches the expected remote OPC UA server address.
     * This method checks if the host string is valid, parses it as a URI, attempts to resolve it to an address,
     * and verifies if it matches the configured remote server's IP address.
     *
     * @param host the string representation of the host to be validated
     * @return {@code true} if the host is non-empty, resolvable, and matches the expected remote server address;
     *         otherwise {@code false}
     */
    public boolean validateHost(String host) {
        if (StringUtils.isBlank(host)) {
            logger.error("Host es nulo o vacío");
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
            logger.error("Host no resolvible para la URL: {}. Mensaje: {}", host, e.getMessage());
            return false;
        }
        return false;
    }
    /**
     * Validates whether the given port number is within the acceptable range and is not blank.
     * The port number must be between 0 and 65535, inclusive.
     *
     * @param port the port number to validate
     * @return {@code true} if the port is within the valid range and non-blank, {@code false} otherwise
     */
    public boolean validatePort(int port) {
        if (StringUtils.isBlank(String.valueOf(port))) {
            logger.error("Puerto es nulo o vacío");
            return false;
        }
        if (port < 0 || port > 65535) {
            logger.error("El puerto debe estar entre 0 y 65535.");
            return false;
        }
        return true;
    }
    /**
     * Validates whether the given OPC UA endpoint is valid based on the provided `UrlType`.
     * The method checks if the URL of the endpoint is non-blank and determines if the
     * endpoint type is one of the valid predefined types (e.g., OPCUA_LOCAL, OPCUA_REMOTE, OPCUA_SECURE).
     *
     * @param endpoint the OPC UA endpoint to validate, represented as a `UrlType` object
     * @return {@code true} if the URL is non-blank and the endpoint type is valid;
     *         otherwise {@code false} if the URL is blank or the endpoint type is invalid
     */
    public boolean validateEndpoint(UrlType endpoint) {
        if (StringUtils.isBlank(endpoint.getUrl())) {
            logger.error("Endpoint es nulo o vacío");
            return false;
        }
        switch (endpoint){
            case OPCUA_LOCAL, OPCUA_REMOTE, OPCUA_SECURE -> {
                return true;
            }
            default -> {
                logger.error("Endpoint inválido");
                return false;
            }
        }
    }
    /**
     * Validates the provided timeout value to ensure it meets the defined criteria.
     * This method checks if the timeout is non-blank, positive, and does not exceed
     * the maximum allowed value of 30000 milliseconds.
     *
     * @param timeout the timeout value to validate, specified in milliseconds
     * @return {@code true} if the timeout value is valid; {@code false} otherwise
     */
    public boolean validateTimeout(int timeout) {
        if (StringUtils.isBlank(String.valueOf(timeout))) {
            logger.error("Timeout es nulo o vacío");
            return false;
        }
        if (timeout <= 0) {
            logger.error("El tiempo de espera debe ser mayor a 0.");
            return false;
        }
        if (timeout > 30000) {
            logger.error("El tiempo de espera excede el máximo permitido.");
            return false;
        }
        return true;
    }
    /**
     * Validates the provided security policy string to ensure it matches one of the predefined valid policies.
     * The validation checks if the input is non-blank and is one of the allowed policy names.
     *
     * @param securityPolicy the security policy string to validate
     * @return {@code true} if the security policy is valid and recognized; {@code false} otherwise
     */
    public boolean validateSecurityPolicy(String securityPolicy) {
        if (StringUtils.isBlank(securityPolicy)) {
            logger.error("Política de Seguridad nula o vacia");
            return false;
        }
        switch (securityPolicy){
            case "None", "Basic128Rsa15", "Basic256", "Basic256Sha256", "Aes128Sha256RsaOaep", "Aes256Sha256RsaPss" -> {
                return true;
            }
            default -> {
                logger.error("Política de Seguridad inválida");
                return false;
            }
        }
    }
    /**
     * Validates the provided security mode string to determine if it matches one of the acceptable modes.
     * This method checks if the input is non-blank and belongs to the predefined valid security modes:
     * "None", "Sign", or "SignAndEncrypt".
     *
     * @param securityMode the security mode string to validate
     * @return {@code true} if the security mode is valid and recognized; {@code false} otherwise
     */
    public boolean validateSecurityMode(String securityMode) {
        if (StringUtils.isBlank(securityMode)) {
            logger.error("Modo de Seguridad es nulo o vacío");
            return false;
        }
        switch (securityMode){
            case "None", "Sign", "SignAndEncrypt" -> {
                return true;
            }
            default -> {
                logger.error("Modo de Seguridad inválido");
                return false;
            }
        }
    }
    /**
     * Validates the format of the provided certificate.
     * Checks if the certificate is non-blank and adheres to the expected PEM structure.
     *
     * @param certificate the string representation of the certificate to validate
     * @return {@code true} if the certificate is non-blank and conforms to the expected format, {@code false} otherwise
     */
    public boolean validateCertificate(String certificate) {
        if (StringUtils.isBlank(certificate)) {
            logger.error("Certificado es nulo o vacío");
            return false;
        }
        try {
            // Placeholder logic for certificate validation (could involve checking X.509 compliance)
            boolean isValidCertificate = certificate.startsWith("-----BEGIN CERTIFICATE-----") &&
                                          certificate.endsWith("-----END CERTIFICATE-----");
            if (!isValidCertificate) {
                logger.error("Formato del certificado inválido");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar el certificado: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates the format of the provided private key string.
     * This method checks if the private key is non-blank and adheres to the expected PEM structure
     * with the correct delimiters for a private key.
     *
     * @param privateKey the string representation of the private key to be validated
     * @return {@code true} if the private key is non-blank and conforms to the expected format; {@code false} otherwise
     */
    public boolean validatePrivateKey(String privateKey) {
        if (StringUtils.isBlank(privateKey)) {
            logger.error("ClavePrivada es nula o vacía");
            return false;
        }
        try {
            // Check key formatting based on typical PEM structure
            boolean isValidPrivateKey = privateKey.startsWith("-----BEGIN PRIVATE KEY-----") &&
                                        privateKey.endsWith("-----END PRIVATE KEY-----");
            if (!isValidPrivateKey) {
                logger.error("Formato de la clave privada inválido");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar la clave privada: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates the provided certificate chain to ensure it adheres to the expected PEM structure.
     * Each certificate in the chain is validated to confirm it starts and ends with the correct delimiters
     * ("-----BEGIN CERTIFICATE-----" and "-----END CERTIFICATE-----").
     *
     * @param certificateChain the string representation of the certificate chain to be validated,
     *                         containing multiple concatenated certificates in PEM format
     * @return {@code true} if the certificate chain is valid and appropriately formatted;
     *         {@code false} otherwise
     */
    public boolean validateCertificateChain(String certificateChain) {
        if (StringUtils.isBlank(certificateChain)) {
            logger.error("Cadena de Certificado es nula o vacía");
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
                        logger.error("Formato de certificado inválido en la cadena: {}", cert);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar la cadena de certificado: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates the provided certificate alias string to ensure it meets the defined criteria.
     * This method checks if the alias is non-blank, does not exceed the maximum allowed length,
     * and only contains valid characters (letters, numbers, hyphens, and underscores).
     *
     * @param certificateAlias the certificate alias string to validate
     * @return {@code true} if the certificate alias is valid; {@code false} otherwise
     */
    public boolean validateCertificateAlias(String certificateAlias) {
        if (StringUtils.isBlank(certificateAlias)) {
            logger.error("Alias vacío o nulo");
            return false;
        }
        // Validate alias with professional formatting and allowed constraints
        try {
            if (certificateAlias.length() > 50) {
                logger.error("El alias excede la longitud máxima permitida de 50 caracteres");
                return false;
            }
            if (!certificateAlias.matches("^[a-zA-Z0-9_-]+$")) {
                logger.error("El alias contiene caracteres inválidos. Solo se permiten letras, números, guiones y guiones bajos");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar el alias del certificado: {}", e.getMessage());
            return false;
        }
        
    }
    /**
     * Validates whether the provided certificate file exists, is readable,
     * and adheres to the expected PEM structure. This method ensures that
     * the file content starts with "-----BEGIN CERTIFICATE-----" and ends
     * with "-----END CERTIFICATE-----".
     *
     * @param certificateFile the path to the certificate file to validate
     * @return {@code true} if the certificate file exists, is readable, and conforms
     *         to the expected PEM format; {@code false} otherwise
     */
    public boolean validateCertificateFile(String certificateFile) {
        if (StringUtils.isBlank(certificateFile)) {
            logger.error("Archivo de Certificación nulo o vacío");
            return false;
        }
        try {
            java.io.File file = new java.io.File(certificateFile);
            if (!file.exists()) {
                logger.error("El archivo de certificación no existe: {}", certificateFile);
                return false;
            }
            if (!file.canRead()) {
                logger.error("El archivo de certificación no se puede leer: {}", certificateFile);
                return false;
            }
            // Read file content and validate certificate structure
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            boolean isValidCertificate = fileContent.startsWith("-----BEGIN CERTIFICATE-----") &&
                                          fileContent.endsWith("-----END CERTIFICATE-----");
            if (!isValidCertificate) {
                logger.error("El formato del archivo de certificación es inválido: {}", certificateFile);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar el archivo de certificación: {}. Mensaje: {}", certificateFile, e.getMessage());
            return false;
        }
    }
    /**
     * Validates whether the provided private key file exists, is readable, and adheres to
     * the expected PEM format. The method checks the file's presence, readability, and its
     * contents to ensure it starts with "-----BEGIN PRIVATE KEY-----" and ends with
     * "-----END PRIVATE KEY-----".
     *
     * @param privateKeyFile the path to the private key file to validate
     * @return {@code true} if the private key file is valid, readable, and conforms to
     *         the expected format; {@code false} otherwise
     */
    public boolean validatePrivateKeyFile(String privateKeyFile) {
        if (StringUtils.isBlank(privateKeyFile)) {
            logger.error("Archivo de clave privada nulo o vacío");
            return false;
        }
        try {
            java.io.File file = new java.io.File(privateKeyFile);
            if (!file.exists()) {
                logger.error("El archivo de clave privada no existe: {}", privateKeyFile);
                return false;
            }
            if (!file.canRead()) {
                logger.error("El archivo de clave privada no se puede leer: {}", privateKeyFile);
                return false;
            }
            // Read file content and validate private key structure
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            boolean isValidPrivateKey = fileContent.startsWith("-----BEGIN PRIVATE KEY-----") &&
                                        fileContent.endsWith("-----END PRIVATE KEY-----");
            if (!isValidPrivateKey) {
                logger.error("El formato del archivo de clave privada es inválido: {}", privateKeyFile);
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar el archivo de clave privada: {}. Mensaje: {}", privateKeyFile, e.getMessage());
            return false;
        }
    }
    /**
     * Validates the provided certificate chain file to ensure it exists, is readable,
     * and its content adheres to the expected PEM structure. Each certificate in the file
     * is checked to confirm it starts with "-----BEGIN CERTIFICATE-----" and ends with
     * "-----END CERTIFICATE-----".
     *
     * @param certificateChainFile the path to the certificate chain file to validate
     * @return {@code true} if the certificate chain file exists, is readable, and properly formatted;
     *         {@code false} otherwise
     */
    public boolean validateCertificateChainFile(String certificateChainFile) {
        if (StringUtils.isBlank(certificateChainFile)) {
            logger.error("Archivo de cadena de certificado nulo o vacío");
            return false;
        }
        try {
            java.io.File file = new java.io.File(certificateChainFile);
            if (!file.exists()) {
                logger.error("El archivo de cadena de certificado no existe: {}", certificateChainFile);
                return false;
            }
            if (!file.canRead()) {
                logger.error("El archivo de cadena de certificado no se puede leer: {}", certificateChainFile);
                return false;
            }
            String fileContent = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            String[] certificates = fileContent.split("-----END CERTIFICATE-----");
            for (String cert : certificates) {
                cert = cert.trim();
                if (!cert.isEmpty()) {
                    cert += "-----END CERTIFICATE-----";
                    if (!cert.startsWith("-----BEGIN CERTIFICATE-----") || !cert.endsWith("-----END CERTIFICATE-----")) {
                        logger.error("Formato de cadena de certificado inválido: {}", cert);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar el archivo de cadena de certificados: {}. Mensaje: {}", certificateChainFile, e.getMessage());
            return false;
        }
    }
    /**
     * Validates a certificate alias file by checking its existence, readability,
     * content format, and adherence to specified rules.
     *
     * @param certificateAliasFile the path to the certificate alias file to validate
     * @return true if the certificate alias file meets all validation criteria; false otherwise
     */
    public boolean validateCertificateAliasFile(String certificateAliasFile) {
        if (StringUtils.isBlank(certificateAliasFile)) {
            logger.error("Alias del archivo no puede ser nulo o vacío");
            return false;
        }
        try {
            java.io.File file = new java.io.File(certificateAliasFile);
            if (!file.exists()) {
                logger.error("El archivo del alias de certificado no existe: {}", certificateAliasFile);
                return false;
            }
            if (!file.canRead()) {
                logger.error("El archivo del alias de certificado no se puede leer: {}", certificateAliasFile);
                return false;
            }
            // Read the alias from the file
            String alias = new String(java.nio.file.Files.readAllBytes(file.toPath())).trim();
            if (alias.length() > 50) {
                logger.error("El alias del archivo excede la longitud máxima permitida de 50 caracteres");
                return false;
            }
            if (!alias.matches("^[a-zA-Z0-9_-]+$")) {
                logger.error("El alias contiene caracteres inválidos. Solo se permiten letras, números, guiones y guiones bajos.");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar el alias del archivo de certificado: {}. Mensaje: {}", certificateAliasFile, e.getMessage());
            return false;
        }
    }
    /**
     * Validates the provided certificate file password based on specific security criteria.
     * The password must be non-null, non-empty, and meet the following conditions:
     * - Have a minimum length of 8 characters
     * - Contain at least one uppercase letter
     * - Contain at least one lowercase letter
     * - Contain at least one numeric character
     * - Contain at least one special character (e.g., @, #, $, etc.)
     * Logs appropriate error messages if the password does not satisfy any of the criteria or if an exception occurs.
     *
     * @param certificateFilePassword the password for the certificate file to validate
     * @return true if the password meets all validation criteria; false otherwise
     */
    public boolean validateCertificateFilePassword(String certificateFilePassword) {
        if (StringUtils.isBlank(certificateFilePassword)) {
            logger.error("Contraseña del archivo del certificado no puede ser nula o vacia");
            return false;
        }
        try {
            if (certificateFilePassword.length() < 8) {
                logger.error("La contraseña del archivo del certificado es demasiado corta. Debe tener al menos 8 caracteres.");
                return false;
            }
            if (!certificateFilePassword.matches(".*[A-Z].*")) {
                logger.error("La contraseña del archivo del certificado debe contener al menos una letra mayúscula.");
                return false;
            }
            if (!certificateFilePassword.matches(".*[a-z].*")) {
                logger.error("La contraseña del archivo del certificado debe contener al menos una letra minúscula.");
                return false;
            }
            if (!certificateFilePassword.matches(".*\\d.*")) {
                logger.error("La contraseña del archivo del certificado debe contener al menos un número.");
                return false;
            }
            if (!certificateFilePassword.matches(".*[@#$%^&+=].*")) {
                logger.error("La contraseña del archivo del certificado debe contener al menos un carácter especial (como @, #, $, etc.).");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar la contraseña del archivo del certificado: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates the provided private key file password against specific security criteria.
     * The password must not be null or empty, must be at least 8 characters long, and it
     * must contain at least one uppercase letter, one lowercase letter, one number,
     * and one special character.
     *
     * @param privateKeyFilePassword the password for the private key file to be validated
     * @return true if the password meets all security criteria, false otherwise
     */
    public boolean validatePrivateKeyFilePassword(String privateKeyFilePassword) {
        if (StringUtils.isBlank(privateKeyFilePassword)) {
            logger.error("Contraseña del archivo de clave privada no puede ser nula o vacía");
            return false;
        }
        try {
            if (privateKeyFilePassword.length() < 8) {
                logger.error("La contraseña del archivo de clave privada es demasiado corta. Debe tener al menos 8 caracteres.");
                return false;
            }
            if (!privateKeyFilePassword.matches(".*[A-Z].*")) {
                logger.error("La contraseña del archivo de clave privada debe contener al menos una letra mayúscula.");
                return false;
            }
            if (!privateKeyFilePassword.matches(".*[a-z].*")) {
                logger.error("La contraseña del archivo de clave privada debe contener al menos una letra minúscula.");
                return false;
            }
            if (!privateKeyFilePassword.matches(".*\\d.*")) {
                logger.error("La contraseña del archivo de clave privada debe contener al menos un número.");
                return false;
            }
            if (!privateKeyFilePassword.matches(".*[@#$%^&+=].*")) {
                logger.error("La contraseña del archivo de clave privada debe contener al menos un carácter especial (como @, #, $, etc.).");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar la contraseña del archivo de clave privada: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates the password for a certificate chain file based on predefined security criteria.
     *
     * @param certificateChainFilePassword The password to be validated. Must not be null or empty and
     *                                      should meet the requirements for minimum length, special characters,
     *                                      uppercase letters, lowercase letters, and digits.
     * @return true if the password meets all the security criteria; false otherwise.
     */
    public boolean validateCertificateChainFilePassword(String certificateChainFilePassword) {
        if (StringUtils.isBlank(certificateChainFilePassword)) {
            logger.error("Contraseña del archivo de la cadena de certificado no puede ser nula o vacía");
            return false;
        }
        try {
            if (certificateChainFilePassword.length() < 8) {
                logger.error("La contraseña del archivo de la cadena de certificado es demasiado corta. Debe tener al menos 8 caracteres.");
                return false;
            }
            if (!certificateChainFilePassword.matches(".*[A-Z].*")) {
                logger.error("La contraseña del archivo de la cadena de certificado debe contener al menos una letra mayúscula.");
                return false;
            }
            if (!certificateChainFilePassword.matches(".*[a-z].*")) {
                logger.error("La contraseña del archivo de la cadena de certificado debe contener al menos una letra minúscula.");
                return false;
            }
            if (!certificateChainFilePassword.matches(".*\\d.*")) {
                logger.error("La contraseña del archivo de la cadena de certificado debe contener al menos un número.");
                return false;
            }
            if (!certificateChainFilePassword.matches(".*[@#$%^&+=].*")) {
                logger.error("La contraseña del archivo de la cadena de certificado debe contener al menos un carácter especial (como @, #, $, etc.).");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar la contraseña de la cadena de certificado: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates the given certificate alias file password based on the following criteria:
     * - The password cannot be null or empty.
     * - The password must be at least 8 characters long.
     * - The password must contain at least one uppercase letter, one lowercase letter, one digit, and one special character.
     *
     * @param certificateAliasFilePassword The certificate alias file password to validate.
     * @return true if the password meets the validation criteria, otherwise false.
     */
    public boolean validateCertificateAliasFilePassword(String certificateAliasFilePassword) {
        if (StringUtils.isBlank(certificateAliasFilePassword)) {
            logger.error("Contraseña del archivo alias del certificado no puede ser nula o vacía");
            return false;
        }
        try {
            if (certificateAliasFilePassword.length() < 8) {
                logger.error("La contraseña del archivo alias del certificado es demasiado corta. Debe tener al menos 8 caracteres.");
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*[A-Z].*")) {
                logger.error("La contraseña del archivo alias del certificado debe contener al menos una letra mayúscula.");
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*[a-z].*")) {
                logger.error("La contraseña del archivo alias del certificado debe contener al menos una letra minúscula.");
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*\\d.*")) {
                logger.error("La contraseña del archivo alias del certificado debe contener al menos un número.");
                return false;
            }
            if (!certificateAliasFilePassword.matches(".*[@#$%^&+=].*")) {
                logger.error("La contraseña del archivo alias del certificado debe contener al menos un carácter especial (como @, #, $, etc.).");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error al validar la contraseña del archivo alias del certificado: {}", e.getMessage());
            return false;
        }
    }
    /**
     * Validates if the given host string corresponds to a localhost address.
     *
     * @param host the host string to be validated, must be a valid URI.
     * @return true if the host corresponds to a localhost address, false otherwise or in case of an error.
     */
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
            logger.error("Error de localhost para la URL: {}. Mensaje: {}", host, e.getMessage());
            return false;
        }
        return false;
    }
    /**
     * Validates a UaClient instance provided by a Supplier.
     *
     * The method performs several checks to ensure that the UaClient instance
     * meets criteria such as having a valid and active session, valid host,
     * and valid port number. If any validation fails, appropriate error messages
     * are logged, and the method returns false.
     *
     * @param clientSupplier the Supplier that provides a UaClient instance for validation
     * @return true if the UaClient instance satisfies all validation checks; false otherwise
     */
    public boolean validate(Supplier<UaClient> clientSupplier) {
        try {
            UaClient client = clientSupplier.get();
            if (!validateActiveSession(client)) {
                logger.error("Error: La sesión activa del cliente no es válida");
                return false;
            }
            if (!validateValidSession(client)) {
                logger.error("Error: La sesión válida del cliente no se pudo validar");
                return false;
            }
            if (!validateHost(client.getConfig().getEndpoint().getEndpointUrl().substring(7))) {
                logger.error("Error: El host del cliente no es válido");
                return false;
            }
            if (!validatePort(Integer.parseInt(client.getConfig().getEndpoint().getEndpointUrl().substring(7).split(":")[1]))) {
                logger.error("Error: El puerto proporcionado es inválido");
                return false;
            }
            return true;
        } catch (Exception e) {
            logger.error("Error validando UaClient: {}", e.getMessage());
            return false;
        }
    }

}
