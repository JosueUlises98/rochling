package org.kopingenieria.logging.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.kopingenieria.logging.model.LogEvent;
import org.kopingenieria.logging.service.LoggingService;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Aspect
@Component
public class LoggingAspect {

    private final LoggingService loggingService;

    public LoggingAspect(LoggingService loggingService) {
        this.loggingService = loggingService;
    }

    @Around("execution(* org.kopingenieria..*.*(..))") // Punto de corte para los métodos a interceptar
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        LogEvent.LogEventBuilder logEventBuilder = LogEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(Instant.now().toString())
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameters(getParameters(method, joinPoint.getArgs()));

        Object result;

        try {
            result = joinPoint.proceed();
            logEventBuilder.result(result != null ? result.toString() : null);
            logEventBuilder.success(true);
            return result;
        } catch (Throwable ex) {
            logEventBuilder.exception(ex.getMessage());
            logEventBuilder.success(false);
            throw ex;
        } finally {
            stopWatch.stop();
            logEventBuilder.duration(stopWatch.getTotalTimeMillis());
            loggingService.log(logEventBuilder.build());
        }
    }

    private Map<String, Object> getParameters(Method method, Object[] args) {
        Map<String, Object> parameters = new HashMap<>();
        String[] parameterNames = Arrays.stream(method.getParameters()).map(Parameter::getName).toArray(String[]::new);
        for (int i = 0; i < parameterNames.length; i++) {
            parameters.put(parameterNames[i], args[i]);
        }
        return parameters;
    }
}
