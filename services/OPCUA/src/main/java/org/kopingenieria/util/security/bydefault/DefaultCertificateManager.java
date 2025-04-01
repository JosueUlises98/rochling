package org.kopingenieria.util.security.bydefault;

import lombok.Getter;
import org.eclipse.milo.opcua.sdk.client.api.config.OpcUaClientConfigBuilder;
import org.eclipse.milo.opcua.stack.client.security.DefaultClientCertificateValidator;
import org.eclipse.milo.opcua.stack.core.security.DefaultTrustListManager;
import org.kopingenieria.config.opcua.bydefault.DefaultConfiguration;
import org.kopingenieria.exception.exceptions.OpcUaConfigurationException;
import org.kopingenieria.util.loader.CertificateLoader;

import java.io.File;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

public class DefaultCertificateManager {

    @Getter
    private X509Certificate clientCertificate;
    @Getter
    private PrivateKey privateKey;

    public void configurarCertificados(DefaultConfiguration defaultConfig)
            throws OpcUaConfigurationException {
        try {
            // Cargar certificado y clave privada una sola vez
            this.clientCertificate = CertificateLoader.loadX509Certificate(
                    defaultConfig.getAuthentication().getCertificatePath()
            );
            this.privateKey = CertificateLoader.loadPrivateKey(
                    defaultConfig.getAuthentication().getPrivateKeyPath()
            );

            validarCertificado(this.clientCertificate);

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error configurando certificados", e);
        }
    }

    public void aplicarConfiguracionSeguridad(OpcUaClientConfigBuilder config,
                                              DefaultConfiguration defaultConfig) throws OpcUaConfigurationException {
        try {
            // Configurar validador de certificados
            File trustListFile = configurarListaConfianza(defaultConfig.getAuthentication().getTrustListPath());
            DefaultTrustListManager trustListManager = new DefaultTrustListManager(trustListFile);
            DefaultClientCertificateValidator certificateValidator =
                    new DefaultClientCertificateValidator(trustListManager);

            // Aplicar configuración de seguridad
            config.setCertificateValidator(certificateValidator);
            config.setCertificate(clientCertificate);
            config.setKeyPair(new KeyPair(clientCertificate.getPublicKey(), privateKey));

        } catch (Exception e) {
            throw new OpcUaConfigurationException("Error aplicando configuración de seguridad", e);
        }
    }

    private void validarCertificado(X509Certificate certificate) throws OpcUaConfigurationException {
        Date fechaActual = new Date();
        // Validar fecha de expiración
        if (fechaActual.after(certificate.getNotAfter())) {
            throw new OpcUaConfigurationException("El certificado ha expirado");
        }
        // Validar fecha de inicio de validez
        if (fechaActual.before(certificate.getNotBefore())) {
            throw new OpcUaConfigurationException("El certificado aún no es válido");
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
