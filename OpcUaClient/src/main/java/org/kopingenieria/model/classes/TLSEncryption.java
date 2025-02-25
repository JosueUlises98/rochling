package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Set;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "tls_encryptions")
@EntityListeners(AuditingEntityListener.class)
public final class TLSEncryption extends Encryption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de cifrado
    @ElementCollection
    @CollectionTable(
            name = "tls_enabled_protocols",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    @Column(name = "enabled_protocol")
    private Set<String> enabledProtocols;

    @ElementCollection
    @CollectionTable(
            name = "tls_enabled_cipher_suites",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    @Column(name = "enabled_cipher_suite")
    private Set<String> enabledCipherSuites;

    @Override
    public String toString() {
        return "TLSEncryption{" +
                ", keyLength='" + keyLength + '\'' +
                ",algorithName='" + algorithmName + '\'' +
                ",protocolVersion='" + protocolVersion + '\'' +
                ", enabledProtocols=" + enabledProtocols +
                ", enabledCipherSuites=" + enabledCipherSuites +
                '}';
    }
}
