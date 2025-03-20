package org.kopingenieria.application.db.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;
import java.util.Arrays;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_encryptions")
@EntityListeners(AuditingEntityListener.class)
public final class OpcUaEncryption extends Encryption {

    @Serial
    private final static long serialVersionUID = 401L;

    @Column(name = "security_policy")
    private String securityPolicy;// OPC UA Security Policy, e.g., "Basic256Sha256"
    @Column(name = "message_security_mode")
    private String messageSecurityMode; // Security mode, e.g., "Sign" or "SignAndEncrypt"
    @Column(name = "client_certificate")
    private byte[] clientCertificate; // Client authentication certificate as a byte array
    @Column(name = "private_key")
    private byte[] privateKey; // Private key associated with the client certificate
    @Column(name = "trusted_certificates")
    private List<byte[]> trustedCertificates; // List of trusted certificates as byte arrays

    @Override
    public String toString() {
        return "TLSEncryption{" +
                ", keyLength='" + keyLength + '\'' +
                ",algorithmName='" + algorithmName + '\'' +
                ",protocolVersion='" + protocolVersion + '\'' +
                ",securityPolicy=" + securityPolicy +
                ",messageSecurityMode=" + messageSecurityMode +
                ",clientCertificate=" + Arrays.toString(clientCertificate) +
                ",privateKey=" + Arrays.toString(privateKey) +
                ",trustedCertificates=" + trustedCertificates +
                '}';
    }
}
