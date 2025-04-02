package org.kopingenieria.util.security.user;

import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.kopingenieria.config.opcua.user.UserConfiguration;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;
import org.kopingenieria.util.loader.CertificateLoader;
import org.kopingenieria.util.loader.PrivateKeyLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Component("certificateManager")
public class UserCertificateManager {

    @Getter
    private X509Certificate clientCertificate;
    @Getter
    private PrivateKey privateKey;

    public void configurarCertificados(UserConfiguration userConfig)
            throws OpcUaConfigurationException {
        try {
            // Cargar certificado y clave privada una sola vez
            this.clientCertificate = CertificateLoader.loadX509Certificate(
                    userConfig.getAuthentication().getCertificatePath()
            );
            this.privateKey = PrivateKeyLoader.loadPrivateKey(
                    userConfig.getAuthentication().getPrivateKeyPath()
            );

            validarCertificado(this.clientCertificate);

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error configurando certificados", e);
        }
    }

    public void aplicarConfiguracionSeguridad(OpcUaClientConfigBuilder config,
                                              UserConfiguration userConfig) throws OpcUaConfigurationException {
        try {
            // Configurar validador de certificados
            File trustListFile = configurarListaConfianza(userConfig.getAuthentication().getTrustListPath());
            DefaultTrustListManager trustListManager = new DefaultTrustListManager(trustListFile);
            DefaultClientCertificateValidator certificateValidator =
                    new DefaultClientCertificateValidator(trustListManager);

            // Aplicar configuración de seguridad
            config.setCertificateValidator(certificateValidator);
            config.setCertificate(clientCertificate);
            config.setKeyPair(new KeyPair(clientCertificate.getPublicKey(),privateKey));

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error aplicando configuración de seguridad", e);
        }
    }

    public void validarCertificado(X509Certificate certificate) throws OpcUaConfigurationException {
        try {
            // Obtener fecha actual
            Date fechaActual = new Date();

            // 1. Validación temporal
            if (fechaActual.after(certificate.getNotAfter())) {
                throw new OpcUaConfigurationException("El certificado ha expirado");
            }
            if (fechaActual.before(certificate.getNotBefore())) {
                throw new OpcUaConfigurationException("El certificado aún no es válido");
            }

            // 2. Verificar firma del certificado
            certificate.verify(certificate.getPublicKey());

            // 3. Validar uso de claves
            boolean[] keyUsage = certificate.getKeyUsage();
            if (keyUsage != null && !keyUsage[0] && !keyUsage[1]) { // digitalSignature y nonRepudiation
                throw new OpcUaConfigurationException("El certificado no tiene los usos de clave requeridos");
            }

            // 4. Verificar versión del certificado
            if (certificate.getVersion() != 3) {
                throw new OpcUaConfigurationException("Versión de certificado no soportada. Se requiere X.509v3");
            }

            // 5. Validar algoritmo de firma
            String sigAlg = certificate.getSigAlgName().toUpperCase();
            if (sigAlg.contains("MD5") || sigAlg.contains("SHA1")) {
                throw new OpcUaConfigurationException("Algoritmo de firma débil detectado: " + sigAlg);
            }

            // 6. Validar longitud de clave
            int keySize = ((RSAPublicKey) certificate.getPublicKey()).getModulus().bitLength();
            if (keySize < 2048) {
                throw new OpcUaConfigurationException("Longitud de clave insegura: " + keySize + " bits");
            }

        } catch (CertificateExpiredException | CertificateNotYetValidException e) {
            throw new OpcUaConfigurationException("Error en la validación temporal del certificado", e);
        } catch (CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                 NoSuchProviderException | SignatureException e) {
            throw new OpcUaConfigurationException("Error en la validación del certificado", e);
        }
    }

    private File configurarListaConfianza(String trustListPath) throws OpcUaConfigurationException {
        if (trustListPath == null || trustListPath.trim().isEmpty()) {
            throw new OpcUaConfigurationException("Ruta de lista de confianza no válida");
        }
        File trustListFile = new File(trustListPath);
        if (!trustListFile.exists() && !trustListFile.mkdirs()) {
            throw new OpcUaConfigurationException("No se pudo crear directorio de lista de confianza");
        }
        return trustListFile;
    }
}
