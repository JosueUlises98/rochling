package org.kopingenieria.model.classes;

import jakarta.persistence.Column;


public final class TLSSession extends Session {
    // Configuración de sesión
    @Column(name = "session_timeout")
    private Integer sessionTimeout;

    @Column(name = "session_cache_size")
    private Integer sessionCacheSize;

}
