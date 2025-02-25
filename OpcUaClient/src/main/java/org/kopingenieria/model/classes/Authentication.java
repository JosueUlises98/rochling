package org.kopingenieria.model.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Authentication permits TLSAuthentication,SSHAuthentication,OpcUaAuthentication  {

    protected String none;
    protected String username;
    protected String password;
    protected String certificate;

}
