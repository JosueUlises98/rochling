package org.kopingenieria.model.classes;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.JoinColumn;

import java.util.Set;

public class SSHEncryption extends Encryption{

    // Configuración de seguridad
    @Column(name = "strict_host_checking")
    private Boolean strictHostKeyChecking;

    @ElementCollection
    @CollectionTable(
            name = "ssh_allowed_algorithms",
            joinColumns = @JoinColumn(name = "connection_id")
    )
    private Set<String> allowedAlgorithms;
}
