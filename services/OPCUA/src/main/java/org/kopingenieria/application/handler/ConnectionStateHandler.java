package org.kopingenieria.application.handler;

import org.kopingenieria.application.monitoring.health.HealthCheck;
import org.kopingenieria.exception.exceptions.InvalidConnectionStateTransitionException;

@FunctionalInterface
public interface ConnectionStateHandler {
    void handle(HealthCheck healthCheck) throws InvalidConnectionStateTransitionException;
}
