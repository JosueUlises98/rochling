package org.kopingenieria.model.enums.client.network.connection;

import java.io.Serializable;

public enum ConnectionStatus implements Serializable {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    FAILED,
    ERROR,
    RECONNECTING,
    DISCONNECTING,
    UNKNOWN
}
