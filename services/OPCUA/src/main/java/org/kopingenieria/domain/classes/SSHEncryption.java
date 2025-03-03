package org.kopingenieria.domain.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_encryptions")
@EntityListeners(AuditingEntityListener.class)
public final class SSHEncryption extends Encryption{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Configuración de seguridad
    @Column(name = "strict_host_checking")
    private Boolean strictHostKeyChecking;

    @ElementCollection
    @CollectionTable(
            name = "ssh_allowed_algorithms",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    @Column(name = "allowed_algorithm")
    private Set<String> allowedAlgorithms;

    @Override
    public String toString() {
        return "SSHEncryption {" +
                ", keyLength='" + keyLength + '\'' +
                ", algorithmName='" + algorithmName + '\'' +
                ", protocolVersion='" + protocolVersion + '\'' +
                ", strictHostKeyChecking=" + strictHostKeyChecking +
                ", allowedAlgorithms=" + allowedAlgorithms +
                '}';
    }
}
