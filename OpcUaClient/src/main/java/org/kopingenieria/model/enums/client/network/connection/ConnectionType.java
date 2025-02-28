package org.kopingenieria.model.enums.client.network.connection;

import java.io.Serializable;

public enum ConnectionType implements Serializable {

    OPCUA("industrial-connection"),
    SSH("remote-connection"),
    TLS("secure-connection"),
    TCP("standard-connection");

    private String value;

    ConnectionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public static ConnectionType findByValue(String value) {
        for (ConnectionType type : ConnectionType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("No ConnectionType with value: " + value);
    }
}
