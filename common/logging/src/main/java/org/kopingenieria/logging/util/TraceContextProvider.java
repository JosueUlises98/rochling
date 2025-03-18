package org.kopingenieria.logging.util;

import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
public class TraceContextProvider {

    private final ThreadLocal<String> traceIdHolder = new ThreadLocal<>();

    public String getCurrentTraceId() {
        String traceId = traceIdHolder.get();
        if (traceId == null) {
            traceId = generateTraceId();
            traceIdHolder.set(traceId);
        }
        return traceId;
    }

    public void setTraceId(String traceId) {
        traceIdHolder.set(traceId);
    }

    public void clearTraceId() {
        traceIdHolder.remove();
    }

    private String generateTraceId() {
        return UUID.randomUUID().toString();
    }
}
