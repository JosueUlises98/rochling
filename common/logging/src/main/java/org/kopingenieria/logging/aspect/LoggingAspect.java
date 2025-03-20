package org.kopingenieria.logging.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.kopingenieria.logging.model.*;
import org.kopingenieria.logging.service.LoggingService;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
                    .statusCode(500)
                    .build();

            loggingService.log(errorLog);
            throw e;
        }
    }

    @Around("@annotation(LogDataBase)")
    public Object logDatabaseOperation(ProceedingJoinPoint joinPoint, LogDatabase logDatabase) throws Throwable {
        // Implementación previa del aspecto de base de datos
        String methodName = joinPoint.getSignature().getName();

        LogEntry.LogEntryBuilder preBuilder = LogEntry.builder()
                .type(LogEntryType.DATABASE_OPERATION)
                .operation(LogEntryType.DATABASE_OPERATION.name())
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

    @Around("@annotation(LogMethod)")
    public Object logMethod(ProceedingJoinPoint joinPoint, LogMethod logMethod) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // Log de entrada al método
        LogEntry methodEntryLog = LogEntry.builder()
                .type(LogEntryType.METHOD_ENTRY)
                .timestamp(LocalDateTime.now())
                .methodName(methodName)
                .className(className)
                .arguments(logMethod.includeArgs() ? joinPoint.getArgs() : null)
                .build();

        loggingService.log(methodEntryLog);

        try {
            Object result = joinPoint.proceed();

            // Log de salida del método
            LogEntry methodExitLog = LogEntry.builder()
                    .type(LogEntryType.METHOD_EXIT)
                    .timestamp(LocalDateTime.now())
                    .methodName(methodName)
                    .className(className)
                    .result(logMethod.includeResult() ? result : null)
                    .build();

            loggingService.log(methodExitLog);

            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Around("@annotation(LogPerformance)")
    public Object logPerformance(ProceedingJoinPoint joinPoint, LogPerfomance logPerformance) throws Throwable {

        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            LogEntry performanceLog = LogEntry.builder()
                    .type(LogEntryType.PERFORMANCE_METRIC)
                    .timestamp(LocalDateTime.now())
                    .methodName(methodName)
                    .className(className)
                    .executionTimeMs(executionTime)
                    .build();

            loggingService.log(performanceLog);

            return result;
        } catch (Exception e) {
            throw e;
        }
    }

    @Around("@annotation(LogSystemEvent)")
    public Object logSystemEvent(ProceedingJoinPoint joinPoint, LogSystemEvent logSystemEvent) throws Throwable {

        String eventName = logSystemEvent.event();

        LogEntry preEventLog = LogEntry.builder()
                .type(LogEntryType.SYSTEM_EVENT)
                .timestamp(LocalDateTime.now())
                .eventName(eventName)
                .eventPhase("START")
                .build();

        loggingService.log(preEventLog);

        try {
            Object result = joinPoint.proceed();

            LogEntry postEventLog = LogEntry.builder()
                    .type(LogEntryType.SYSTEM_EVENT)
                    .timestamp(LocalDateTime.now())
                    .eventName(eventName)
                    .eventPhase("COMPLETE")
                    .result(result)
                    .build();

            loggingService.log(postEventLog);

            return result;
        } catch (Exception e) {
            LogEntry errorEventLog = LogEntry.builder()
                    .type(LogEntryType.SYSTEM_EVENT)
                    .timestamp(LocalDateTime.now())
                    .eventName(eventName)
                    .eventPhase("ERROR")
                    .error(e)
                    .build();

            loggingService.log(errorEventLog);
            throw e;
        }
    }

    @AfterThrowing(pointcut = "@annotation(LogException)", throwing = "ex")
    public void logError(JoinPoint joinPoint, Exception ex) {
        LogEntry errorLog = LogEntry.builder()
                .type(LogEntryType.ERROR)
                .timestamp(LocalDateTime.now())
                .className(joinPoint.getTarget().getClass().getSimpleName())
                .methodName(joinPoint.getSignature().getName())
                .error(ex)
                .errorMessage(ex.getMessage())
                .stackTrace(Arrays.toString(ex.getStackTrace()))
                .build();
        loggingService.log(errorLog);
    }

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
        methodName = methodName.toLowerCase();
        return switch (methodName.split("_")[0]) {
            case "get", "find", "read" -> "SELECT";
            case "save", "insert" -> "INSERT";
            case "update", "modify" -> "UPDATE";
            case "delete", "remove" -> "DELETE";
            default -> "UNKNOWN";
        };
    }

}
