package org.kopingenieria.model;

/**
 * The Url enum represents predefined URLs used for establishing connections
 * with a Programmable Logic Controller (PLC) or other OPC-UA compatible systems.
 *
 * Each enum constant corresponds to a specific OPC-UA address.
 * This enum is designed to provide centralized, reusable, and strongly typed
 * references for connection addresses.
 *
 * Enum Constants:
 * - Adress1: Holds the URL "opc.tcp://192.168.1.100:4840".
 * - Adress2: Holds the URL "opc.tcp://192.168.1.100:4840".
 *
 * Methods Overview:
 * - getUrl(): Retrieves the string representation of the URL associated with the enum constant.
 * - toString(): Returns a string representation of the Url enum instance, including the URL value.
 */
public enum Url {

    //Direccion de red plc prueba
    Adress1("opc.tcp://192.168.1.12:4840"),
    //Direccion de red de loopback
    Adress2("opc.tcp://localserver:4840");

    private String url;

    Url(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public String toString() {
        return "Url{" +
                "url='" + url + '\'' +
                '}';
    }
}
