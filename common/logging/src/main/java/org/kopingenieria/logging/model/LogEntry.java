package org.kopingenieria.logging.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class LogEntry {

    private String id;
    private LogEntryType type;
    private LogLevel level;
    private String className;
    private String methodName;
    private String description;
    private String batchId;
    private String correlationId;
    private String source;
    private String message;

    // Campos específicos para METHOD_ENTRY/METHOD_EXIT
    private boolean includeParams;
    private Object[] arguments;
    private Object result;

    // Campos REST
    private String httpMethod;
    private String endpoint;
    private Object requestBody;
    private Object responseBody;
    private int statusCode;
    private Map<String, String> headers;
    private Map<String, String> queryParams;

    // Campos DATABASE
    private String operation;
    private String sql;
    private Object[] sqlParams;

    // Campos PERFORMANCE
    private long executionTimeMs;
    private String metricName;
    private Map<String, Object> metrics;

    // Campos ERROR
    private Throwable error;
    private String errorMessage;
    private String stackTrace;

    // Campos SYSTEM_EVENT
    private String eventName;
    private String eventPhase;
    private Map<String, Object> eventDetails;

    // Campos comunes
    private LocalDateTime timestamp;
    private String applicationName;
    private String hostName;
    private String threadName;
    private String traceId;

    @JsonIgnore
    private int retryCount;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> additionalInfo;

}
