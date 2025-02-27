package org.kopingenieria.model.enums.network.connection;

public enum ProtocolType {

    OPCUA("opcua"),
    SSH("ssh"),
    TCP("tcp"),
    TLS("tls");

    private final String value;

    ProtocolType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }

    public static ProtocolType fromValue(String value) {
        for (ProtocolType protocolType : ProtocolType.values()) {
            if (protocolType.toString().equalsIgnoreCase(value)) {
                return protocolType;
            }
        }
        throw new IllegalArgumentException("Unknown protocol type: " + value);
    }
}
