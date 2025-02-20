package org.kopingenieria.logging.model;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class LogEvent {
    private String eventId;
    private String timestamp;
    private String className;
    private String methodName;
    private Map<String, Object> parameters;
    private String result;
    private String exception;
    private long duration;
    private boolean success;
    private Map<String, String> metadata; // Agrega metadatos opcionales
}
