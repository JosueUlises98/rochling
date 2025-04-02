package org.kopingenieria.application.validators.contract.user;

import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import java.security.cert.X509Certificate;
import java.util.List;

public interface UserEncryptionValidator {

    boolean validateSecurityPolicy(SecurityPolicy securityPolicy);

    boolean validateSecurityMode(MessageSecurityMode messageSecurityMode);

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
