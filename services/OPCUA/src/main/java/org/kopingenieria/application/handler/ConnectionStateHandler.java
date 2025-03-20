package org.kopingenieria.application.handler;

import org.kopingenieria.application.monitoring.health.HealthCheck;

@FunctionalInterface
public interface ConnectionStateHandler {
    void handle(HealthCheck healthCheck);
}
