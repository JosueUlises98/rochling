package org.kopingenieria.aspect;

@Aspect
@Component
@Slf4j
public class RestControllerLoggingAspect {

    private final LoggingService loggingService;

    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logRestCall(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request =
                ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String path = request.getRequestURI();
        String method = request.getMethod();

        MDC.put("path", path);
        MDC.put("httpMethod", method);

        long startTime = System.currentTimeMillis();

        try {
            loggingService.logRestRequest(request);

            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;
            loggingService.logRestResponse(result, executionTime);

            return result;
        } catch (Exception e) {
            loggingService.logRestError(e);
            throw e;
        } finally {
            MDC.clear();
        }
    }
}
