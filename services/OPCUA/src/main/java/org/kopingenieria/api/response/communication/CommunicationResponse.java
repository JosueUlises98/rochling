package org.kopingenieria.api.response.communication;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.MessageStatusCode;
import java.time.LocalDateTime;

@Data
@Builder
public class CommunicationResponse {
    private String message;
    private MessageStatusCode statusCode;
    private boolean success;
    private LocalDateTime timestamp;
    private String serverEndpoint;
    private int statusCodeValue;
    private String diagnosticInfo;
    private Object responseData;
}
