package org.kopingenieria.service;

@Service
@Slf4j
public class LoggingService {

    @Autowired
    private LoggingProperties properties;

    public void logMethodEntry(String className, String methodName, Object[] args) {
        if (properties.isEnabled()) {
            log.info("Entering method [{}.{}] with parameters: {}",
                    className, methodName, LoggingUtils.sanitizeParams(args));
        }
    }

    public void logMethodExit(String className, String methodName, Object result, long executionTime) {
        if (properties.isEnabled()) {
            log.info("Exiting method [{}.{}] with result: {} (took {} ms)",
                    className, methodName, LoggingUtils.sanitizeParams(result), executionTime);
        }
    }

    public void logRestRequest(HttpServletRequest request) {
        if (properties.isEnabled() && properties.isIncludePayload()) {
            log.info("REST Request - Method: [{}] Path: [{}]",
                    request.getMethod(), request.getRequestURI());

            if (properties.isIncludeHeaders()) {
                logHeaders(request);
            }
        }
    }

    public void logRestResponse(Object response, long executionTime) {
        if (properties.isEnabled() && properties.isIncludePayload()) {
            log.info("REST Response (took {} ms): {}",
                    executionTime, LoggingUtils.sanitizeParams(response));
        }
    }

    public void logError(String className, String methodName, Exception e) {
        log.error("Error in [{}.{}]: {}", className, methodName, e.getMessage(), e);
    }
}
