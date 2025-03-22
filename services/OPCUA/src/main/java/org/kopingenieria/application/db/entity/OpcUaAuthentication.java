package org.kopingenieria.application.db.entity;

import jakarta.persistence.*;
import lombok.*;
import org.kopingenieria.api.request.OpcUaAuthenticationRequest;
import org.kopingenieria.api.response.AuthenticationResponse;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_authentications")
@EntityListeners(AuditingEntityListener.class)
public final class OpcUaAuthentication extends Authentication {

    @Serial
    private static final long serialVersionUID = 301L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "security_policy")
    private OpcUaAuthenticationRequest request;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_policy")
    private AuthenticationResponse response;

    @Enumerated(EnumType.STRING)
    @Column(name = "security_policy")
    private SecurityPolicy securityPolicy;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_security_mode")
    private MessageSecurityMode messageSecurityMode;

    @Column(name = "certificate_path")
    private String certificatePath;

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "trust_list_path")
    private String trustListPath;

    @Column(name = "issuer_list_path")
    private String issuerListPath;

    @Column(name = "revocation_list_path")
    private String revocationListPath;

    @Override
    public String toString() {
        return "OpcUaAuthentication{" +
                ",request=" + request +
                ",response=" + response +
                ", securityPolicy=" + securityPolicy +
                ", messageSecurityMode=" + messageSecurityMode +
                ", certificatePath='" + certificatePath + '\'' +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", trustListPath='" + trustListPath + '\'' +
                ", issuerListPath='" + issuerListPath + '\'' +
                ", revocationListPath='" + revocationListPath + '\'' +
                '}';
    }
}
