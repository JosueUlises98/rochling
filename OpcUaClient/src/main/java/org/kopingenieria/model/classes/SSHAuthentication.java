package org.kopingenieria.model.classes;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.kopingenieria.model.enums.client.ssh.SshAuthenticationType;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.io.Serial;


@EqualsAndHashCode(callSuper = true)
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "ssh_authentications")
@EntityListeners(AuditingEntityListener.class)
public final class SSHAuthentication extends Authentication {

    @Serial
    private static final long serialVersionUID = 179L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos de autenticación
    @Enumerated(EnumType.STRING)
    private SshAuthenticationType authenticationType;

    @Column(name = "private_key_path")
    private String privateKeyPath;

    @Column(name = "private_key_passphrase")
    private String privateKeyPassphrase;

    @Column(name = "known_hosts_path")
    private String knownHostsPath;

    @Override
    public String toString() {
        return "SSHAuthentication{" +
                ",request=" + request +
                ",response=" + response +
                ", authenticationType=" + authenticationType +
                ", privateKeyPath='" + privateKeyPath + '\'' +
                ", privateKeyPassphrase='" + privateKeyPassphrase + '\'' +
                ", knownHostsPath='" + knownHostsPath + '\'' +
                "} ";
    }
}
