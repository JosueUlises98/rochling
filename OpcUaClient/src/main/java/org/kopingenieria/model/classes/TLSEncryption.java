package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Set;
@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_encryptions")
@EntityListeners(AuditingEntityListener.class)
public final class TLSEncryption extends Encryption{
    // Configuración de cifrado
    @ElementCollection
    @CollectionTable(
            name = "tls_enabled_protocols",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private Set<String> enabledProtocols;

    @ElementCollection
    @CollectionTable(
            name = "tls_enabled_cipher_suites",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private Set<String> enabledCipherSuites;
}
