package org.kopingenieria.util.loader;

import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.springframework.stereotype.Component;
import java.io.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

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
}
