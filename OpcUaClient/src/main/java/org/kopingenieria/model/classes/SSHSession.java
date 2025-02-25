package org.kopingenieria.model.classes;

import jakarta.persistence.Column;

public final class SSHSession extends Session{
    // Configuración de sesión
    @Column(name = "session_timeout")
    private Integer sessionTimeout;

    @Column(name = "connection_timeout")
    private Integer connectionTimeout;

    @Column(name = "channel_timeout")
    private Integer channelTimeout;
}
