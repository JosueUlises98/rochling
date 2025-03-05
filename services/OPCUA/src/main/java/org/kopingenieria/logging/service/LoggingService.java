package org.kopingenieria.logging.service;

import org.kopingenieria.logging.model.LogEvent;

public interface LoggingService {
    void log(LogEvent logEvent,Object[]keys,Object[]values);
}
