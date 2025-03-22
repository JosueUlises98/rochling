package org.kopingenieria.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.security.MessageSecurityMode;
import org.kopingenieria.domain.enums.security.SecurityPolicy;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationResponse {
    private String token;
    private String username;
    private boolean authenticated;
    private SecurityPolicy securityPolicy;
    private MessageSecurityMode messageSecurityMode;
    private LocalDateTime timestamp;
    private String error;
}
