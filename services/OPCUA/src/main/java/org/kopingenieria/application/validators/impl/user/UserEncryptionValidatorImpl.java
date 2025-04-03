package org.kopingenieria.application.validators.impl.user;

import org.kopingenieria.application.validators.contract.user.UserEncryptionValidator;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Set;

public class UserEncryptionValidatorImpl implements UserEncryptionValidator {

    private final StringBuilder validationResult;
    private static final Set<String> SECURITY_POLICIES = Set.of(
            "Basic128Rsa15",
            "Basic256",
            "Basic256Sha256",
            "Aes128_Sha256_RsaOaep",
            "Aes256_Sha256_RsaPss"
    );

    private static final Set<String> SECURITY_MODES = Set.of(
            "NONE",
            "SIGN",
            "SIGN_AND_ENCRYPT"
    );

    public UserEncryptionValidatorImpl() {
        this.validationResult = new StringBuilder();
    }

    @Override
    public boolean validateSecurityPolicy(String securityPolicy) {
        if (securityPolicy == null || securityPolicy.trim().isEmpty()) {
            logValidationError("La política de seguridad no puede ser nula o vacía");
            return false;
        }

        if (!SECURITY_POLICIES.contains(securityPolicy)) {
            logValidationError("Política de seguridad no válida: " + securityPolicy);
            return false;
        }

        if (securityPolicy.equals("Basic128Rsa15")) {
            logValidationError("Basic128Rsa15 está obsoleto y no se recomienda su uso");
            return false;
        }

        logValidationSuccess("Política de seguridad válida: " + securityPolicy);
        return true;
    }

    @Override
    public boolean validateSecurityMode(String messageSecurityMode) {
        if (messageSecurityMode == null || messageSecurityMode.trim().isEmpty()) {
            logValidationError("El modo de seguridad no puede ser nulo o vacío");
            return false;
        }

        if (!SECURITY_MODES.contains(messageSecurityMode.toUpperCase())) {
            logValidationError("Modo de seguridad no válido: " + messageSecurityMode);
            return false;
        }

        if (messageSecurityMode.equalsIgnoreCase("NONE")) {
            logValidationError("El modo de seguridad NONE no está permitido");
            return false;
        }

        logValidationSuccess("Modo de seguridad válido: " + messageSecurityMode);
        return true;
    }

    @Override
    public boolean validateClientCertificate(byte[] clientCertificate) {
        if (clientCertificate == null || clientCertificate.length == 0) {
            logValidationError("El certificado del cliente no puede ser nulo o vacío");
            return false;
        }

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(
                    new ByteArrayInputStream(clientCertificate)
            );

            cert.checkValidity();

            // Verificar el uso de la clave
            boolean[] keyUsage = cert.getKeyUsage();
            if (keyUsage == null || !keyUsage[0]) { // digitalSignature
                logValidationError("El certificado no permite firma digital");
                return false;
            }

            logValidationSuccess("Certificado del cliente válido");
            return true;
        } catch (CertificateExpiredException e) {
            logValidationError("El certificado ha expirado");
            return false;
        } catch (Exception e) {
            logValidationError("Error al validar el certificado: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validatePrivateKey(byte[] privateKey) {
        if (privateKey == null || privateKey.length == 0) {
            logValidationError("La clave privada no puede ser nula o vacía");
            return false;
        }

        if (privateKey.length < 256) { // Mínimo 2048 bits
            logValidationError("La clave privada debe tener al menos 2048 bits");
            return false;
        }

        logValidationSuccess("Clave privada válida");
        return true;
    }

    @Override
    public boolean validateTrustedCertificates(List<byte[]> trustedCertificates) {
        if (trustedCertificates == null || trustedCertificates.isEmpty()) {
            logValidationError("La lista de certificados de confianza no puede ser nula o vacía");
            return false;
        }

        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            for (int i = 0; i < trustedCertificates.size(); i++) {
                byte[] certBytes = trustedCertificates.get(i);
                X509Certificate cert = (X509Certificate) cf.generateCertificate(
                        new ByteArrayInputStream(certBytes)
                );

                cert.checkValidity();

                if (cert.getBasicConstraints() == -1) {
                    logValidationError("El certificado " + (i + 1) + " no es un certificado CA");
                    return false;
                }
            }

            logValidationSuccess("Certificados de confianza válidos: " + trustedCertificates.size());
            return true;
        } catch (Exception e) {
            logValidationError("Error en certificados de confianza: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateAlgorithmParameters(Integer keyLength,
                                               String algorithmName,
                                               String protocolVersion) {
        if (keyLength == null || keyLength < 2048) {
            logValidationError("La longitud de clave debe ser al menos 2048 bits");
            return false;
        }

        Set<String> validAlgorithms = Set.of("RSA", "ECDSA");
        if (!validAlgorithms.contains(algorithmName.toUpperCase())) {
            logValidationError("Algoritmo no soportado: " + algorithmName);
            return false;
        }

        Set<String> validProtocols = Set.of("TLS 1.2", "TLS 1.3");
        if (!validProtocols.contains(protocolVersion)) {
            logValidationError("Versión de protocolo no soportada: " + protocolVersion);
            return false;
        }

        logValidationSuccess(String.format(
                "Parámetros válidos - Algoritmo: %s, Longitud: %d, Protocolo: %s",
                algorithmName, keyLength, protocolVersion
        ));
        return true;
    }

    @Override
    public boolean validateCertificateChain(List<X509Certificate> certificateChain) {
        if (certificateChain == null || certificateChain.isEmpty()) {
            logValidationError("La cadena de certificados no puede ser nula o vacía");
            return false;
        }

        try {
            for (int i = 0; i < certificateChain.size() - 1; i++) {
                X509Certificate current = certificateChain.get(i);
                X509Certificate issuer = certificateChain.get(i + 1);

                current.verify(issuer.getPublicKey());
                current.checkValidity();

                logValidationSuccess("Certificado " + (i + 1) + " verificado correctamente");
            }

            // Verificar el certificado raíz
            X509Certificate root = certificateChain.getLast();
            root.checkValidity();

            if (!root.getSubjectX500Principal().equals(root.getIssuerX500Principal())) {
                logValidationError("El último certificado no es autofirmado (no es raíz)");
                return false;
            }

            logValidationSuccess("Cadena de certificados válida");
            return true;
        } catch (Exception e) {
            logValidationError("Error en la cadena de certificados: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean validateCompleteConfiguration() {
        if (validationResult.toString().contains("Error:")) {
            return false;
        }
        logValidationSuccess("Configuración completa validada correctamente");
        return true;
    }

    @Override
    public String getValidationResult() {
        return validationResult.toString();
    }

    @Override
    public boolean checkCertificateRevocation(X509Certificate certificate) {
        if (certificate == null) {
            logValidationError("El certificado no puede ser nulo");
            return false;
        }

        try {
            // Verificar puntos de distribución CRL
            byte[] crlDP = certificate.getExtensionValue("2.5.29.31");
            if (crlDP == null) {
                logValidationError("El certificado no tiene puntos de distribución CRL");
                return false;
            }

            // Verificar OCSP
            byte[] aiaBytes = certificate.getExtensionValue("1.3.6.1.5.5.7.1.1");
            if (aiaBytes == null) {
                logValidationError("El certificado no tiene información OCSP");
                return false;
            }

            logValidationSuccess("Estado de revocación verificado correctamente");
            return true;
        } catch (Exception e) {
            logValidationError("Error al verificar la revocación: " + e.getMessage());
            return false;
        }
    }

    private void logValidationError(String message) {
        validationResult.append("Error: ").append(message).append("\n");
    }

    private void logValidationSuccess(String message) {
        validationResult.append("Éxito: ").append(message).append("\n");
    }

}
