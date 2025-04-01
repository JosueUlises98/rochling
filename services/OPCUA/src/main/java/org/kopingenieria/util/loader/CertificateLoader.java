package org.kopingenieria.util.loader;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component("certificateLoader")
public class CertificateLoader {

    public static X509Certificate loadX509Certificate(String certificatePath)
            throws CertificateException, IOException {
        try (FileInputStream fis = new FileInputStream(certificatePath)) {
            // Primero intentamos cargar como DER
            try {
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                return (X509Certificate) cf.generateCertificate(fis);
            } catch (CertificateException e) {
                // Si falla, intentamos como PEM
                try (PemReader pemReader = new PemReader(new FileReader(certificatePath))) {
                    PemObject pemObject = pemReader.readPemObject();
                    ByteArrayInputStream bis = new ByteArrayInputStream(pemObject.getContent());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    return (X509Certificate) cf.generateCertificate(bis);
                }
            }
        }
    }

    public static PrivateKey loadPrivateKey(String privateKeyPath)
            throws IOException, GeneralSecurityException {
        byte[] keyBytes = Files.readAllBytes(new File(privateKeyPath).toPath());
        // Intenta primero como PKCS#8
        try {
            return loadPkcs8PrivateKey(keyBytes);
        } catch (InvalidKeySpecException e) {
            // Si falla, intenta como PEM
            try {
                return loadPemPrivateKey(privateKeyPath);
            } catch (Exception pemException) {
                // Si ambos fallan, intenta como PKCS#1
                return loadPkcs1PrivateKey(keyBytes);
            }
        }
    }

    private static PrivateKey loadPkcs8PrivateKey(byte[] keyBytes)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }

    private static PrivateKey loadPemPrivateKey(String privateKeyPath)
            throws IOException {
        try (PEMParser pemParser = new PEMParser(new FileReader(privateKeyPath))) {
            Object object = pemParser.readObject();
            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();

            if (object instanceof PEMKeyPair) {
                return converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
            } else if (object instanceof PrivateKeyInfo) {
                return converter.getPrivateKey((PrivateKeyInfo) object);
            }
            throw new IOException("Formato PEM no soportado");
        }
    }

    private static PrivateKey loadPkcs1PrivateKey(byte[] keyBytes)
            throws IOException, GeneralSecurityException {
        // Convertir PKCS#1 a PKCS#8
        byte[] pkcs8Bytes = convertPkcs1ToPkcs8(keyBytes);
        return loadPkcs8PrivateKey(pkcs8Bytes);
    }

    private static byte[] convertPkcs1ToPkcs8(byte[] pkcs1Bytes) {
        String pkcs1Pem = Base64.getEncoder().encodeToString(pkcs1Bytes);
        StringBuilder builder = new StringBuilder();
        builder.append("-----BEGIN PRIVATE KEY-----\n");
        builder.append(pkcs1Pem);
        builder.append("\n-----END PRIVATE KEY-----\n");
        return builder.toString().getBytes();
    }

}
