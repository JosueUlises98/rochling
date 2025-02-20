package org.kopingenieria.logging.service;

import org.kopingenieria.logging.model.LogEvent;

public interface NotificacionService {
    void sendAlert(LogEvent logEvent);
}
