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
public abstract sealed class Authentication implements Serializable permits OpcUaAuthentication {

    @Serial
    private static final long serialVersionUID = 300L;

    // Atributos comunes base
    protected String username;
    protected String password;

}
