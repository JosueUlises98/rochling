package org.kopingenieria.api.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthenticationRequest {
    private String username;
    private String password;
    private String certificate;
    private String none;
}
