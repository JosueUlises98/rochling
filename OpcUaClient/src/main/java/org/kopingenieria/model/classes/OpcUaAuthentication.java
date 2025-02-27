package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.model.enums.opcua.communication.MessageSecurityMode;
import org.kopingenieria.model.enums.opcua.communication.SecurityPolicy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "opcua_authentications")
@EntityListeners(AuditingEntityListener.class)
public final class OpcUaAuthentication extends Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de seguridad
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
                "none='" + none + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", certificate='" + certificate + '\'' +
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
