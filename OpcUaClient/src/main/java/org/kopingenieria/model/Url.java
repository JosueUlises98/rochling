package org.kopingenieria.model;

/**
 * The Url enum represents predefined URLs used for establishing connections
 * with a Programmable Logic Controller (PLC) or other OPC-UA compatible systems.
 * <p>
 * Each enum constant corresponds to a specific OPC-UA address.
 * This enum is designed to provide centralized, reusable, and strongly typed
 * references for connection addresses.
 * <p>
 * Enum Constants:
 * - Adress1: Holds the URL "opc.tcp://192.168.1.100:4840".
 * - Adress2: Holds the URL "opc.tcp://192.168.1.100:4840".
 * <p>
 * Methods Overview:
 * - getUrl(): Retrieves the string representation of the URL associated with the enum constant.
 * - toString(): Returns a string representation of the Url enum instance, including the URL value.
 */
public enum Url {

    //Direccion de red plc prueba
    Adress1("opc.tcp://192.168.1.12:4840"),
    //Direccion de red de loopback
    Adress2("opc.tcp://localserver:4840");

    /**
     * Extracts and returns the protocol from the URL associated with the enum constant.
     * For example, in "opc.tcp://192.168.1.12:4840", it will return "opc.tcp".
     *
     * @return The protocol part of the URL.
     */
    public String getProtocol() {
        if (url != null && url.contains("://")) {
            return url.split("://")[0];
        }
        return null;
    }
    /**
     * Extracts and returns the IP address from the URL associated with the enum constant.
     * For example, in "opc.tcp://192.168.1.12:4840", it will return "192.168.1.12".
     *
     * @return The IP address part of the URL, or null if missing.
     */
    public String getIpAddress() {
        if (url != null && url.contains("://")) {
            String[] parts = url.split("://");
            if (parts.length > 1 && parts[1].contains(":")) {
                return parts[1].split(":")[0];
            }
        }
        return null;
    }
    /**
     * Extracts and returns the port from the URL associated with the enum constant.
     * For example, in "opc.tcp://192.168.1.12:4840", it will return 4840.
     *
     * @return The port part of the URL as an integer, or -1 if no port is found.
     */
    public int getPort() {
        if (url != null && url.contains(":")) {
            try {
                String[] parts = url.split(":");
                if (parts.length > 2) {
                    return Integer.parseInt(parts[2]);
                }
            } catch (NumberFormatException e) {
                // Log error or handle invalid port number, if necessary.
            }
        }
        return -1;
    }

    private String url;

    Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
