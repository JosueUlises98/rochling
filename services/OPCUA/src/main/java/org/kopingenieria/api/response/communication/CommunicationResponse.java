package org.kopingenieria.api.response.communication;

import lombok.Builder;
import lombok.Data;
import org.kopingenieria.domain.enums.communication.MessageStatusCode;

@Data
@Builder
public class CommunicationResponse {
    private String message;
    private MessageStatusCode statusCode;
    private boolean success;
    private String timestamp;
}
