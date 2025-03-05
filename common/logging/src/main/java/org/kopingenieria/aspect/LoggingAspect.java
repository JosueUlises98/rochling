package org.kopingenieria.aspect;

import org.kopingenieria.service.LoggingService;

@Aspect
@Component
@Slf4j
public class LoggingAspect {

    private final LoggingService loggingService;

    public LoggingAspect(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Around("@annotation(loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint, Loggable loggable) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        MDC.put("className", className);
        MDC.put("methodName", methodName);

        long startTime = System.currentTimeMillis();

        try {
            if (loggable.includeParams()) {
                loggingService.logMethodEntry(className, methodName, joinPoint.getArgs());
            }

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            if (loggable.includeResult()) {
                loggingService.logMethodExit(className, methodName, result, executionTime);
            }

            return result;
        } catch (Exception e) {
            loggingService.logError(className, methodName, e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
