package org.kopingenieria.logging.util;

import lombok.AllArgsConstructor;
import org.kopingenieria.logging.model.LogEntry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@AllArgsConstructor
public class LogEntryEnricher {

    private final TraceContextProvider traceContextProvider;

    @Value("${spring.application.name}")
    private String applicationName;

    public LogEntry enrich(LogEntry logEntry) {
        String hostName = getHostName();
        String traceId = traceContextProvider.getCurrentTraceId();

        return LogEntry.builder()
                .id(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .applicationName(applicationName)
                .hostName(hostName)
                .threadName(Thread.currentThread().getName())
                .traceId(traceId)
                // Copiar todos los demás campos del logEntry original
                .build();
    }

    private String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown-host";
        }
    }
}
