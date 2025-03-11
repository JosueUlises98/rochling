package org.kopingenieria.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kopingenieria.exception.LogWriteFailureEvent;
import org.kopingenieria.model.LogEntry;
import org.kopingenieria.processor.BulkLogProcessor;
import org.kopingenieria.util.LogEntryEnricher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoggingService {

    private final ElasticSearchLogWriter elasticsearchWriter;
    private final FileLogWriter fileWriter;
    private final LogEntryEnricher logEntryEnricher;
    private final BulkLogProcessor bulkLogProcessor;
    private final ApplicationEventPublisher eventPublisher;

    public void log(LogEntry logEntry) {
        try {
            LogEntry enrichedEntry = logEntryEnricher.enrich(logEntry);
            bulkLogProcessor.addLogEntry(enrichedEntry);
        } catch (Exception e) {
            handleLoggingError(logEntry, e);
        }
    }

    public void logImmediate(LogEntry logEntry) {
        try {
            LogEntry enrichedEntry = logEntryEnricher.enrich(logEntry);
            elasticsearchWriter.write(enrichedEntry);
        } catch (Exception e) {
            handleLoggingError(logEntry, e);
        }
    }

    private void handleLoggingError(LogEntry logEntry, Exception e) {
        log.warn("Error writing to Elasticsearch, falling back to file storage", e);
        try {
            fileWriter.write(logEntry);
            eventPublisher.publishEvent(new LogWriteFailureEvent(logEntry.getErrorMessage(), e));
        } catch (Exception ex) {
            log.error("Critical: Failed to write log entry to fallback storage", ex);
        }
    }
}
