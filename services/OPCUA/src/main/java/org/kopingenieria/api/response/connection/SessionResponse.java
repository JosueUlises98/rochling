package org.kopingenieria.api.response.connection;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.SessionStatus;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionResponse {
    private Long id;
    private String sessionId;
    private String sessionName;
    private String serverUri;
    private SessionStatus status;
    private LocalDateTime creationTime;
    private LocalDateTime expirationTime;
    private LocalDateTime lastActivity;
    private String error;
}
