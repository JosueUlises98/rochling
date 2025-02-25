package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_authentications")
@EntityListeners(AuditingEntityListener.class)
public class TLSAuthentication extends Authentication{

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


}
