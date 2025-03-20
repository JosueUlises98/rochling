package org.kopingenieria.api.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AuthenticationResponse {
    private String token;
    private String username;
    private boolean authenticated;
    private LocalDateTime timestamp;
    private String error;
}
