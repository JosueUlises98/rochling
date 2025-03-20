package org.kopingenieria.api.validator;

import lombok.RequiredArgsConstructor;
import org.kopingenieria.api.request.OpcUaAuthenticationRequest;
import org.kopingenieria.api.request.OpcUaConnectionRequest;
import org.kopingenieria.api.request.OpcUaSessionRequest;
import org.kopingenieria.api.request.SubscriptionRequest;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.exception.ValidationRequestException;
import org.kopingenieria.logging.model.LogException;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogMethod;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private static final int MIN_PORT = 1;
    private static final int MAX_PORT = 65535;
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(opc.tcp|http|https)://[\\w.-]+(:\\d{1,5})?(/[\\w.-]*)*$"
    );
    private static final Set<String> VALID_CERTIFICATE_EXTENSIONS = Set.of(".pem", ".der", ".crt");
    private static final Set<String> VALID_KEY_EXTENSIONS = Set.of(".pem", ".key", ".pfx");

    @LogMethod(
            description = "Validación de request de autenticación OPC UA",
            operation = "VALIDATE_AUTHENTICATION",
            level = LogLevel.INFO,
            sensitiveFields = {"password", "privateKeyPath", "secret"}
    )
    @LogException(
            message = "Error en validación de autenticación OPC UA",
            method = "VALIDATE_AUTHENTICATION_ERROR",
            level = LogLevel.ERROR
    )
    @Auditable(
            value = "AUTH_VALIDATION",
            type = AuditEntryType.OPERATION,
            description = "Validación de credenciales de autenticación OPC UA",
            excludeFields = {"password", "privateKeyPath", "secret"}
    )
    public void validateAuthentication(OpcUaAuthenticationRequest request) throws ValidationRequestException {
        List<String> errors = new ArrayList<>();
        validateNotNull(request, "request de autenticación", errors);

        if (request != null) {
            validateSecurityPolicy(request.getSecurityPolicy().name(), errors);
            validateMessageSecurityMode(request.getMessageSecurityMode().name(), errors);
            validateCertificatePaths(request, errors);
            validateAuthenticationCredentials(request, errors);
        }
        throwIfErrors(errors, "autenticación OPC UA");
    }

    @LogMethod(
            description = "Validación de parámetros de conexión OPC UA",
            operation = "VALIDATE_CONNECTION",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error en validación de conexión OPC UA",
            method = "VALIDATE_CONNECTION_ERROR",
            level = LogLevel.ERROR,
            stackTrace = {""}
    )
    @Auditable(
            value = "CONNECTION_VALIDATION",
            type = AuditEntryType.OPERATION,
            description = "Validación de parámetros de conexión OPC UA"
    )
    public void validateConnection(OpcUaConnectionRequest request) throws ValidationRequestException {
        List<String> errors = new ArrayList<>();
        validateNotNull(request, "request de conexión", errors);

        if (request != null) {
            validateEndpointUrl(request.getEndpointUrl(), errors);
            validateApplicationIdentity(request, errors);
            validateConnectionParameters(request, errors);
        }
        throwIfErrors(errors, "conexión OPC UA");
    }

    @LogMethod(
            description = "Validación de configuración de sesión OPC UA",
            operation = "VALIDATE_SESSION",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error en validación de sesión OPC UA",
            method = "VALIDATE_SESSION_ERROR",
            level = LogLevel.ERROR,
            stackTrace = {""}
    )
    @Auditable(
            value = "SESSION_VALIDATION",
            type = AuditEntryType.OPERATION,
            description = "Validación de parámetros de sesión OPC UA"
    )
    public void validateSession(OpcUaSessionRequest request) throws ValidationRequestException {
        List<String> errors = new ArrayList<>();
        validateNotNull(request, "request de sesión", errors);

        if (request != null) {
            validateSessionName(request.getSessionName(), errors);
            validateTimeout(request.getTimeout(), "tiempo de sesión", errors);
        }
        throwIfErrors(errors, "sesión OPC UA");
    }

    @LogMethod(
            description = "Validación de configuración de suscripción OPC UA",
            operation = "VALIDATE_SUBSCRIPTION",
            level = LogLevel.INFO
    )
    @LogException(
            message = "Error en validación de suscripción OPC UA",
            method = "VALIDATE_SUBSCRIPTION_ERROR",
            level = LogLevel.ERROR,
            stackTrace = {""}
    )
    @Auditable(
            value = "SUBSCRIPTION_VALIDATION",
            type = AuditEntryType.OPERATION,
            description = "Validación de parámetros de suscripción OPC UA"
    )
    public void validateSubscription(SubscriptionRequest request) throws ValidationRequestException {
        List<String> errors = new ArrayList<>();
        validateNotNull(request, "request de suscripción", errors);

        if (request != null) {
            validatePublishingInterval(request.getPublishingInterval(), errors);
            validateLifetimeCount(Long.valueOf(request.getLifetimeCount()), errors);
            validateMaxKeepAliveCount(Long.valueOf(request.getMaxKeepAliveCount()), errors);
            validateMaxNotificationsPerPublish(Long.valueOf(request.getMaxNotificationsPerPublish()), errors);
            validatePriority(request.getPriority(), errors);
        }
        throwIfErrors(errors, "suscripción OPC UA");
    }

    private void validateNotNull(Object value, String fieldName, List<String> errors) {
        if (value == null) {
            errors.add(String.format("El %s no puede ser nulo", fieldName));
        }
    }

    private void validateSecurityPolicy(String securityPolicy, List<String> errors) {
        if (securityPolicy == null || securityPolicy.trim().isEmpty()) {
            errors.add("La política de seguridad no puede estar vacía");
        }
    }

    private void validateMessageSecurityMode(String securityMode, List<String> errors) {
        if (securityMode == null || securityMode.trim().isEmpty()) {
            errors.add("El modo de seguridad de mensajes no puede estar vacío");
        }
    }

    private void validateCertificatePaths(OpcUaAuthenticationRequest request, List<String> errors) {
        if (request.getCertificatePath() != null) {
            validateFilePath(request.getCertificatePath(), VALID_CERTIFICATE_EXTENSIONS, "certificado", errors);
        }
        if (request.getPrivateKeyPath() != null) {
            validateFilePath(request.getPrivateKeyPath(), VALID_KEY_EXTENSIONS, "llave privada", errors);
        }
    }

    private void validateFilePath(String path, Set<String> validExtensions, String fileType, List<String> errors) {
        if (path.trim().isEmpty()) {
            errors.add(String.format("La ruta del %s no puede estar vacía", fileType));
            return;
        }

        boolean hasValidExtension = validExtensions.stream()
                .anyMatch(ext -> path.toLowerCase().endsWith(ext));
        if (!hasValidExtension) {
            errors.add(String.format("El archivo de %s debe tener una extensión válida %s",
                    fileType, validExtensions));
        }

        File file = new File(path);
        if (!file.exists()) {
            errors.add(String.format("El archivo de %s no existe en la ruta especificada", fileType));
        } else if (!file.isFile()) {
            errors.add(String.format("La ruta especificada para el %s no es un archivo", fileType));
        }
    }

    private void validateAuthenticationCredentials(OpcUaAuthenticationRequest credentials, List<String> errors) {
        if (credentials == null) {
            errors.add("Las credenciales de autenticación no pueden ser nulas");
            return;
        }
        if (credentials.getUsername() != null && credentials.getUsername().trim().isEmpty()) {
            errors.add("El nombre de usuario no puede estar vacío");
        }
        if (credentials.getPassword() != null && credentials.getPassword().trim().isEmpty()) {
            errors.add("La contraseña no puede estar vacía");
        }
    }

    private void validateEndpointUrl(String url, List<String> errors) {
        if (url == null || url.trim().isEmpty()) {
            errors.add("La URL del endpoint no puede estar vacía");
            return;
        }

        if (!URL_PATTERN.matcher(url).matches()) {
            errors.add("La URL del endpoint tiene un formato inválido");
        }

        validatePort(url, errors);
    }

    private void validatePort(String url, List<String> errors) {
        try {
            URI uri = new URI(url);
            if (uri.getPort() != -1) {
                int port = uri.getPort();
                if (port < MIN_PORT || port > MAX_PORT) {
                    errors.add(String.format("El puerto debe estar entre %d y %d", MIN_PORT, MAX_PORT));
                }
            }
        } catch (URISyntaxException e) {
            errors.add("La URL tiene un formato inválido");
        }
    }

    private void validateApplicationIdentity(OpcUaConnectionRequest request, List<String> errors) {
        if (request.getApplicationName() == null || request.getApplicationName().trim().isEmpty()) {
            errors.add("El nombre de la aplicación no puede estar vacío");
        }
        if (request.getApplicationUri() == null || request.getApplicationUri().trim().isEmpty()) {
            errors.add("El URI de la aplicación no puede estar vacío");
        }
    }

    private void validateConnectionParameters(OpcUaConnectionRequest request, List<String> errors) {
        validateTimeout(request.getTimeout(), "tiempo de conexión", errors);
        validateTimeout(request.getChannelLifetime(), "tiempo de vida del canal", errors);
    }

    private void validateTimeout(Long timeout, String timeoutType, List<String> errors) {
        if (timeout == null) {
            errors.add(String.format("El %s no puede ser nulo", timeoutType));
        } else if (timeout <= 0) {
            errors.add(String.format("El %s debe ser mayor que cero", timeoutType));
        }
    }

    private void validateSessionName(String sessionName, List<String> errors) {
        if (sessionName == null || sessionName.trim().isEmpty()) {
            errors.add("El nombre de la sesión no puede estar vacío");
        }
    }

    private void validatePublishingInterval(Double publishingInterval, List<String> errors) {
        if (publishingInterval == null) {
            errors.add("El intervalo de publicación no puede ser nulo");
        } else if (publishingInterval <= 0) {
            errors.add("El intervalo de publicación debe ser mayor que cero");
        }
    }

    private void validateLifetimeCount(Long lifetimeCount, List<String> errors) {
        if (lifetimeCount == null) {
            errors.add("El contador de tiempo de vida no puede ser nulo");
        } else if (lifetimeCount <= 0) {
            errors.add("El contador de tiempo de vida debe ser mayor que cero");
        }
    }

    private void validateMaxKeepAliveCount(Long maxKeepAliveCount, List<String> errors) {
        if (maxKeepAliveCount == null) {
            errors.add("El contador máximo de keep-alive no puede ser nulo");
        } else if (maxKeepAliveCount <= 0) {
            errors.add("El contador máximo de keep-alive debe ser mayor que cero");
        }
    }

    private void validateMaxNotificationsPerPublish(Long maxNotifications, List<String> errors) {
        if (maxNotifications == null) {
            errors.add("El número máximo de notificaciones por publicación no puede ser nulo");
        } else if (maxNotifications <= 0) {
            errors.add("El número máximo de notificaciones por publicación debe ser mayor que cero");
        }
    }

    private void validatePriority(Integer priority, List<String> errors) {
        if (priority == null) {
            errors.add("La prioridad no puede ser nula");
        } else if (priority < 0) {
            errors.add("La prioridad no puede ser negativa");
        }
    }

    private void throwIfErrors(List<String> errors, String context) throws ValidationRequestException {
        if (!errors.isEmpty()) {
            throw new ValidationRequestException(
                    String.format("Errores de validación en %s: %s",
                            context,
                            String.join("; ", errors)
                    ));
        }
    }
}
