package org.kopingenieria.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.kopingenieria.model.LogEntry;
import org.kopingenieria.model.LogEntryType;
import org.kopingenieria.model.LogRestCall;
import org.kopingenieria.service.LoggingService;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final LoggingService loggingService;
    private final HttpServletRequest request;

    @Around("@annotation(LogRestCall)")
    public Object logRestEndpoint(ProceedingJoinPoint joinPoint, LogRestCall logRestEndpoint) throws Throwable {
        // Implementación previa del aspecto REST
        LogEntry.LogEntryBuilder requestBuilder = LogEntry.builder()
                .type(LogEntryType.REST_REQUEST)
                .timestamp(LocalDateTime.now())
                .httpMethod(request.getMethod())
                .endpoint(request.getRequestURI());

        if (logRestEndpoint.includeHeaders()) {
            requestBuilder.headers(extractHeaders(request));
        }
        if (logRestEndpoint.includeQueryParams()) {
            requestBuilder.queryParams(extractQueryParams(request));
        }
        if (logRestEndpoint.includeBody() && joinPoint.getArgs().length > 0) {
            requestBuilder.requestBody(joinPoint.getArgs()[0]);
        }

        loggingService.log(requestBuilder.build());

        try {
            Object result = joinPoint.proceed();

            LogEntry responseLog = LogEntry.builder()
                    .type(LogEntryType.REST_RESPONSE)
                    .timestamp(LocalDateTime.now())
                    .endpoint(request.getRequestURI())
                    .responseBody(result)
                    .statusCode(200)
                    .build();

            loggingService.log(responseLog);
            return result;
        } catch (Exception e) {
            LogEntry errorLog = LogEntry.builder()
                    .type(LogEntryType.ERROR)
                    .timestamp(LocalDateTime.now())
                    .endpoint(request.getRequestURI())
                    .error(e)
                    .errorMessage(e.getMessage())
                    .stackTrace(ExceptionUtils.getStackTrace(e))
                    .statusCode(500)
                    .build();

            loggingService.log(errorLog);
            throw e;
        }
    }

    @Around("@annotation(logDatabase)")
    public Object logDatabaseOperation(ProceedingJoinPoint joinPoint, LogDatabase logDatabase) throws Throwable {
        // Implementación previa del aspecto de base de datos
        String methodName = joinPoint.getSignature().getName();

        LogEntry.LogEntryBuilder preBuilder = LogEntry.builder()
                .type(LogEntryType.DATABASE_OPERATION)
                .operation(inferDatabaseOperation(methodName))
                .timestamp(LocalDateTime.now());

        if (logDatabase.includeParams()) {
            preBuilder.sqlParams(joinPoint.getArgs());
        }

        loggingService.log(preBuilder.build());

        try {
            Object result = joinPoint.proceed();

            if (logDatabase.includeResult()) {
                LogEntry postLog = LogEntry.builder()
                        .type(LogEntryType.DATABASE_OPERATION)
                        .operation(inferDatabaseOperation(methodName))
                        .result(result)
                        .timestamp(LocalDateTime.now())
                        .build();

                loggingService.log(postLog);
            }

            return result;
        } catch (Exception e) {
            LogEntry errorLog = LogEntry.builder()
                    .type(LogEntryType.ERROR)
                    .operation(inferDatabaseOperation(methodName))
                    .error(e)
                    .timestamp(LocalDateTime.now())
                    .build();

            loggingService.log(errorLog);
            throw e;
        }
    }

    // Implementaciones previas de los demás aspectos...

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames())
                .forEach(headerName -> headers.put(headerName, request.getHeader(headerName)));
        return headers;
    }

    private Map<String, String> extractQueryParams(HttpServletRequest request) {
        return request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> String.join(",", e.getValue())
                ));
    }

    private String inferDatabaseOperation(String methodName) {
        if (methodName.startsWith("find") || methodName.startsWith("get")) return "SELECT";
        if (methodName.startsWith("save") || methodName.startsWith("insert")) return "INSERT";
        if (methodName.startsWith("update")) return "UPDATE";
        if (methodName.startsWith("delete")) return "DELETE";
        return "UNKNOWN";
    }
}
