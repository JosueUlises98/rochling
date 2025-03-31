package org.kopingenieria.application.validators.bydefault;


import org.eclipse.milo.opcua.sdk.client.OpcUaSession;
import org.eclipse.milo.opcua.stack.core.types.builtin.ByteString;
import org.eclipse.milo.opcua.stack.core.types.builtin.NodeId;
import org.eclipse.milo.opcua.stack.core.types.builtin.unsigned.UInteger;
import org.eclipse.milo.opcua.stack.core.types.structured.SignedSoftwareCertificate;
import org.kopingenieria.application.validators.contracts.AuthenticationValidator;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class DefaultAuthenticationValidator implements AuthenticationValidator {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long TOKEN_VALIDITY_MINUTES = 30;
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$");
    // Cache para almacenar intentos fallidos
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    // Cache para tokens y sus timestamps
    private final Map<String, LocalDateTime> tokenCache = new ConcurrentHashMap<>();
    // Cache para checksums de datos
    private final Map<String, String> dataChecksums = new ConcurrentHashMap<>();


    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de credenciales",description = "Validacion de credenciales del cliente opcua")
    @LogSystemEvent(description = "Validacion de credenciales de cliente opcua ", event = "Validacion de credenciales",level = LogLevel.DEBUG)
    public boolean validateUserCredentials(String username, String password) {
        if (!validarIntegridadDatos(username, password)) {
            return false;
        }
        if (isUserBlocked(username)) {
            return false;
        }
        boolean isValid = verificarIntegridadCredenciales(username, password);
        if (!isValid) {
            registrarIntentoFallido(username);
        } else {
            resetearIntentosFallidos(username);
        }
        return isValid;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del certificado",description = " Validacion del certificado del cliente opcua")
    @LogSystemEvent(description = "Validacion del certificado del cliente opcua", event = "Validacion de certificado",level = LogLevel.DEBUG)
    public boolean validateClientCertificate(String certificate) {
        if (!validarIntegridadDatos(certificate)) {
            return false;
        }
        try {
            return verificarIntegridadCertificado(certificate);
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de la sesion",description = "Validacion de la sesion del cliente opcua")
    @LogSystemEvent(description = "Validacion de la sesion del cliente opcua", event = "Validacion de sesion opcua",level = LogLevel.DEBUG)
    public boolean isSessionValid(String token, Object... datosSessionOpcUa) {
        try {
            // 1. Validar que el token exista y esté activo
            if (!isActiveTokenSession(token)) {
                throw new IllegalStateException("La sesión no está activa o el token es inválido");
            }

            // 2. Validar que el timestamp del token sea válido
            if (!createTimeStampToken(token)) {
                throw new IllegalStateException("No se pudo actualizar el timestamp del token");
            }

            // 3. Validar el esquema de la sesión OPC UA
            if (!validateSchematicSessionToken(datosSessionOpcUa)) {
                throw new IllegalStateException("Los datos de la sesión OPC UA no son válidos");
            }

            return true;

        } catch (Exception e) {
            // Considerar logging del error
            System.err.println("Error en la validación de sesión: " + e.getMessage());
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del token de sesion",description = "Validacion del token de sesion del cliente opcua")
    @LogSystemEvent(description = "Validacion del token de sesion opcua", event = "Validacion del token de sesion opcua",level = LogLevel.DEBUG)
    public boolean isSessionTokenValid(String token) {
        if (!validarIntegridadDatos(token)) {
            return false;
        }
        LocalDateTime tokenTimestamp = tokenCache.get(token);
        if (tokenTimestamp == null) {
            return false;
        }
        return !tokenExpirado(tokenTimestamp);
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de complejidad del password",description = "Validacion de complejidad del password del cliente opcua")
    @LogSystemEvent(description = "Validacion de complejidad del password del cliente opcua ", event = "Validacion de complejidad de password",level = LogLevel.DEBUG)
    public boolean enforcePasswordComplexity(String password) {
        if (!validarIntegridadDatos(password)) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    private boolean validarIntegridadDatos(String... datos) {
        for (String dato : datos) {
            if (dato == null || dato.trim().isEmpty()) {
                return false;
            }
            String nuevoChecksum = calcularChecksum(dato);
            String checksumPrevio = dataChecksums.get(dato);
            if (checksumPrevio != null && !checksumPrevio.equals(nuevoChecksum)) {
                return false;
            }
            dataChecksums.put(dato, nuevoChecksum);
        }
        return true;
    }

    private boolean isActiveTokenSession(String token) {
        Objects.requireNonNull(token,"Se necesita el token para validar su actividad");
        LocalDateTime tokenTimestamp = tokenCache.get(token);
        if (tokenTimestamp == null) {
            return false;
        }
        return !(tokenTimestamp.isAfter(LocalDateTime.now()));
    }

    private boolean createTimeStampToken(String token){
        Objects.requireNonNull(token,"Se necesita el token para crear su timestamp");
        LocalDateTime tokenTimeStamp = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);
        tokenCache.put(token,tokenTimeStamp);
        return true;
    }

    //Se crea una sesion opcua de prueba para validar los parametros necesarios
    private boolean validateSchematicSessionToken(Object...dataSessionToken){
        Objects.requireNonNull(dataSessionToken,"Se necesitan los datos de la sesion para validarlos");
        OpcUaSession opcUaSession = new OpcUaSession((NodeId) dataSessionToken[0],(NodeId) dataSessionToken[1],
                (String)dataSessionToken[2],
                (Double)dataSessionToken[3],
                (UInteger) dataSessionToken[4],
                (ByteString) dataSessionToken[5],
                (SignedSoftwareCertificate[]) dataSessionToken[6]);
        NodeId sessionId = opcUaSession.getSessionId();
        return sessionId.equals(dataSessionToken[1]);
    }

    private boolean verificarIntegridadCredenciales(String username, String password) {
        // Verificación de formato y caracteres válidos
        if (!username.matches("^[a-zA-Z0-9._-]{3,50}$")) {
            return false;
        }
        // Verificación de consistencia de datos
        String credentialHash = calcularChecksum(username + password);
        return dataChecksums.containsKey(credentialHash);
    }

    private boolean verificarIntegridadCertificado(String certificate) {
        try {
            // Verificar formato Base64
            Base64.getDecoder().decode(certificate);
            // Verificar checksum
            String certificateChecksum = calcularChecksum(certificate);
            return dataChecksums.containsKey(certificateChecksum);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String calcularChecksum(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isUserBlocked(String username) {
        return failedAttempts.getOrDefault(username, 0) >= MAX_FAILED_ATTEMPTS;
    }

    private void registrarIntentoFallido(String username) {
        failedAttempts.merge(username, 1, Integer::sum);
    }

    private void resetearIntentosFallidos(String username) {
        failedAttempts.remove(username);
    }

    private boolean tokenExpirado(LocalDateTime timestamp) {
        return LocalDateTime.now().isAfter(timestamp.plusMinutes(TOKEN_VALIDITY_MINUTES));
    }

    public void limpiarCaches() {
        LocalDateTime ahora = LocalDateTime.now();
        tokenCache.entrySet().removeIf(entry ->
                tokenExpirado(entry.getValue()));
        // Limpiar checksums antiguos (por ejemplo, más de 24 horas)
        dataChecksums.clear(); // En una implementación real, usar criterios específicos
    }
}
