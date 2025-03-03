package org.kopingenieria.domain.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;


@SuperBuilder
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public abstract sealed class Configuration implements Serializable permits TCPConfiguration,TLSConfiguration,SSHConfiguration,OpcUaConfiguration {

    // Atributos comunes base
    protected String name;
    protected String description;
    protected boolean enabled;

}
