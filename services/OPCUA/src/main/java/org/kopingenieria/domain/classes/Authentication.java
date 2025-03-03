package org.kopingenieria.domain.classes;

import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.io.Serializable;

@MappedSuperclass
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public abstract sealed class Authentication implements Serializable permits TLSAuthentication,SSHAuthentication,OpcUaAuthentication  {

    //Atributos base
    protected AuthenticationRequest request;
    protected AuthenticationResponse response;
}
