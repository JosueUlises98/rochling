package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.model.enums.ssh.SshAuthenticationType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_connections")
@EntityListeners(AuditingEntityListener.class)
public class SSHAuthentication extends Authentication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de autenticación
    @Enumerated(EnumType.STRING)
    private SshAuthenticationType authenticationType;

    @Column(length = 1024)
    private String password;

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "private_key_passphrase")
    private String privateKeyPassphrase;

    @Column(name = "known_hosts_path")
    private String knownHostsPath;
}
