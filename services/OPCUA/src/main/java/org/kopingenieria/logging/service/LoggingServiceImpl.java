package org.kopingenieria.logging.service;

import org.kopingenieria.logging.model.LogEvent;
import org.kopingenieria.logging.util.JsonUtils;
import org.kopingenieria.logging.util.MetricsRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class LoggingServiceImpl implements LoggingService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingServiceImpl.class);

    private final MetricsRegistry metricsRegistry;

    private final NotificationServiceImpl notificationService;

    public LoggingServiceImpl(MetricsRegistry metricsRegistry, NotificationServiceImpl notificationService) {
        this.metricsRegistry = metricsRegistry;
        this.notificationService = notificationService;
    }

    public void log(LogEvent event) {

        // Agregar metadatos adicionales (ej. usuario, entorno, etc.)
        Map<String, String> metadata = new HashMap<>();
        metadata.put("application", "opcua-client");
        metadata.put("environment", "production");
        metadata.put("user", "admin");
        metadata.put("version", "1.0.0");
        metadata.put("timestamp", event.getTimestamp());
        event.setMetadata(metadata);

        //Registro a consola
        logToConsole(event);

        // Registro a Elasticsearch (asíncrono para no bloquear el flujo principal)
        logToElasticsearch(event);

        // Métricas
        metricsRegistry.incrementRequestCounter(event.getClassName(), event.getMethodName(), event.isSuccess());
        metricsRegistry.recordRequestDuration(event.getClassName(), event.getMethodName(), event.getDuration());

        if (!event.isSuccess()) {
            notificationService.sendAlert(event);
        }
    }

    private void logToConsole(LogEvent event) {
        logger.info("Log Event: {}", JsonUtils.toJson(event));
    }

    private void logToElasticsearch(LogEvent event) {
        logger.info("Enviando evento a Elasticsearch: {}", event.getEventId());
    }

}
