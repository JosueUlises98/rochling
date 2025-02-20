package org.kopingenieria.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
public class AuthenticationResponse implements Serializable {
    private String token;
    private String username;
    private boolean authenticated;
    private LocalDateTime timestamp;
    private String error;
}
