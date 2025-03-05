package org.kopingenieria.domain.classes;

@FunctionalInterface
public interface ConnectionStateHandler {
    void handle(HealthCheck healthCheck);
}
