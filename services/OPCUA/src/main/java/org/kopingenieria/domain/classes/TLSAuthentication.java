package org.kopingenieria.domain.classes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_authentications")
@EntityListeners(AuditingEntityListener.class)
public final class TLSAuthentication implements Serializable {

    @Serial
    private static final long serialVersionUID = 789L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de seguridad
    @Column(name = "client_auth_required")
    private Boolean clientAuthRequired;

    @Column(name = "hostname_verification")
    private Boolean hostnameVerification;

    @Column(name = "ocsp_enabled")
    private Boolean ocspEnabled;

    @Column(name = "crl_enabled")
    private Boolean crlEnabled;

    @Override
    public String toString() {
        return "TLSAuthentication{" +
                ",request=" + request +
                ",response=" + response +
                ", clientAuthRequired=" + clientAuthRequired +
                ", hostnameVerification=" + hostnameVerification +
                ", ocspEnabled=" + ocspEnabled +
                ", crlEnabled=" + crlEnabled +
                "} ";
    }
}
