package org.kopingenieria.services;

import org.kopingenieria.domain.classes.OpcUaSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private final Map<String, OpcUaSession> activeSessions = new ConcurrentHashMap<>();

    public void registerSession(OpcUaSession session) {
        activeSessions.put(session.getSessionId(), session);
        log.info("Sesión registrada: {}", session.getSessionId());
    }

    public Optional<OpcUaSession> getSession(String sessionId) {
        return Optional.ofNullable(activeSessions.get(sessionId));
    }

    public void removeSession(String sessionId) {
        activeSessions.remove(sessionId);
        log.info("Sesión eliminada: {}", sessionId);
    }

    public List<OpcUaSession> getAllActiveSessions() {
        return new ArrayList<>(activeSessions.values());
    }


}
