package org.kopingenieria.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.kopingenieria.model.AuditEvent;
import org.kopingenieria.model.AuditableOperation;
import org.kopingenieria.repository.AuditEventRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditEventRepository auditEventRepository;
    private final AuditEventPublisher eventPublisher;
    private final SecurityContextHolder securityContext;
    private final ObjectMapper objectMapper;

    @Around("@annotation(auditableOperation)")
    public Object auditOperation(ProceedingJoinPoint joinPoint,
                                 AuditableOperation auditableOperation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();

        AuditEventBuilder builder = AuditEvent.builder()
                .eventType(auditableOperation.type().name())
                .component(joinPoint.getTarget().getClass().getSimpleName())
                .action(methodName)
                .userId(securityContext.getCurrentUser().getId())
                .timestamp(LocalDateTime.now())
                .traceId(MDC.get("traceId"))
                .ipAddress(RequestContextHolder.getCurrentRequest().getRemoteAddr());

        try {
            // Capturar parámetros si es necesario
            if (auditableOperation.includeParams()) {
                String params = captureMethodParameters(joinPoint);
                builder.details("Parameters: " + params);
            }

            // Ejecutar método
            Object result = joinPoint.proceed();

            // Capturar resultado si es necesario
            if (auditableOperation.includeResult() && result != null) {
                builder.outcome("SUCCESS")
                        .details(objectMapper.writeValueAsString(result));
            }

            return result;

        } catch (Exception e) {
            builder.outcome("ERROR")
                    .details("Error: " + e.getMessage());
            throw e;
        } finally {
            builder.executionTime(System.currentTimeMillis() - startTime);
            AuditEvent auditEvent = builder.build();

            // Persistir y publicar evento
            auditEventRepository.save(auditEvent);
            eventPublisher.publishAuditEvent(auditEvent);
        }
    }
}
