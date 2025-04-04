package org.kopingenieria.application.validators.bydefault;

import java.security.cert.X509Certificate;
import java.util.List;

public interface DefaultEncryptionValidator {

    boolean validateSecurityPolicy(String securityPolicy);

    boolean validateSecurityMode(String messageSecurityMode);

    boolean validateClientCertificate(byte[] clientCertificate);

    boolean validatePrivateKey(byte[] privateKey);

    boolean validateTrustedCertificates(List<byte[]> trustedCertificates);

    boolean validateAlgorithmParameters(Integer keyLength,
                                        String algorithmName,
                                        String protocolVersion);

    boolean validateCertificateChain(List<X509Certificate> certificateChain);

    boolean validateCompleteConfiguration();

    String getValidationResult();

    boolean checkCertificateRevocation(X509Certificate certificate);
}
