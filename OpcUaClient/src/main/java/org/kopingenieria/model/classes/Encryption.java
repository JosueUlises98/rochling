package org.kopingenieria.model.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Encryption permits TLSEncryption,SSHEncryption,OpcUaEncryption  {
    protected int keyLength;
    protected String algorithmName;
    protected String protocolVersion;
}
