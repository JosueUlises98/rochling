package org.kopingenieria.event;

import lombok.Getter;
import org.kopingenieria.model.LogEntry;
import org.springframework.context.ApplicationEvent;

@Getter
public class LogWriterFailureEvent extends ApplicationEvent {

    private final LogEntry logEntry;
    private final Exception exception;

    public LogWriterFailureEvent(LogEntry logEntry, Exception exception) {
        super(logEntry);
        this.logEntry = logEntry;
        this.exception = exception;
    }
}
