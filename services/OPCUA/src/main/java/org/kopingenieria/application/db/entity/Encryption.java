package org.kopingenieria.application.db.entity;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Encryption implements Serializable permits OpcUaEncryption {

    @Serial
    private static final long serialVersionUID = 400;

    //Atributos base
    protected int keyLength;
    protected String algorithmName;
    protected String protocolVersion;
}
