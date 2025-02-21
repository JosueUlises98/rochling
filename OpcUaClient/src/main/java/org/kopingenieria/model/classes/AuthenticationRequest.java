package org.kopingenieria.model.classes;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class AuthenticationRequest implements Serializable {
    private String username;
    private String password;
    private String certificate;
    private String none;
}
