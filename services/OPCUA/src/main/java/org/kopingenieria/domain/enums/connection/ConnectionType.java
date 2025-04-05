package org.kopingenieria.domain.enums.connection;

import java.io.Serializable;

public enum ConnectionType implements Serializable {

    OPCUA("industrial-connection");

    private String value;
    
    private static final String OFFICIAL_URI = "https://www.opcfoundation.org";

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
    
    public static String getOfficialUri() {
        return OFFICIAL_URI;
    }
    
}
