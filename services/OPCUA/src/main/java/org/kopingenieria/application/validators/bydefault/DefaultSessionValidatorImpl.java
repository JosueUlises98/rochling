package org.kopingenieria.application.validators.bydefault;



import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.eclipse.milo.opcua.sdk.client.api.UaSession;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.domain.model.user.UserSessionConfiguration;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class DefaultSessionValidatorImpl implements DefaultSessionValidator {

    @Autowired
    private UserSessionConfiguration sessionConfig;

    private final double DEFAULT_SESSION_TIMEOUT; // 1 hora en milisegundos
    private final StringBuilder validationResult;

    public DefaultSessionValidatorImpl() {
        this.validationResult = new StringBuilder();
        UserSessionConfiguration build = UserSessionConfiguration.builder().build();
        DEFAULT_SESSION_TIMEOUT = build.getTimeout().getDuration();
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion",description = "Validacion de sesion opcua")
    @LogSystemEvent(description = "Validacion de sesion opcua", event = "Validacion de sesion",level = LogLevel.DEBUG)
    public boolean validateSession(UaClient client) {
        if (client == null) {
            logValidationError("Cliente OPC UA nulo");
            return false;
        }

        try {
            CompletableFuture<Boolean> sessionValid = client.getSession().thenCompose(session -> {
                if (session == null) {
                    return CompletableFuture.completedFuture(false);
                }
                NodeId sessionId = session.getSessionId();
                return CompletableFuture.completedFuture(!NodeId.NULL_VALUE.equals(sessionId));
            });

            boolean isValid = sessionValid.get(5, TimeUnit.SECONDS);
            if (!isValid) {
                logValidationError("Sesión no válida o no establecida");
                return false;
            }

            logValidationSuccess("Sesión validada correctamente");
            return true;
        } catch (Exception e) {
            logValidationError("Error al validar sesión: " + e.getMessage());
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de token de sesion",description = "Validacion de token de sesion opcua")
    @LogSystemEvent(description = "Validacion token de sesion opcua", event = "Validacion de token de sesion",level = LogLevel.DEBUG)
    public boolean validateSessionToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            logValidationError("Token de sesión inválido");
            return false;
        }
        // En Milo, el token normalmente es un identificador único
        NodeId.parse(token);
        logValidationSuccess("Token de sesión válido");
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion activa",description = "Validacion de sesion activa opcua")
    @LogSystemEvent(description = "Validacion de sesion activa opcua", event = "Validacion de sesion activa",level = LogLevel.DEBUG)
    public boolean isSessionActive(UserSessionConfiguration user) {
        if (user == null) {
            logValidationError("Configuración de usuario nula");
            return false;
        }
        return !user.getSessionStatus().isExpired() & !isSessionExpired(user);
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion expirada",description = "Validacion de sesion expirada opcua")
    @LogSystemEvent(description = "Validacion de sesion expirada opcua", event = "Validacion de sesion expirada",level = LogLevel.DEBUG)
    public boolean isSessionExpired(UserSessionConfiguration user) {
        if (user == null || user.getSessionStatus().isExpired()) {
            return true;
        }

        try {
            LocalDateTime lastActivity = user.getLastActivity();
            double elapsed = ChronoUnit.MILLIS.between(lastActivity, LocalDateTime.now());

            boolean expired = elapsed > user.getTimeout().toMilliseconds();

            if (expired) {
                logValidationError("Sesión expirada");
                return true;
            }

            logValidationSuccess("Sesión vigente");
            return false;
        } catch (Exception e) {
            logValidationError("Error al verificar expiración: " + e.getMessage());
            return true;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion activa",description = "Validacion de sesion activa opcua")
    @LogSystemEvent(description = "Validacion de sesion activa opcua", event = "Validacion de sesion activa",level = LogLevel.DEBUG)
    public boolean validateSessionTimeout(UaClient client) {
        try {
            Double timeout = client.getSession()
                    .thenApply(UaSession::getSessionTimeout)
                    .get(5, TimeUnit.SECONDS);

            if (timeout == null || timeout <= 0) {
                logValidationError("Timeout de sesión inválido");
                return false;
            }

            if (timeout > DEFAULT_SESSION_TIMEOUT) {
                logValidationError("Timeout de sesión excede el límite permitido");
                return false;
            }

            logValidationSuccess("Timeout de sesión válido: " + timeout + "ms");
            return true;
        } catch (Exception e) {
            logValidationError("Error al validar timeout de sesión: " + e.getMessage());
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del resultado final",description = "Validacion del resultado final opcua")
    @LogSystemEvent(description = "Validacion del resultado final opcua", event = "Validacion del resultado final opcua",level = LogLevel.DEBUG)
    public String getValidationResult() {
        return validationResult.toString();
    }

    private void logValidationError(String message) {
        validationResult.append("Error: ").append(message).append("\n");
    }

    private void logValidationSuccess(String message) {
        validationResult.append("Éxito: ").append(message).append("\n");
    }
}
