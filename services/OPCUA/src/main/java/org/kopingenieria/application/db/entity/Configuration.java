package org.kopingenieria.application.db.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;


@SuperBuilder
@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
public abstract sealed class Configuration implements Serializable permits OpcUaConfiguration {

    @Serial
    private static final long serialVersionUID = 100;

    // Atributos comunes base
    protected String name;
    protected String description;
    protected boolean enabled;

}
