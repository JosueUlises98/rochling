package org.kopingenieria.model.classes;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.kopingenieria.model.enums.ssh.SshVersion;

import java.util.List;

@Entity
@Getter
@Setter
public class SSHConfiguration extends Configuration<SSHConfiguration> {
    // Atributos de autenticación SSH
    private String username;
    private String privateKeyPath;
    private String privateKeyPassphrase;
    private String knownHostsPath;

    // Atributos de sesión SSH
    private Integer sessionTimeout;
    private String preferredAuthentications;
    private boolean strictHostKeyChecking;

    @ElementCollection
    private List<String> allowedAlgorithms;

    private boolean compressionEnabled;
    private Integer compressionLevel;

    @Enumerated(EnumType.STRING)
    private SshVersion version; // SSH1/SSH2
}
