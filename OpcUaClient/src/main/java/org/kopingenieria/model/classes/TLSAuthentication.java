package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_authentications")
@EntityListeners(AuditingEntityListener.class)
public final class TLSAuthentication extends Authentication{

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
                "none='" + none + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", certificate='" + certificate + '\'' +
                ", clientAuthRequired=" + clientAuthRequired +
                ", hostnameVerification=" + hostnameVerification +
                ", ocspEnabled=" + ocspEnabled +
                ", crlEnabled=" + crlEnabled +
                "} ";
    }
}
