package org.kopingenieria.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.kopingenieria.model.AuditEntryType;
import org.kopingenieria.model.annotation.Auditable;
import org.kopingenieria.model.dto.AuditEventDTO;
import org.kopingenieria.model.entity.AuditEvent;
import org.kopingenieria.service.AuditService;
import org.kopingenieria.util.AuditUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.env.Environment;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import java.lang.reflect.Method;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;


@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditService auditService;
    private static final String SENSITIVE_DATA_PLACEHOLDER = "[DATOS SENSIBLES]";

    @Pointcut("@annotation(org.kopingenieria.annotation.Auditable)")
    public void auditableMethod() {}

    @Around("auditableMethod()")
    public Object auditOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        Object result = null;
        Throwable error = null;

        try {
            result = joinPoint.proceed();
            return result;
        } catch (Throwable t) {
            error = t;
            throw t;
        } finally {
            registerAuditEvent(joinPoint, result, error, System.currentTimeMillis() - startTime);
        }
    }

    private void registerAuditEvent(ProceedingJoinPoint joinPoint, Object result,
                                    Throwable error, long executionTime) {
        try {
            Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
            Auditable auditable = method.getAnnotation(Auditable.class);

            AuditEventDTO event = AuditEventDTO.builder()
                    .eventtype(error != null ? "ERROR" : "SUCCESS")
                    .component(joinPoint.getTarget().getClass().getSimpleName())
                    .action(method.getName())
                    .userid(getCurrentUser())
                    .timestamp(LocalDateTime.now())
                    .details(buildDetails(joinPoint, auditable, result, error))
                    .outcome(error != null ? "FAILURE" : "SUCCESS")
                    .traceid(UUID.randomUUID().toString())
                    .executiontime(executionTime)
                    .build();

            auditService.registerAsyncEvent(event);
        } catch (Exception ignored) {}
    }

    private String getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .orElse("SYSTEM");
    }

    private String buildDetails(ProceedingJoinPoint joinPoint, Auditable auditable,
                                Object result, Throwable error) {
        StringBuilder details = new StringBuilder()
                .append("Class: ").append(joinPoint.getTarget().getClass().getName())
                .append("\nMethod: ").append(joinPoint.getSignature().getName());

        if (auditable.includeParams() && joinPoint.getArgs().length > 0) {
            details.append("\nParameters: ").append(serializeParameters(joinPoint.getArgs()));
        }

        if (error != null) {
            details.append("\nError: ").append(serializeError(error));
        } else if (auditable.includeResult()) {
            details.append("\nResult: ").append(serializeResult(result));
        }

        return details.toString();
    }

    private String serializeParameters(Object[] args) {
        if (args == null || args.length == 0) return "none";

        StringBuilder params = new StringBuilder();
        for (Object arg : args) {
            if (arg == null) {
                params.append("null, ");
            } else if (isSensitiveType(arg)) {
                params.append(SENSITIVE_DATA_PLACEHOLDER).append(", ");
            } else {
                params.append(arg.toString()).append(", ");
            }
        }

        String result = params.toString();
        return result.endsWith(", ") ? result.substring(0, result.length() - 2) : result;
    }

    private String serializeResult(Object result) {
        if (result == null) return "void";
        return isSensitiveType(result) ? SENSITIVE_DATA_PLACEHOLDER : result.toString();
    }

    private String serializeError(Throwable error) {
        return String.format("%s: %s", error.getClass().getSimpleName(), error.getMessage());
    }

    private boolean isSensitiveType(Object obj) {
        if (obj == null) return false;

        String className = obj.getClass().getName().toLowerCase();
        return className.contains("password") ||
                className.contains("credential") ||
                className.contains("secret") ||
                className.contains("token") ||
                className.contains("key");
    }

    private boolean isSensitiveField(String fieldName) {
        if (fieldName == null) return false;

        String lowerFieldName = fieldName.toLowerCase();
        return lowerFieldName.contains("password") ||
                lowerFieldName.contains("credential") ||
                lowerFieldName.contains("secret") ||
                lowerFieldName.contains("token") ||
                lowerFieldName.contains("key");
    }
}
