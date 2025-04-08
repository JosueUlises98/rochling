package org.kopingenieria.application.validators.bydefault;

import io.micrometer.common.util.StringUtils;
import org.eclipse.milo.opcua.sdk.client.api.UaClient;
import org.kopingenieria.audit.model.AuditEntryType;
import org.kopingenieria.audit.model.annotation.Auditable;
import org.kopingenieria.domain.enums.connection.Timeouts;
import org.kopingenieria.domain.enums.connection.UrlType;
import org.kopingenieria.logging.model.LogLevel;
import org.kopingenieria.logging.model.LogSystemEvent;
import java.net.InetAddress;
import java.net.URI;


public class DefaultConnectionValidatorImpl implements DefaultConnectionValidator {

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion activa",description = "Validacion de sesion activa opcua")
    @LogSystemEvent(description = "Validacion de sesion activa opcua", event = "Validacion de sesion activa",level = LogLevel.DEBUG)
    public boolean validateActiveSession(UaClient client) {
        try {
            if (client == null) {
                return false;
            }
            return !client.getSession().isDone();
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de sesion",description = "Validacion de sesion opcua")
    @LogSystemEvent(description = "Validacion de sesion valida opcua", event = "Validacion de sesion valida",level = LogLevel.DEBUG)
    public boolean validateValidSession(UaClient client) {
        if (StringUtils.isBlank(client.toString())) {
            return false;
        }
        try {
            return client.getSession() != null && !client.getSession().isDone();
        } catch (Exception e) {
            return false;
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de host",description = "Validacion de host opcua")
    @LogSystemEvent(description = "Validacion de host opcua", event = "Validacion de host",level = LogLevel.DEBUG)
    public boolean validateHost(String host) {
        if (StringUtils.isBlank(host)) {
            return false;
        }
        try {
            URI uri = new URI(host);
            InetAddress name = InetAddress.getByName(uri.getHost());// Intentamos resolver el nombre
            //Si y solo si
            if (name != null) {
                if (name.getHostAddress().equalsIgnoreCase(UrlType.OPCUA_REMOTE.getIpAddress())) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de puerto",description = "Validacion de puerto opcua")
    @LogSystemEvent(description = "Validacion de puerto opcua", event = "Validacion de puerto",level = LogLevel.DEBUG)
    public boolean validatePort(int port) {
        if (StringUtils.isBlank(String.valueOf(port))) {
            return false;
        }
        if (port < 0 || port > 65535) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de endpoint",description = "Validacion de endpoint opcua")
    @LogSystemEvent(description = "Validacion de endpoint opcua", event = "Validacion de endpoint",level = LogLevel.DEBUG)
    public boolean validateEndpoint(String endpoint) {
        if (StringUtils.isBlank(endpoint)) {
            return false;
        }
        switch (endpoint){
            case UrlType.OpcUa.LOCAL, UrlType.OpcUa.REMOTE, UrlType.OpcUa.SECURE -> {
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion de timeout",description = "Validacion de timeout de conexion opcua")
    @LogSystemEvent(description = "Validacion de timeout de conexion opcua", event = "Validacion de timeout de conexion",level = LogLevel.DEBUG)
    public boolean validateTimeout(int timeout) {
        if (StringUtils.isBlank(String.valueOf(timeout))) {
            return false;
        }
        if (timeout <= 0) {
            return false;
        }
        if (timeout > Timeouts.CONNECTION.toMilliseconds()) {
            return false;
        }
        return true;
    }

    @Auditable(type = AuditEntryType.OPERATION,value = "Validacion del host local",description = "Validacion del host local opcua")
    @LogSystemEvent(description = "Validacion del host local opcua", event = "Validacion del host local",level = LogLevel.DEBUG)
    public boolean validateLocalHost(String host) {
        try {
            URI uri = new URI(host);
            InetAddress name = InetAddress.getByName(uri.getHost());// Intentamos resolver el nombre
            if (name != null) {
                if (name.isLoopbackAddress()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }
}

